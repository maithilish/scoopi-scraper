package org.codetab.scoopi.step.mediator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.helper.Snooze;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.store.IJobStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class JobRunnerTest {
    @InjectMocks
    private JobRunner jobRunner;

    @Mock
    private Configs configs;
    @Mock
    private TaskMediator taskMediator;
    @Mock
    private IJobStore jobStore;
    @Mock
    private StateFliper stateFliper;
    @Mock
    private AtomicBoolean cancelled;
    @Mock
    private Snooze snooze;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCancel() {
        jobRunner.cancel();

        verify(cancelled).set(true);
        verify(stateFliper).cancel();
    }

    @Test
    public void testRunStateTerminated() throws Exception {
        when(stateFliper.isTMState(TMState.TERMINATED)).thenReturn(true);
        when(stateFliper.isTMState(TMState.SHUTDOWN)).thenReturn(false);
        when(cancelled.get()).thenReturn(false);

        jobRunner.run();

        verifyNoInteractions(jobStore, taskMediator);
    }

    @Test
    public void testRunStateShutdown() throws Exception {
        when(stateFliper.isTMState(TMState.TERMINATED)).thenReturn(false);
        when(stateFliper.isTMState(TMState.SHUTDOWN)).thenReturn(true);
        when(cancelled.get()).thenReturn(false);

        jobRunner.run();

        verifyNoInteractions(jobStore, taskMediator);
    }

    @Test
    public void testRunStateCancelled() throws Exception {
        when(stateFliper.isTMState(TMState.TERMINATED)).thenReturn(false);
        when(stateFliper.isTMState(TMState.SHUTDOWN)).thenReturn(false);
        when(cancelled.get()).thenReturn(true);

        jobRunner.run();

        verifyNoInteractions(jobStore, taskMediator);
    }

    @Test
    public void testRunPushPayload() throws Exception {
        int jobTakeRetryDelay = 1;
        Payload payload = Mockito.mock(Payload.class);

        TMState tMState = TMState.READY;
        int aquireLockTimeout = 50;

        when(configs.getInt("scoopi.job.takeRetryDelay", "50"))
                .thenReturn(jobTakeRetryDelay);

        when(stateFliper.isTMState(TMState.TERMINATED)).thenReturn(false)
                .thenReturn(true); // to break while
        when(stateFliper.isTMState(TMState.SHUTDOWN)).thenReturn(false);
        when(cancelled.get()).thenReturn(false);

        when(jobStore.takeJob()).thenReturn(payload);
        when(stateFliper.getTMState()).thenReturn(tMState);
        jobRunner.run();

        verify(jobStore).resetCrashedJobs();
        verify(stateFliper).acquireJobToTaskQueueLock(aquireLockTimeout,
                TimeUnit.MILLISECONDS);
        verify(taskMediator).pushPayload(payload);
        verify(stateFliper).releaseJobToTaskQueueLock();
    }

    @Test
    public void testRunPushPayloadNoJobException() throws Exception {
        int jobTakeRetryDelay = 1;
        Payload payload = Mockito.mock(Payload.class);

        TMState tMState = TMState.READY;
        int aquireLockTimeout = 50;

        when(configs.getInt("scoopi.job.takeRetryDelay", "50"))
                .thenReturn(jobTakeRetryDelay);

        when(stateFliper.isTMState(TMState.TERMINATED)).thenReturn(false)
                .thenReturn(true); // to break while
        when(stateFliper.isTMState(TMState.SHUTDOWN)).thenReturn(false);
        when(cancelled.get()).thenReturn(false);

        when(jobStore.takeJob()).thenThrow(NoSuchElementException.class);
        when(stateFliper.getTMState()).thenReturn(tMState);

        jobRunner.run();

        verify(jobStore).resetCrashedJobs();
        verify(stateFliper).acquireJobToTaskQueueLock(aquireLockTimeout,
                TimeUnit.MILLISECONDS);
        verify(taskMediator, never()).pushPayload(payload);
        verify(stateFliper).releaseJobToTaskQueueLock();
        verify(snooze).sleepUninterruptibly(jobTakeRetryDelay);
    }

    @Test
    public void testRunInterrupted() throws Exception {
        int jobTakeRetryDelay = 1;
        Payload payload = Mockito.mock(Payload.class);

        TMState tMState = TMState.READY;
        int aquireLockTimeout = 50;

        when(configs.getInt("scoopi.job.takeRetryDelay", "50"))
                .thenReturn(jobTakeRetryDelay);

        when(stateFliper.isTMState(TMState.TERMINATED)).thenReturn(false)
                .thenReturn(true); // to break while
        when(stateFliper.isTMState(TMState.SHUTDOWN)).thenReturn(false);
        when(cancelled.get()).thenReturn(false);

        when(jobStore.takeJob()).thenThrow(InterruptedException.class);
        when(stateFliper.getTMState()).thenReturn(tMState);

        jobRunner.run();

        assertTrue(Thread.currentThread().isInterrupted());

        verify(jobStore).resetCrashedJobs();
        verify(stateFliper).acquireJobToTaskQueueLock(aquireLockTimeout,
                TimeUnit.MILLISECONDS);
        verify(taskMediator, never()).pushPayload(payload);
        verify(stateFliper).releaseJobToTaskQueueLock();
    }

    @Test
    public void testRunPushPayloadIllegalState() throws Exception {
        int jobTakeRetryDelay = 1;
        Payload payload = Mockito.mock(Payload.class);

        TMState tMState = TMState.READY;
        int aquireLockTimeout = 50;

        when(configs.getInt("scoopi.job.takeRetryDelay", "50"))
                .thenReturn(jobTakeRetryDelay);

        when(stateFliper.isTMState(TMState.TERMINATED)).thenReturn(false)
                .thenReturn(true); // to break while
        when(stateFliper.isTMState(TMState.SHUTDOWN)).thenReturn(false);
        when(cancelled.get()).thenReturn(false);

        when(jobStore.takeJob()).thenThrow(IllegalStateException.class);
        when(stateFliper.getTMState()).thenReturn(tMState);

        jobRunner.run();

        assertFalse(Thread.currentThread().isInterrupted());

        verify(jobStore).resetCrashedJobs();
        verify(stateFliper).acquireJobToTaskQueueLock(aquireLockTimeout,
                TimeUnit.MILLISECONDS);
        verify(taskMediator, never()).pushPayload(payload);
        verify(stateFliper).releaseJobToTaskQueueLock();
    }

}
