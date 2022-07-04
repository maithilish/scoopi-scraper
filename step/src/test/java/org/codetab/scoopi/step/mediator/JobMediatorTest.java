package org.codetab.scoopi.step.mediator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.codetab.scoopi.exception.JobStateException;
import org.codetab.scoopi.exception.TransactionException;
import org.codetab.scoopi.helper.Snooze;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.store.IJobStore;
import org.codetab.scoopi.store.IShutdown;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class JobMediatorTest {
    @InjectMocks
    private JobMediator jobMediator;

    @Mock
    private JobRunner jobRunner;
    @Mock
    private TaskMediator taskMediator;
    @Mock
    private IJobStore jobStore;
    @Mock
    private IShutdown shutdown;
    @Mock
    private Monitor monitor;
    @Mock
    private Errors errors;
    @Mock
    private Snooze snooze;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInit() {
        jobMediator.init();

        verify(shutdown).init();
        verify(jobStore).open();
    }

    @Test
    public void testStart() {
        jobMediator.start();

        verify(jobRunner).start();
        verify(monitor).start();
    }

    @Test
    public void testCancel() {
        jobMediator.cancel();

        verify(jobRunner).cancel();
    }

    @Test
    public void testWaitForFinish() throws Exception {
        jobMediator.waitForFinish();

        verify(taskMediator).waitForFinish();
        verify(jobRunner).join();
        verify(monitor).stop();
        verify(jobStore).close();
        verify(shutdown).setTerminate();
        verify(errors, never()).inc();
    }

    @Test
    public void testWaitForFinishException() throws Exception {
        doThrow(InterruptedException.class).when(jobRunner).join();

        jobMediator.waitForFinish();

        assertTrue(Thread.currentThread().isInterrupted());

        verify(taskMediator).waitForFinish();
        verify(monitor, never()).stop();
        verify(jobStore, never()).close();
        verify(shutdown, never()).setTerminate();
        verify(errors).inc();
    }

    @Test
    public void testPushJob() throws Exception {
        Payload payload = Mockito.mock(Payload.class);
        jobMediator.pushJob(payload);

        verify(jobStore).putJob(payload);
    }

    @Test
    public void testPushJobs() throws Exception {
        List<Payload> payloads = new ArrayList<>();
        long jobId = 1L;
        boolean grape = true;

        when(jobStore.resetTakenJob(jobId)).thenReturn(grape);
        jobMediator.pushJobs(payloads, jobId);

        verify(jobStore).putJobs(payloads, jobId);
    }

    @Test
    public void testPushJobsException() throws Exception {
        List<Payload> payloads = new ArrayList<>();
        long jobId = 1L;
        JobStateException e = Mockito.mock(JobStateException.class);
        String apple = "Foo";

        when(e.getLocalizedMessage()).thenReturn(apple);

        doThrow(e).when(jobStore).putJobs(payloads, jobId);

        jobMediator.pushJobs(payloads, jobId);

        verify(jobStore, never()).resetTakenJob(jobId);
    }

    @Test
    public void testPushJobsTransactionException() throws Exception {
        List<Payload> payloads = new ArrayList<>();
        long jobId = 1L;
        String apple = "Foo";
        TransactionException e = Mockito.mock(TransactionException.class);
        boolean grape = true;
        int retryWait = 50;

        when(e.getLocalizedMessage()).thenReturn(apple);
        when(jobStore.resetTakenJob(jobId)).thenReturn(grape);

        doThrow(e).when(jobStore).putJobs(payloads, jobId);

        jobMediator.pushJobs(payloads, jobId);

        verify(snooze, never()).sleepUninterruptibly(retryWait);
    }

    @Test
    public void testPushJobsTransactionExceptionWhileBreak() throws Exception {
        List<Payload> payloads = new ArrayList<>();
        long jobId = 1L;
        String apple = "Foo";
        TransactionException e = Mockito.mock(TransactionException.class);
        boolean grape = false; // while loop
        int retryWait = 50;

        when(e.getLocalizedMessage()).thenReturn(apple);
        when(jobStore.resetTakenJob(jobId)).thenReturn(grape);

        doThrow(e).when(jobStore).putJobs(payloads, jobId);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                when(jobStore.resetTakenJob(jobId)).thenReturn(true); // break
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.schedule(runnable, 100, TimeUnit.MILLISECONDS);

        jobMediator.pushJobs(payloads, jobId);

        verify(snooze, atLeastOnce()).sleepUninterruptibly(retryWait);
    }

    @Test
    public void testGetJobIdSequence() {
        long apple = 1L;

        when(jobStore.getJobIdSeq()).thenReturn(apple);

        long actual = jobMediator.getJobIdSequence();

        assertEquals(apple, actual);
    }

    @Test
    public void testMarkJobFinished() throws Exception {
        long jobId = 1L;

        jobMediator.markJobFinished(jobId);

        verify(jobStore).markFinished(jobId);
    }

    @Test
    public void testMarkJobFinishedException() throws Exception {
        long jobId = 1L;
        JobStateException e = Mockito.mock(JobStateException.class);
        String apple = "Foo";

        when(e.getLocalizedMessage()).thenReturn(apple);

        doThrow(e).when(jobStore).markFinished(jobId);

        jobMediator.markJobFinished(jobId);
    }

    @Test
    public void testMarkJobFinishedTransactionException() throws Exception {
        long jobId = 1L;
        String apple = "Foo";
        TransactionException e = Mockito.mock(TransactionException.class);
        boolean grape = true;

        when(e.getLocalizedMessage()).thenReturn(apple);
        when(jobStore.resetTakenJob(jobId)).thenReturn(grape);

        doThrow(e).when(jobStore).markFinished(jobId);

        jobMediator.markJobFinished(jobId);

        verify(jobStore).markFinished(jobId);
    }

    @Test
    public void testMarkJobFinishedTransactionExceptionWhileBreak()
            throws Exception {
        long jobId = 1L;
        String apple = "Foo";
        TransactionException e = Mockito.mock(TransactionException.class);
        boolean grape = false;
        int retryWait = 50;

        when(e.getLocalizedMessage()).thenReturn(apple);
        when(jobStore.resetTakenJob(jobId)).thenReturn(grape);

        doThrow(e).when(jobStore).markFinished(jobId);

        when(jobStore.resetTakenJob(jobId)).thenReturn(grape);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                when(jobStore.resetTakenJob(jobId)).thenReturn(true); // break
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.schedule(runnable, 100, TimeUnit.MILLISECONDS);

        jobMediator.markJobFinished(jobId);

        verify(jobStore).markFinished(jobId);
        verify(snooze, atLeastOnce()).sleepUninterruptibly(retryWait);
    }

    @Test
    public void testResetTakenJob() {
        long jobId = 1L;
        boolean apple = true;
        int retryWait = 50;

        when(jobStore.resetTakenJob(jobId)).thenReturn(apple);

        jobMediator.resetTakenJob(jobId);

        verify(snooze, never()).sleepUninterruptibly(retryWait);
    }

    @Test
    public void testResetTakenJobWhileBreak() {
        long jobId = 1L;
        boolean apple = false;
        int retryWait = 50;

        when(jobStore.resetTakenJob(jobId)).thenReturn(apple);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                when(jobStore.resetTakenJob(jobId)).thenReturn(true); // break
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.schedule(runnable, 100, TimeUnit.MILLISECONDS);

        jobMediator.resetTakenJob(jobId);

        verify(snooze, atLeastOnce()).sleepUninterruptibly(retryWait);
    }
}
