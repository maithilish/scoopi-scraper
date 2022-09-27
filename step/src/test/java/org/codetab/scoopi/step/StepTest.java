package org.codetab.scoopi.step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.step.mediator.JobMediator;
import org.codetab.scoopi.step.mediator.TaskMediator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class StepTest {
    @InjectMocks
    private TestStep step;

    @Mock
    private Configs configs;
    @Mock
    private MetricsHelper metricsHelper;
    @Mock
    private ITaskDef taskDef;
    @Mock
    private TaskMediator taskMediator;
    @Mock
    private JobMediator jobMediator;
    @Mock
    private ObjectFactory factory;
    @Mock
    private Object output;
    @Mock
    private Payload payload;
    @Mock
    private Marker jobMarker;
    @Mock
    private Marker jobAbortedMarker;

    static class TestStep extends Step {

        @Override
        public void initialize() {
        }

        @Override
        public void load() {
        }

        @Override
        public void store() {
        }

        @Override
        public void process() {
        }

    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSetup() throws IllegalAccessException {
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        Marker jobMarker2 = Mockito.mock(Marker.class);
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        Marker jobAbortedMarker2 = Mockito.mock(Marker.class);

        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2);
        when(jobInfo.getJobMarker()).thenReturn(jobMarker2);
        when(jobInfo2.getJobAbortedMarker()).thenReturn(jobAbortedMarker2);

        step.setup();

        assertSame(jobMarker2, FieldUtils.readField(step, "jobMarker", true));
        assertSame(jobAbortedMarker2,
                FieldUtils.readField(step, "jobAbortedMarker", true));

    }

    @Test
    public void testHandoverTryIfGetStepInfoGetNextStepNameEqualsIgnoreCase()
            throws Exception {
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String group = "Foo";
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String stepName = "Bar";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String taskName = "Baz";
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String grape = "End";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        long jobId = 1L;
        JobInfo jobInfo4 = Mockito.mock(JobInfo.class);
        String kiwi = "Quux";
        StepInfo nextStep = Mockito.mock(StepInfo.class);
        JobInfo jobInfo5 = Mockito.mock(JobInfo.class);
        Payload nextStepPayload = Mockito.mock(Payload.class);
        StepInfo stepInfo3 = Mockito.mock(StepInfo.class);
        String banana = "Corge";
        JobInfo jobInfo6 = Mockito.mock(JobInfo.class);
        String cherry = "Grault";

        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo3).thenReturn(jobInfo4).thenReturn(jobInfo5)
                .thenReturn(jobInfo6);
        when(jobInfo.getGroup()).thenReturn(group);
        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2)
                .thenReturn(stepInfo3);
        when(stepInfo.getStepName()).thenReturn(stepName);
        when(jobInfo2.getTask()).thenReturn(taskName);
        when(stepInfo2.getNextStepName()).thenReturn(grape);
        when(jobInfo3.getId()).thenReturn(jobId);
        when(jobInfo4.getLabel()).thenReturn(kiwi);
        when(stepInfo3.getStepName()).thenReturn(banana);
        when(jobInfo6.getLabel()).thenReturn(cherry);

        step.handover();

        verify(jobMediator).markJobFinished(jobId);
        verify(taskDef, never()).getNextStep(group, taskName, stepName);
        verify(factory, never()).createPayload(jobInfo5, nextStep, output);
        verify(taskMediator, never()).pushPayload(nextStepPayload);
        verify(nextStep, never()).getStepName();
    }

    @Test
    public void testHandoverTryElseGetStepInfoGetNextStepNameEqualsIgnoreCase()
            throws Exception {
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String group = "Foo";
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String stepName = "Bar";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String taskName = "Baz";
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String grape = "Qux";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        long jobId = 1L;
        JobInfo jobInfo4 = Mockito.mock(JobInfo.class);
        StepInfo nextStep = Mockito.mock(StepInfo.class);
        JobInfo jobInfo5 = Mockito.mock(JobInfo.class);
        Payload nextStepPayload = Mockito.mock(Payload.class);
        StepInfo stepInfo3 = Mockito.mock(StepInfo.class);
        String banana = "Quux";
        JobInfo jobInfo6 = Mockito.mock(JobInfo.class);
        String cherry = "Corge";
        String peach = "Grault";

        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo5).thenReturn(jobInfo6);
        when(jobInfo.getGroup()).thenReturn(group);
        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2)
                .thenReturn(stepInfo3);
        when(stepInfo.getStepName()).thenReturn(stepName);
        when(jobInfo2.getTask()).thenReturn(taskName);
        when(stepInfo2.getNextStepName()).thenReturn(grape);
        when(taskDef.getNextStep(group, taskName, stepName))
                .thenReturn(nextStep);
        when(factory.createPayload(jobInfo5, nextStep, output))
                .thenReturn(nextStepPayload);
        when(stepInfo3.getStepName()).thenReturn(banana);
        when(jobInfo6.getLabel()).thenReturn(cherry);
        when(nextStep.getStepName()).thenReturn(peach);

        step.handover();

        verify(jobInfo3, never()).getId();
        verify(jobMediator, never()).markJobFinished(jobId);
        verify(jobInfo4, never()).getLabel();
        verify(taskMediator).pushPayload(nextStepPayload);
    }

    @Test
    public void testHandoverTryCatchExceptionIf() throws Exception {
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String group = "Foo";
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String stepName = "Bar";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String taskName = "Baz";
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String grape = "Qux";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        long jobId = 1L;
        JobInfo jobInfo4 = Mockito.mock(JobInfo.class);
        StepInfo nextStep = Mockito.mock(StepInfo.class);
        JobInfo jobInfo5 = Mockito.mock(JobInfo.class);
        Payload nextStepPayload = Mockito.mock(Payload.class);
        StepInfo stepInfo3 = Mockito.mock(StepInfo.class);
        String banana = "Quux";
        JobInfo jobInfo6 = Mockito.mock(JobInfo.class);
        String cherry = "Corge";

        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo3).thenReturn(jobInfo4).thenReturn(jobInfo5)
                .thenReturn(jobInfo6);
        when(jobInfo.getGroup()).thenThrow(IllegalStateException.class);
        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2)
                .thenReturn(stepInfo3);
        when(stepInfo.getStepName()).thenReturn(stepName);
        when(jobInfo2.getTask()).thenReturn(taskName);
        when(stepInfo2.getNextStepName()).thenReturn(grape);
        when(stepInfo3.getStepName()).thenReturn(banana);
        when(jobInfo6.getLabel()).thenReturn(cherry);

        assertThrows(StepRunException.class, () -> step.handover());

        assertFalse(Thread.currentThread().isInterrupted());

        verify(jobInfo3, never()).getId();
        verify(jobMediator, never()).markJobFinished(jobId);
        verify(jobInfo4, never()).getLabel();
        verify(taskDef, never()).getNextStep(group, taskName, stepName);
        verify(factory, never()).createPayload(jobInfo5, nextStep, output);
        verify(taskMediator, never()).pushPayload(nextStepPayload);
        verify(nextStep, never()).getStepName();
    }

    @Test
    public void testHandoverTryElseGetStepInfoGetNextStepNameEqualsIgnoreCaseInterruptedException()
            throws Exception {
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String group = "Foo";
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String stepName = "Bar";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String taskName = "Baz";
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String grape = "Qux";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        long jobId = 1L;
        JobInfo jobInfo4 = Mockito.mock(JobInfo.class);
        StepInfo nextStep = Mockito.mock(StepInfo.class);
        JobInfo jobInfo5 = Mockito.mock(JobInfo.class);
        Payload nextStepPayload = Mockito.mock(Payload.class);
        StepInfo stepInfo3 = Mockito.mock(StepInfo.class);
        String banana = "Quux";
        JobInfo jobInfo6 = Mockito.mock(JobInfo.class);
        String cherry = "Corge";
        String peach = "Grault";

        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo5).thenReturn(jobInfo6);
        when(jobInfo.getGroup()).thenReturn(group);
        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2)
                .thenReturn(stepInfo3);
        when(stepInfo.getStepName()).thenReturn(stepName);
        when(jobInfo2.getTask()).thenReturn(taskName);
        when(stepInfo2.getNextStepName()).thenReturn(grape);
        when(taskDef.getNextStep(group, taskName, stepName))
                .thenReturn(nextStep);
        when(factory.createPayload(jobInfo5, nextStep, output))
                .thenReturn(nextStepPayload);
        when(stepInfo3.getStepName()).thenReturn(banana);
        when(jobInfo6.getLabel()).thenReturn(cherry);
        when(nextStep.getStepName()).thenReturn(peach);

        doThrow(InterruptedException.class).when(taskMediator)
                .pushPayload(nextStepPayload);

        assertThrows(StepRunException.class, () -> step.handover());

        assertTrue(Thread.currentThread().isInterrupted());

        verify(jobInfo3, never()).getId();
        verify(jobMediator, never()).markJobFinished(jobId);
        verify(jobInfo4, never()).getLabel();

    }

    @Test
    public void testGetOutput() {

        Object actual = step.getOutput();

        assertSame(output, actual);
    }

    @Test
    public void testSetOutput() {
        Object output2 = Mockito.mock(Object.class);
        step.setOutput(output2);

        Object actual = step.getOutput();

        assertSame(output2, actual);
    }

    @Test
    public void testGetPayload() {

        Payload actual = step.getPayload();

        assertSame(payload, actual);
    }

    @Test
    public void testSetPayload() {
        Payload payload2 = Mockito.mock(Payload.class);
        step.setPayload(payload2);

        Object actual = step.getPayload();

        assertSame(payload2, actual);
    }

    @Test
    public void testGetJobInfo() {
        JobInfo jobInfo = Mockito.mock(JobInfo.class);

        when(payload.getJobInfo()).thenReturn(jobInfo);

        JobInfo actual = step.getJobInfo();

        assertSame(jobInfo, actual);
    }

    @Test
    public void testGetStepInfo() {
        StepInfo stepInfo = Mockito.mock(StepInfo.class);

        when(payload.getStepInfo()).thenReturn(stepInfo);

        StepInfo actual = step.getStepInfo();

        assertSame(stepInfo, actual);
    }

    @Test
    public void testGetStepName() {
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String apple = "Foo";

        when(payload.getStepInfo()).thenReturn(stepInfo);
        when(stepInfo.getStepName()).thenReturn(apple);

        String actual = step.getStepName();

        assertEquals(apple, actual);
    }

    @Test
    public void testGetJobMarker() {

        Marker actual = step.getJobMarker();

        assertSame(jobMarker, actual);
    }

    @Test
    public void testGetJobAbortedMarker() {

        Marker actual = step.getJobAbortedMarker();

        assertSame(jobAbortedMarker, actual);
    }

    @Test
    public void testGetLabelIfIsNull() {
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String grape = "Foo";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String orange = "Bar";
        String stepLabel = "step: Foo, job: [Bar]";

        when(payload.getStepInfo()).thenReturn(stepInfo);
        when(stepInfo.getStepName()).thenReturn(grape);
        when(payload.getJobInfo()).thenReturn(jobInfo);
        when(jobInfo.getLabel()).thenReturn(orange);

        String actual = step.getLabel();

        assertEquals(stepLabel, actual);
    }

    @Test
    public void testGetLabelElseIsNull() throws Exception {
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String grape = "Foo";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String stepLabel = "xyz";

        FieldUtils.writeField(step, "stepLabel", stepLabel, true);

        when(payload.getStepInfo()).thenReturn(stepInfo);
        when(stepInfo.getStepName()).thenReturn(grape);

        String actual = step.getLabel();

        assertEquals(stepLabel, actual);
        verify(payload, never()).getJobInfo();
        verify(jobInfo, never()).getLabel();
    }

    @Test
    public void testGetLabeled() {
        String message = "Foo";
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String grape = "Bar";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String orange = "Baz";
        String mango = "step: Bar, job: [Baz], Foo";

        when(payload.getStepInfo()).thenReturn(stepInfo);
        when(stepInfo.getStepName()).thenReturn(grape);
        when(payload.getJobInfo()).thenReturn(jobInfo);
        when(jobInfo.getLabel()).thenReturn(orange);

        String actual = step.getLabeled(message);

        assertEquals(mango, actual);
    }
}
