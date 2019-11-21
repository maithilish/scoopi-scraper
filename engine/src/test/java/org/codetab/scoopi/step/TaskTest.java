package org.codetab.scoopi.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.codetab.scoopi.exception.StepPersistenceException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.log.Log.CAT;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.TaskInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Marker;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;

public class TaskTest {

    @Mock
    private Step step;
    @Mock
    private MetricsHelper metricsHelper;
    @Mock
    private ErrorLogger errorLogger;
    @Mock
    private TaskInfo taskInfo;

    @InjectMocks
    private Task task;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRun() {
        Counter counter = new Counter();
        Timer timer = Mockito.mock(Timer.class);
        Context context = Mockito.mock(Context.class);
        JobInfo jobInfo = Mockito.mock(JobInfo.class);

        given(metricsHelper.getTimer(step, "task", "time")).willReturn(timer);
        given(timer.time()).willReturn(context);
        given(metricsHelper.getCounter(task, "system", "error"))
                .willReturn(counter);
        given(step.getJobInfo()).willReturn(jobInfo);

        task.run();

        InOrder inOrder = inOrder(step, context, taskInfo);
        inOrder.verify(step).getMarker();
        inOrder.verify(step).getLabel();
        inOrder.verify(step).getJobInfo();
        inOrder.verify(taskInfo).setJobInfo(jobInfo);
        inOrder.verify(step).setup();
        inOrder.verify(step).initialize();
        inOrder.verify(step).load();
        inOrder.verify(step).process();
        inOrder.verify(step).store();
        inOrder.verify(step).handover();
        inOrder.verify(context).stop();

        verifyNoMoreInteractions(step, context, taskInfo);
    }

    @Test
    public void testRunThrowsException() {

        Timer timer = Mockito.mock(Timer.class);
        Context context = Mockito.mock(Context.class);
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        Marker marker = Mockito.mock(Marker.class);

        given(step.getMarker()).willReturn(marker);
        given(metricsHelper.getTimer(step, "task", "time")).willReturn(timer);
        given(timer.time()).willReturn(context);
        given(step.getJobInfo()).willReturn(jobInfo);

        StepRunException stepRunException = new StepRunException("x");
        StepPersistenceException stepPersistenceException =
                new StepPersistenceException("x");
        IllegalStateException illegalStateException =
                new IllegalStateException("x");

        given(step.getLabeled(stepRunException.getMessage())).willReturn("");
        given(step.getLabeled(stepPersistenceException.getMessage()))
                .willReturn("");
        given(step.getLabeled(illegalStateException.getMessage()))
                .willReturn("");

        given(step.initialize()).willThrow(stepRunException)
                .willThrow(stepPersistenceException)
                .willThrow(illegalStateException);

        task.run();
        verify(errorLogger).log(eq(marker), eq(CAT.ERROR), any(String.class),
                any(StepRunException.class));

        task.run();
        verify(errorLogger).log(eq(marker), eq(CAT.ERROR), any(String.class),
                any(StepPersistenceException.class));

        task.run();
        verify(errorLogger).log(eq(marker), eq(CAT.INTERNAL), any(String.class),
                any(IllegalStateException.class));
    }

    @Test
    public void testGetStep() {
        task.setStep(step);
        assertThat(task.getStep()).isSameAs(step);
    }
}
