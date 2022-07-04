package org.codetab.scoopi.step.mediator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.step.IStep;
import org.codetab.scoopi.step.Task;
import org.codetab.scoopi.store.IPayloadStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class TaskRunnerTest {
    @InjectMocks
    private TaskRunner taskRunner;

    @Mock
    private Configs configs;
    @Mock
    private TaskPoolService poolService;
    @Mock
    private IPayloadStore payloadStore;
    @Mock
    private StateFliper stateFliper;
    @Mock
    private TaskFactory taskFactory;
    @Mock
    private Errors errors;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRunStateIsShutdown() throws Exception {
        int taskTakeTimeout = 1;
        boolean apple = true;
        Task task = Mockito.mock(Task.class);
        String poolName = "Foo";
        int aquireLockTimeout = 50;

        when(configs.getInt("scoopi.task.takeTimeout", "500"))
                .thenReturn(taskTakeTimeout);
        when(stateFliper.isTMState(TMState.SHUTDOWN)).thenReturn(apple);

        taskRunner.run();

        verify(poolService).waitForFinish();
        verify(stateFliper, never()).acquireTaskQueueToPoolLock(
                aquireLockTimeout, TimeUnit.MILLISECONDS);
        verify(poolService, never()).submit(poolName, task);
        verify(stateFliper, never()).releaseTaskQueueToPoolLock();
        verify(errors, never()).inc();
    }

    @Test
    public void testRun() throws Exception {
        int taskTakeTimeout = 1;
        int takeTimeout = 0;
        int grape = 1;
        Payload payload = Mockito.mock(Payload.class);
        Task task = Mockito.mock(Task.class);
        IStep iStep = Mockito.mock(IStep.class);
        String poolName = "Foo";
        int aquireLockTimeout = 50;

        when(configs.getInt("scoopi.task.takeTimeout", "500"))
                .thenReturn(taskTakeTimeout);

        when(stateFliper.isTMState(TMState.SHUTDOWN)).thenReturn(false)
                .thenReturn(true); // to break while

        when(payloadStore.getPayloadsCount()).thenReturn(grape);
        when(payloadStore.takePayload(takeTimeout)).thenReturn(payload);
        when(taskFactory.createTask(payload)).thenReturn(task);
        when(task.getStep()).thenReturn(iStep);
        when(iStep.getStepName()).thenReturn(poolName);
        taskRunner.run();

        verify(poolService).waitForFinish();
        verify(stateFliper).acquireTaskQueueToPoolLock(aquireLockTimeout,
                TimeUnit.MILLISECONDS);
        verify(poolService).submit(poolName, task);
        verify(stateFliper).releaseTaskQueueToPoolLock();
        verify(errors, never()).inc();
    }

    @Test
    public void testRunZeroPayload() throws Exception {
        int taskTakeTimeout = 1;
        int takeTimeout = taskTakeTimeout;
        int grape = 0; // payload count is zero
        Payload payload = Mockito.mock(Payload.class);
        Task task = Mockito.mock(Task.class);
        IStep iStep = Mockito.mock(IStep.class);
        String poolName = "Foo";
        int aquireLockTimeout = 50;

        when(configs.getInt("scoopi.task.takeTimeout", "500"))
                .thenReturn(taskTakeTimeout);

        when(stateFliper.isTMState(TMState.SHUTDOWN)).thenReturn(false)
                .thenReturn(true); // to break while

        when(payloadStore.getPayloadsCount()).thenReturn(grape);
        when(payloadStore.takePayload(takeTimeout)).thenReturn(payload);
        when(taskFactory.createTask(payload)).thenReturn(task);
        when(task.getStep()).thenReturn(iStep);
        when(iStep.getStepName()).thenReturn(poolName);
        taskRunner.run();

        verify(poolService).waitForFinish();
        verify(stateFliper).acquireTaskQueueToPoolLock(aquireLockTimeout,
                TimeUnit.MILLISECONDS);
        verify(poolService).submit(poolName, task);
        verify(stateFliper).releaseTaskQueueToPoolLock();
        verify(errors, never()).inc();
    }

    @Test
    public void testRunPayloadIsNull() throws Exception {
        int taskTakeTimeout = 1;
        int takeTimeout = 0;
        int grape = 1;

        Payload payload = Mockito.mock(Payload.class);

        Task task = Mockito.mock(Task.class);
        IStep iStep = Mockito.mock(IStep.class);
        String poolName = "Foo";
        int aquireLockTimeout = 50;

        when(configs.getInt("scoopi.task.takeTimeout", "500"))
                .thenReturn(taskTakeTimeout);

        when(stateFliper.isTMState(TMState.SHUTDOWN)).thenReturn(false)
                .thenReturn(false).thenReturn(true); // to break while

        when(payloadStore.getPayloadsCount()).thenReturn(grape);

        // return null to increment the retryCount
        when(payloadStore.takePayload(takeTimeout)).thenReturn(null)
                .thenReturn(payload);

        when(taskFactory.createTask(payload)).thenReturn(task);
        when(task.getStep()).thenReturn(iStep);
        when(iStep.getStepName()).thenReturn(poolName);
        taskRunner.run();

        verify(poolService).waitForFinish();
        verify(stateFliper, times(2)).acquireTaskQueueToPoolLock(
                aquireLockTimeout, TimeUnit.MILLISECONDS);
        verify(poolService, times(1)).submit(poolName, task);
        verify(stateFliper, times(2)).releaseTaskQueueToPoolLock();
        verify(errors, never()).inc();
    }

    @Test
    public void testRunException() throws Exception {
        int taskTakeTimeout = 1;
        int takeTimeout = 0;
        int grape = 1;
        Payload payload = Mockito.mock(Payload.class);
        Task task = Mockito.mock(Task.class);
        String poolName = "Foo";
        int aquireLockTimeout = 50;

        when(configs.getInt("scoopi.task.takeTimeout", "500"))
                .thenReturn(taskTakeTimeout);

        when(stateFliper.isTMState(TMState.SHUTDOWN)).thenReturn(false)
                .thenReturn(true); // to break while

        when(payloadStore.getPayloadsCount()).thenReturn(grape);
        when(payloadStore.takePayload(takeTimeout)).thenReturn(payload);

        when(taskFactory.createTask(payload))
                .thenThrow(ClassNotFoundException.class);

        taskRunner.run();

        assertFalse(Thread.currentThread().isInterrupted());

        verify(poolService).waitForFinish();
        verify(stateFliper).acquireTaskQueueToPoolLock(aquireLockTimeout,
                TimeUnit.MILLISECONDS);
        verify(poolService, never()).submit(poolName, task);
        verify(stateFliper).releaseTaskQueueToPoolLock();
        verify(errors).inc();
    }

    @Test
    public void testRunInterruptedException() throws Exception {
        int taskTakeTimeout = 1;
        int takeTimeout = 0;
        int grape = 1;

        Task task = Mockito.mock(Task.class);
        String poolName = "Foo";
        int aquireLockTimeout = 50;

        when(configs.getInt("scoopi.task.takeTimeout", "500"))
                .thenReturn(taskTakeTimeout);

        when(stateFliper.isTMState(TMState.SHUTDOWN)).thenReturn(false)
                .thenReturn(true); // to break while

        when(payloadStore.getPayloadsCount()).thenReturn(grape);
        when(payloadStore.takePayload(takeTimeout))
                .thenThrow(InterruptedException.class);

        taskRunner.run();

        assertTrue(Thread.currentThread().isInterrupted());

        verify(poolService).waitForFinish();
        verify(stateFliper).acquireTaskQueueToPoolLock(aquireLockTimeout,
                TimeUnit.MILLISECONDS);
        verify(poolService, never()).submit(poolName, task);
        verify(stateFliper).releaseTaskQueueToPoolLock();
        verify(errors).inc();
    }

}
