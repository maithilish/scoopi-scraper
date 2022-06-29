package org.codetab.scoopi.step.base;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class PayloadFactoryTest {
    @InjectMocks
    private PayloadFactory payloadFactory;

    @Mock
    private ITaskDef taskDef;
    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private Errors errors;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateSeedPayloads() {
        List<LocatorGroup> locatorGroups = new ArrayList<>();
        LocatorGroup locatorGroup = Mockito.mock(LocatorGroup.class);
        locatorGroups.add(locatorGroup);

        String stepName = "Foo";
        String seederClzName = "Bar";

        List<Payload> payloads = new ArrayList<>();
        Payload payload = Mockito.mock(Payload.class);
        payloads.add(payload);

        String undefined = "undefined";
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String apple = "Baz";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);

        when(objectFactory.createStepInfo(stepName, undefined, undefined,
                seederClzName)).thenReturn(stepInfo);
        when(locatorGroup.getGroup()).thenReturn(apple);
        when(objectFactory.createJobInfo(undefined, apple, undefined, undefined,
                undefined)).thenReturn(jobInfo);
        when(objectFactory.createPayload(jobInfo, stepInfo, locatorGroup))
                .thenReturn(payload);

        List<Payload> actual = payloadFactory.createSeedPayloads(locatorGroups,
                stepName, seederClzName);

        assertEquals(payloads, actual);
    }

    @Test
    public void testCreatePayloads() throws Exception {
        String taskGroup = "Foo";

        String taskName = "Baz";
        List<String> taskNames = new ArrayList<>();
        taskNames.add(taskName);

        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String jobName = "Bar";
        Object payloadData = Mockito.mock(Object.class);

        List<Payload> payloads = new ArrayList<>();
        Payload nextStepPayload = Mockito.mock(Payload.class);
        payloads.add(nextStepPayload);

        String apple = "Qux";
        String orange = "Quux";
        StepInfo thisStep = stepInfo;
        String mango = "Corge";
        String stepsName = "Grault";
        String dataDefName = "Garply";
        String apricot = "Waldo";
        StepInfo nextStep = Mockito.mock(StepInfo.class);
        JobInfo jobInfo = Mockito.mock(JobInfo.class);

        when(stepInfo.getStepName()).thenReturn(apple).thenReturn(orange);
        when(taskDef.getNextStep(taskGroup, taskName, orange))
                .thenReturn(thisStep);
        when(thisStep.getNextStepName()).thenReturn(mango);
        when(taskDef.getStepsName(taskGroup, taskName)).thenReturn(stepsName);
        when(taskDef.getFieldValue(taskGroup, taskName, "dataDef"))
                .thenReturn(dataDefName);
        when(thisStep.getStepName()).thenReturn(apricot);
        when(taskDef.getNextStep(taskGroup, taskName, apricot))
                .thenReturn(nextStep);
        when(objectFactory.createJobInfo(jobName, taskGroup, taskName,
                stepsName, dataDefName)).thenReturn(jobInfo);
        when(objectFactory.createPayload(jobInfo, nextStep, payloadData))
                .thenReturn(nextStepPayload);

        List<Payload> actual = payloadFactory.createPayloads(taskGroup,
                taskNames, stepInfo, jobName, payloadData);

        assertEquals(payloads, actual);
        verify(errors, never()).inc();
    }

    @Test
    public void testCreatePayloadsStartStep() throws Exception {
        String taskGroup = "Foo";

        String taskName = "Baz";
        List<String> taskNames = new ArrayList<>();
        taskNames.add(taskName);

        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String jobName = "Bar";
        Object payloadData = Mockito.mock(Object.class);

        List<Payload> payloads = new ArrayList<>();
        Payload nextStepPayload = Mockito.mock(Payload.class);
        payloads.add(nextStepPayload);

        String apple = "start";
        String orange = "start";
        StepInfo thisStep = Mockito.mock(StepInfo.class);
        String mango = "Corge";
        String stepsName = "Grault";
        String dataDefName = "Garply";
        String apricot = "Waldo";
        StepInfo nextStep = Mockito.mock(StepInfo.class);
        JobInfo jobInfo = Mockito.mock(JobInfo.class);

        when(stepInfo.getStepName()).thenReturn(apple).thenReturn(orange);
        when(taskDef.getNextStep(taskGroup, taskName, orange))
                .thenReturn(thisStep);
        when(thisStep.getNextStepName()).thenReturn(mango);
        when(taskDef.getStepsName(taskGroup, taskName)).thenReturn(stepsName);
        when(taskDef.getFieldValue(taskGroup, taskName, "dataDef"))
                .thenReturn(dataDefName);
        when(thisStep.getStepName()).thenReturn(apricot);
        when(taskDef.getNextStep(taskGroup, taskName, apricot))
                .thenReturn(nextStep);
        when(objectFactory.createJobInfo(jobName, taskGroup, taskName,
                stepsName, dataDefName)).thenReturn(jobInfo);
        when(objectFactory.createPayload(jobInfo, nextStep, payloadData))
                .thenReturn(nextStepPayload);

        List<Payload> actual = payloadFactory.createPayloads(taskGroup,
                taskNames, stepInfo, jobName, payloadData);

        assertEquals(payloads, actual);
        verify(errors, never()).inc();
    }

    @Test
    public void testCreatePayloadsEndStep() throws Exception {
        String taskGroup = "Foo";

        String taskName = "Baz";
        List<String> taskNames = new ArrayList<>();
        taskNames.add(taskName);

        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String jobName = "Bar";
        Object payloadData = Mockito.mock(Object.class);

        List<Payload> payloads = new ArrayList<>(); // return empty list

        String apple = "start";
        String orange = "start";
        StepInfo thisStep = Mockito.mock(StepInfo.class);
        String mango = "end"; // end step

        when(stepInfo.getStepName()).thenReturn(apple).thenReturn(orange);
        when(taskDef.getNextStep(taskGroup, taskName, orange))
                .thenReturn(thisStep);
        when(thisStep.getNextStepName()).thenReturn(mango);

        List<Payload> actual = payloadFactory.createPayloads(taskGroup,
                taskNames, stepInfo, jobName, payloadData);

        assertEquals(payloads, actual);
        verify(errors, never()).inc();
    }

    @Test
    public void testCreatePayloadsDefNotFoundException() throws Exception {
        String taskGroup = "Foo";

        String taskName = "Baz";
        List<String> taskNames = new ArrayList<>();
        taskNames.add(taskName);

        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String jobName = "Bar";
        Object payloadData = Mockito.mock(Object.class);

        List<Payload> payloads = new ArrayList<>(); // empty list returned

        String apple = "start";
        String orange = "start";
        when(stepInfo.getStepName()).thenReturn(apple).thenReturn(orange);

        when(taskDef.getNextStep(taskGroup, taskName, orange))
                .thenThrow(DefNotFoundException.class);

        List<Payload> actual = payloadFactory.createPayloads(taskGroup,
                taskNames, stepInfo, jobName, payloadData);

        assertEquals(payloads, actual);
        verify(errors, times(1)).inc();
    }

    @Test
    public void testCreatePayloadsStartStepIOException() throws Exception {
        String taskGroup = "Foo";

        String taskName = "Baz";
        List<String> taskNames = new ArrayList<>();
        taskNames.add(taskName);

        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String jobName = "Bar";
        Object payloadData = Mockito.mock(Object.class);

        List<Payload> payloads = new ArrayList<>(); // return empty list

        String apple = "start";
        String orange = "start";
        StepInfo thisStep = Mockito.mock(StepInfo.class);
        String mango = "Corge";
        String stepsName = "Grault";

        when(stepInfo.getStepName()).thenReturn(apple).thenReturn(orange);
        when(taskDef.getNextStep(taskGroup, taskName, orange))
                .thenReturn(thisStep);
        when(thisStep.getNextStepName()).thenReturn(mango);
        when(taskDef.getStepsName(taskGroup, taskName)).thenReturn(stepsName);

        when(taskDef.getFieldValue(taskGroup, taskName, "dataDef"))
                .thenThrow(IOException.class);

        List<Payload> actual = payloadFactory.createPayloads(taskGroup,
                taskNames, stepInfo, jobName, payloadData);

        assertEquals(payloads, actual);
        verify(errors, times(1)).inc();
    }
}
