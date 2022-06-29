package org.codetab.scoopi.step;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.exception.JobRunException;
import org.codetab.scoopi.exception.StepPersistenceException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.TaskInfo;
import org.codetab.scoopi.step.mediator.JobMediator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;

public class TaskTest {
    @InjectMocks
    private Task task;

    @Mock
    private Errors errors;
    @Mock
    private MetricsHelper metricsHelper;
    @Mock
    private TaskInfo taskInfo;
    @Mock
    private JobMediator jobMediator;
    @Mock
    private IStep step;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSetStep() {
        IStep step1 = Mockito.mock(IStep.class);
        task.setStep(step1);

        Object actual = task.getStep();

        assertSame(step1, actual);
    }

    @Test
    public void testGetStep() {

        IStep actual = task.getStep();

        assertSame(step, actual);
    }

    @Test
    public void testRun() {
        Marker jobMarker = Mockito.mock(Marker.class);
        Marker jobAbortedMarker = Mockito.mock(Marker.class);
        String stepLabel = "Foo";
        JobInfo apple = Mockito.mock(JobInfo.class);
        Timer grape = Mockito.mock(Timer.class);
        Context taskTimer = Mockito.mock(Context.class);
        JobInfo orange = Mockito.mock(JobInfo.class);
        long jobId = 1L;

        when(step.getJobMarker()).thenReturn(jobMarker);
        when(step.getJobAbortedMarker()).thenReturn(jobAbortedMarker);
        when(step.getLabel()).thenReturn(stepLabel);
        when(step.getJobInfo()).thenReturn(apple).thenReturn(orange);
        when(metricsHelper.getTimer(step, "task", "time")).thenReturn(grape);
        when(grape.time()).thenReturn(taskTimer);
        task.run();

        verify(taskInfo).setJobInfo(apple);
        verify(step).setup();
        verify(step).initialize();
        verify(step).load();
        verify(step).process();
        verify(step).store();
        verify(step).handover();
        verify(taskTimer).stop();
        verify(jobMediator, never()).resetTakenJob(jobId);
        verify(errors, never()).inc();
    }

    @Test
    public void testRunJobRunException() {
        Marker jobMarker = Mockito.mock(Marker.class);
        Marker jobAbortedMarker = Mockito.mock(Marker.class);
        String stepLabel = "Foo";
        JobInfo apple = Mockito.mock(JobInfo.class);
        Timer grape = Mockito.mock(Timer.class);
        Context taskTimer = Mockito.mock(Context.class);
        JobInfo orange = Mockito.mock(JobInfo.class);
        long jobId = 1L;

        when(step.getJobMarker()).thenReturn(jobMarker);
        when(step.getJobAbortedMarker()).thenReturn(jobAbortedMarker);
        when(step.getLabel()).thenReturn(stepLabel);
        when(step.getJobInfo()).thenReturn(apple).thenReturn(orange);
        when(metricsHelper.getTimer(step, "task", "time")).thenReturn(grape);

        doThrow(JobRunException.class).when(step).setup();
        when(orange.getId()).thenReturn(jobId);

        task.run();

        verify(taskInfo).setJobInfo(apple);
        verify(step).setup();
        verify(step, never()).initialize();
        verify(step, never()).load();
        verify(step, never()).process();
        verify(step, never()).store();
        verify(step, never()).handover();
        verify(taskTimer, never()).stop();
        verify(jobMediator).resetTakenJob(jobId);
        verify(errors, never()).inc();
    }

    @Test
    public void testRunJobRunExceptionException() {
        Marker jobMarker = Mockito.mock(Marker.class);
        Marker jobAbortedMarker = Mockito.mock(Marker.class);
        String stepLabel = "Foo";
        JobInfo apple = Mockito.mock(JobInfo.class);
        Timer grape = Mockito.mock(Timer.class);
        Context taskTimer = Mockito.mock(Context.class);
        JobInfo orange = Mockito.mock(JobInfo.class);
        long jobId = 1L;

        when(step.getJobMarker()).thenReturn(jobMarker);
        when(step.getJobAbortedMarker()).thenReturn(jobAbortedMarker);
        when(step.getLabel()).thenReturn(stepLabel);
        when(step.getJobInfo()).thenReturn(apple).thenReturn(orange);
        when(metricsHelper.getTimer(step, "task", "time")).thenReturn(grape);

        doThrow(JobRunException.class).when(step).setup();
        doThrow(RuntimeException.class).when(jobMediator).resetTakenJob(jobId);

        when(orange.getId()).thenReturn(jobId);

        task.run();

        verify(taskInfo).setJobInfo(apple);
        verify(step, never()).initialize();
        verify(step, never()).load();
        verify(step, never()).process();
        verify(step, never()).store();
        verify(step, never()).handover();
        verify(taskTimer, never()).stop();
        verify(errors, times(1)).inc();
    }

    @Test
    public void testRunJobRunOtherExceptions() {
        Marker jobMarker = Mockito.mock(Marker.class);
        Marker jobAbortedMarker = Mockito.mock(Marker.class);
        String stepLabel = "Foo";
        JobInfo apple = Mockito.mock(JobInfo.class);
        Timer grape = Mockito.mock(Timer.class);
        Context taskTimer = Mockito.mock(Context.class);
        JobInfo orange = Mockito.mock(JobInfo.class);
        long jobId = 1L;

        when(step.getJobMarker()).thenReturn(jobMarker);
        when(step.getJobAbortedMarker()).thenReturn(jobAbortedMarker);
        when(step.getLabel()).thenReturn(stepLabel);
        when(step.getJobInfo()).thenReturn(apple).thenReturn(orange);
        when(metricsHelper.getTimer(step, "task", "time")).thenReturn(grape);

        doThrow(StepRunException.class).doThrow(StepPersistenceException.class)
                .doThrow(RuntimeException.class).when(step).setup();

        when(orange.getId()).thenReturn(jobId);

        task.run();
        verify(errors, times(1)).inc();

        task.run();
        verify(errors, times(2)).inc();

        task.run();
        verify(errors, times(3)).inc();

        verify(taskInfo).setJobInfo(apple);
        verify(step, never()).initialize();
        verify(step, never()).load();
        verify(step, never()).process();
        verify(step, never()).store();
        verify(step, never()).handover();
        verify(taskTimer, never()).stop();
    }
}
