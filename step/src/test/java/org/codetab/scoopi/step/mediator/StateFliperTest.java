package org.codetab.scoopi.step.mediator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.codetab.scoopi.store.IPayloadStore;
import org.codetab.scoopi.store.IShutdown;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class StateFliperTest {
    @InjectMocks
    private StateFliper stateFliper;

    @Mock
    private IShutdown shutdown;
    @Mock
    private TaskPoolService poolService;
    @Mock
    private IPayloadStore payloadStore;
    @Mock
    private AtomicReference<TMState> tmState;
    @Mock
    private Semaphore jobToTaskQueueLock;
    @Mock
    private Semaphore taskQueueToPoolLock;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCancel() {
        stateFliper.cancel();

        verify(shutdown).cancel();
    }

    @Test
    public void testTryTMShutdownIsCancelled() {
        boolean poolServiceDone = true;
        boolean grape = true;
        boolean payloadStoreDone = true;

        when(payloadStore.isDone()).thenReturn(payloadStoreDone)
                .thenReturn(payloadStoreDone);
        when(poolService.isDone()).thenReturn(poolServiceDone);

        when(shutdown.isCancelled()).thenReturn(grape);

        stateFliper.tryTMShutdown();

        verify(tmState).set(TMState.SHUTDOWN);
        verify(shutdown, never()).setDone();

        verifyNoInteractions(jobToTaskQueueLock, taskQueueToPoolLock);
    }

    @Test
    public void testTryTMShutdown() {
        boolean poolServiceDone = true;
        boolean grape = false; // not cancelled
        boolean payloadStoreDone = true;
        boolean allNodesDone = true;
        boolean jobStoreDone = true;

        when(payloadStore.isDone()).thenReturn(payloadStoreDone)
                .thenReturn(payloadStoreDone);
        when(poolService.isDone()).thenReturn(poolServiceDone);
        when(shutdown.isCancelled()).thenReturn(grape);
        when(shutdown.allNodesDone()).thenReturn(allNodesDone)
                .thenReturn(allNodesDone);
        when(shutdown.jobStoreDone()).thenReturn(jobStoreDone)
                .thenReturn(jobStoreDone);
        stateFliper.tryTMShutdown();

        verify(tmState).set(TMState.SHUTDOWN);
        verify(shutdown).setDone();
        verify(jobToTaskQueueLock).acquireUninterruptibly();
        verify(taskQueueToPoolLock).acquireUninterruptibly();
        verify(tmState).set(TMState.SHUTDOWN);
        verify(taskQueueToPoolLock).release();
        verify(jobToTaskQueueLock).release();
        verify(tmState, never()).set(TMState.READY);
    }

    @Test
    public void testTryTMShutdownNoShutDown1() {
        boolean poolServiceDone = true;
        boolean grape = false; // not cancelled
        boolean payloadStoreDone = true;
        boolean allNodesDone = true;
        boolean jobStoreDone = true;

        when(shutdown.isCancelled()).thenReturn(grape);

        when(payloadStore.isDone()).thenReturn(payloadStoreDone)
                .thenReturn(false);
        when(poolService.isDone()).thenReturn(poolServiceDone);
        when(shutdown.allNodesDone()).thenReturn(allNodesDone);
        when(shutdown.jobStoreDone()).thenReturn(jobStoreDone);

        stateFliper.tryTMShutdown();

        verify(tmState, never()).set(TMState.SHUTDOWN);
        verify(shutdown).setDone();
        verify(jobToTaskQueueLock).acquireUninterruptibly();
        verify(taskQueueToPoolLock).acquireUninterruptibly();
        verify(taskQueueToPoolLock).release();
        verify(jobToTaskQueueLock).release();
        verify(tmState, never()).set(TMState.READY);
    }

    @Test
    public void testTryTMShutdownNoShutDown2() {
        boolean poolServiceDone = true;
        boolean grape = false; // not cancelled
        boolean payloadStoreDone = true;
        boolean allNodesDone = true;
        boolean jobStoreDone = true;

        when(shutdown.isCancelled()).thenReturn(grape);

        when(payloadStore.isDone()).thenReturn(payloadStoreDone);
        when(poolService.isDone()).thenReturn(poolServiceDone)
                .thenReturn(false);
        when(shutdown.allNodesDone()).thenReturn(allNodesDone);
        when(shutdown.jobStoreDone()).thenReturn(jobStoreDone);

        stateFliper.tryTMShutdown();

        verify(tmState, never()).set(TMState.SHUTDOWN);
        verify(shutdown).setDone();
        verify(jobToTaskQueueLock).acquireUninterruptibly();
        verify(taskQueueToPoolLock).acquireUninterruptibly();
        verify(taskQueueToPoolLock).release();
        verify(jobToTaskQueueLock).release();
        verify(tmState, never()).set(TMState.READY);
    }

    @Test
    public void testTryTMShutdownNoShutDown3() {
        boolean poolServiceDone = true;
        boolean grape = false; // not cancelled
        boolean payloadStoreDone = true;
        boolean allNodesDone = true;
        boolean jobStoreDone = true;

        when(shutdown.isCancelled()).thenReturn(grape);

        when(payloadStore.isDone()).thenReturn(payloadStoreDone);
        when(poolService.isDone()).thenReturn(poolServiceDone);
        when(shutdown.allNodesDone()).thenReturn(allNodesDone)
                .thenReturn(false);
        when(shutdown.jobStoreDone()).thenReturn(jobStoreDone);

        stateFliper.tryTMShutdown();

        verify(tmState, never()).set(TMState.SHUTDOWN);
        verify(shutdown).setDone();
        verify(jobToTaskQueueLock).acquireUninterruptibly();
        verify(taskQueueToPoolLock).acquireUninterruptibly();
        verify(taskQueueToPoolLock).release();
        verify(jobToTaskQueueLock).release();
        verify(tmState, never()).set(TMState.READY);
    }

    @Test
    public void testTryTMShutdownNoShutDown4() {
        boolean poolServiceDone = true;
        boolean grape = false; // not cancelled
        boolean payloadStoreDone = true;
        boolean allNodesDone = true;
        boolean jobStoreDone = true;

        when(shutdown.isCancelled()).thenReturn(grape);

        when(payloadStore.isDone()).thenReturn(payloadStoreDone);
        when(poolService.isDone()).thenReturn(poolServiceDone);
        when(shutdown.allNodesDone()).thenReturn(allNodesDone);
        when(shutdown.jobStoreDone()).thenReturn(jobStoreDone)
                .thenReturn(false);

        stateFliper.tryTMShutdown();

        verify(tmState, never()).set(TMState.SHUTDOWN);
        verify(shutdown).setDone();
        verify(jobToTaskQueueLock).acquireUninterruptibly();
        verify(taskQueueToPoolLock).acquireUninterruptibly();
        verify(taskQueueToPoolLock).release();
        verify(jobToTaskQueueLock).release();
        verify(tmState, never()).set(TMState.READY);
    }

    @Test
    public void testTryTMShutdownAllNodesNotDone() {
        boolean poolServiceDone = true;
        boolean grape = false; // not cancelled
        boolean payloadStoreDone = true;
        boolean allNodesDone = false; // not done
        boolean jobStoreDone = true;

        when(payloadStore.isDone()).thenReturn(payloadStoreDone)
                .thenReturn(payloadStoreDone);
        when(poolService.isDone()).thenReturn(poolServiceDone);
        when(shutdown.isCancelled()).thenReturn(grape);
        when(shutdown.allNodesDone()).thenReturn(allNodesDone)
                .thenReturn(allNodesDone);
        when(shutdown.jobStoreDone()).thenReturn(jobStoreDone)
                .thenReturn(jobStoreDone);
        stateFliper.tryTMShutdown();

        verify(tmState, never()).set(TMState.SHUTDOWN);
        verify(shutdown).setDone();
        verify(tmState, never()).set(TMState.SHUTDOWN);
        verify(tmState).set(TMState.READY);

        verifyNoInteractions(jobToTaskQueueLock, taskQueueToPoolLock);
    }

    @Test
    public void testTryTMShutdownJobStoreNotDone() {
        boolean poolServiceDone = true;
        boolean grape = false; // not cancelled
        boolean payloadStoreDone = true;
        boolean allNodesDone = true;
        boolean jobStoreDone = false; // not done

        when(payloadStore.isDone()).thenReturn(payloadStoreDone)
                .thenReturn(payloadStoreDone);
        when(poolService.isDone()).thenReturn(poolServiceDone);
        when(shutdown.isCancelled()).thenReturn(grape);
        when(shutdown.allNodesDone()).thenReturn(allNodesDone)
                .thenReturn(allNodesDone);
        when(shutdown.jobStoreDone()).thenReturn(jobStoreDone)
                .thenReturn(jobStoreDone);

        stateFliper.tryTMShutdown();

        verify(tmState, never()).set(TMState.SHUTDOWN);
        verify(shutdown).setDone();
        verify(tmState, never()).set(TMState.SHUTDOWN);
        verify(tmState).set(TMState.READY);

        verifyNoInteractions(jobToTaskQueueLock, taskQueueToPoolLock);
    }

    @Test
    public void testTryTMShutdownPayloadStoreNotDone() {
        boolean poolServiceDone = true;
        boolean payloadStoreDone = false; // not done

        when(payloadStore.isDone()).thenReturn(payloadStoreDone)
                .thenReturn(payloadStoreDone);
        when(poolService.isDone()).thenReturn(poolServiceDone);

        stateFliper.tryTMShutdown();

        verifyNoInteractions(tmState, shutdown, jobToTaskQueueLock,
                taskQueueToPoolLock);
    }

    @Test
    public void testTryTMShutdownPoolServiceNotDone() {
        boolean poolServiceDone = false; // not done
        boolean payloadStoreDone = true;

        when(payloadStore.isDone()).thenReturn(payloadStoreDone)
                .thenReturn(payloadStoreDone);
        when(poolService.isDone()).thenReturn(poolServiceDone);

        stateFliper.tryTMShutdown();

        verifyNoInteractions(tmState, shutdown, jobToTaskQueueLock,
                taskQueueToPoolLock);
    }

    @Test
    public void testGetTMState() {
        TMState state = TMState.READY;

        when(tmState.get()).thenReturn(state);

        Object actual = stateFliper.getTMState();

        assertSame(state, actual);
    }

    @Test
    public void testSetTMState() {
        TMState state = TMState.READY;

        stateFliper.setTMState(state);

        verify(tmState).set(state);
    }

    @Test
    public void testIsTMState() {
        TMState other = TMState.READY;

        when(tmState.get()).thenReturn(TMState.READY)
                .thenReturn(TMState.SHUTDOWN);

        assertTrue(stateFliper.isTMState(other));
        assertFalse(stateFliper.isTMState(other));
    }

    @Test
    public void testAcquireJobToTaskQueueLock() throws Exception {
        int timeout = 1;
        TimeUnit timeoutUnit = TimeUnit.DAYS;
        stateFliper.acquireJobToTaskQueueLock(timeout, timeoutUnit);

        verify(jobToTaskQueueLock).tryAcquire(timeout, timeoutUnit);
    }

    @Test
    public void testReleaseJobToTaskQueueLock() {
        stateFliper.releaseJobToTaskQueueLock();

        verify(jobToTaskQueueLock).release();
    }

    @Test
    public void testAcquireTaskQueueToPoolLock() throws Exception {
        int timeout = 1;
        TimeUnit timeoutUnit = TimeUnit.DAYS;
        stateFliper.acquireTaskQueueToPoolLock(timeout, timeoutUnit);

        verify(taskQueueToPoolLock).tryAcquire(timeout, timeoutUnit);
    }

    @Test
    public void testReleaseTaskQueueToPoolLock() {
        stateFliper.releaseTaskQueueToPoolLock();

        verify(taskQueueToPoolLock).release();
    }
}
