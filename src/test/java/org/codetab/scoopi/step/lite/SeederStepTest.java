package org.codetab.scoopi.step.lite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.codetab.scoopi.defs.yml.TaskProvider;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.ModelFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.step.TaskMediator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SeederStepTest {

    @Mock
    private TaskMediator taskMediator;
    @Mock
    private TaskProvider taskProvider;
    @Mock
    private ModelFactory factory;

    @InjectMocks
    private SeederStep step;

    private ModelFactory modelFactory;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        modelFactory = new ModelFactory();
        StepInfo stepInfo =
                modelFactory.createStepInfo("s1", "s2", "s3", "clz");
        JobInfo jobInfo = modelFactory.createJobInfo(0, "acme", "group1",
                "task1", "dataDef1");
        Payload payload = modelFactory.createPayload(jobInfo, stepInfo, "data");
        step.setPayload(payload);
    }

    @Test
    public void testInitialize() {
        assertThat(step.initialize()).isTrue();
    }

    @Test
    public void testLoad() {
        assertThat(step.load()).isTrue();
    }

    @Test
    public void testStore() {
        assertThat(step.store()).isTrue();
    }

    @Test
    public void testProcess() {
        assertThat(step.isConsistent()).isFalse();
        assertThat(step.process()).isTrue();
        assertThat(step.getData()).isEqualTo(step.getPayload().getData());
        assertThat(step.isConsistent()).isTrue();
    }

    @Test
    public void testHandover()
            throws DefNotFoundException, InterruptedException {
        step.process();

        String taskGroup = step.getPayload().getJobInfo().getGroup();
        String stepName = step.getPayload().getStepInfo().getStepName();

        StepInfo nextStep =
                modelFactory.createStepInfo("step1", "step0", "step2", "clz");
        JobInfo jobInfo = modelFactory.createJobInfo(0, "acme", "group1",
                "task1", "dataDef1");
        Payload nextStepPayload =
                modelFactory.createPayload(jobInfo, nextStep, "data");

        List<String> taskNames = Arrays.asList("task1", "task2");
        given(taskProvider.getTaskNames(taskGroup)).willReturn(taskNames);

        given(taskProvider.getFieldValue(taskGroup, "task1", "dataDef"))
                .willReturn("dataDef1");
        given(taskProvider.getFieldValue(taskGroup, "task2", "dataDef"))
                .willReturn("dataDef2");
        given(taskProvider.getNextStep(taskGroup, "task1", stepName))
                .willReturn(nextStep);
        given(taskProvider.getNextStep(taskGroup, "task2", stepName))
                .willReturn(nextStep);
        given(factory.createJobInfo(taskMediator.getJobId(), "locator",
                taskGroup, "task1", "dataDef1")).willReturn(jobInfo);
        given(factory.createJobInfo(taskMediator.getJobId(), "locator",
                taskGroup, "task2", "dataDef2")).willReturn(jobInfo);

        given(factory.createPayload(jobInfo, nextStep, step.getData()))
                .willReturn(nextStepPayload).willReturn(nextStepPayload);

        boolean actual = step.handover();

        assertThat(actual).isTrue();

        verify(taskMediator, times(2)).pushPayload(nextStepPayload);
    }

    @Test
    public void testHandoverThrowException()
            throws DefNotFoundException, InterruptedException {
        step.process();

        String taskGroup = step.getPayload().getJobInfo().getGroup();
        String stepName = step.getPayload().getStepInfo().getStepName();

        StepInfo nextStep =
                modelFactory.createStepInfo("step1", "step0", "step2", "clz");
        JobInfo jobInfo = modelFactory.createJobInfo(0, "acme", "group1",
                "task1", "dataDef1");
        Payload nextStepPayload =
                modelFactory.createPayload(jobInfo, nextStep, "data");

        List<String> taskNames = Arrays.asList("task1", "task2");
        given(taskProvider.getTaskNames(taskGroup)).willReturn(taskNames);

        given(taskProvider.getFieldValue(taskGroup, "task1", "dataDef"))
                .willReturn("dataDef1");
        given(taskProvider.getFieldValue(taskGroup, "task2", "dataDef"))
                .willReturn("dataDef2");
        given(taskProvider.getNextStep(taskGroup, "task1", stepName))
                .willThrow(DefNotFoundException.class);
        given(taskProvider.getNextStep(taskGroup, "task2", stepName))
                .willReturn(nextStep);
        given(factory.createJobInfo(taskMediator.getJobId(), "locator",
                taskGroup, "task1", "dataDef1")).willReturn(jobInfo);
        given(factory.createJobInfo(taskMediator.getJobId(), "locator",
                taskGroup, "task2", "dataDef2")).willReturn(jobInfo);

        given(factory.createPayload(jobInfo, nextStep, step.getData()))
                .willReturn(nextStepPayload).willReturn(nextStepPayload);

        boolean actual = step.handover();

        assertThat(actual).isTrue();

        verify(taskMediator, times(1)).pushPayload(nextStepPayload);
    }
}
