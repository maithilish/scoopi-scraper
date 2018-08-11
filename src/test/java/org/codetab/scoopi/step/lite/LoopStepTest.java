package org.codetab.scoopi.step.lite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.codetab.scoopi.defs.yml.TaskProvider;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
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

public class LoopStepTest {

    @Mock
    private TaskMediator taskMediator;
    @Mock
    private TaskProvider taskProvider;
    @Mock
    private ModelFactory factory;

    @InjectMocks
    private LoopStep step;

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

        String group = "lite1";
        String stepName = "step1";
        String taskName = "simpleTask";

        StepInfo nextStep =
                modelFactory.createStepInfo("step1", "step0", "step2", "clz");
        JobInfo jobInfo = modelFactory.createJobInfo(0, "acme", "group1",
                "task1", "dataDef1");
        Payload nextStepPayload =
                modelFactory.createPayload(jobInfo, nextStep, "data");

        given(taskProvider.getNextStep(group, taskName, stepName))
                .willReturn(nextStep);
        given(factory.createJobInfo(0, "acme", group, taskName,
                step.getPayload().getJobInfo().getDataDef()))
                        .willReturn(jobInfo);
        given(factory.createPayload(jobInfo, nextStep, step.getData()))
                .willReturn(nextStepPayload);

        boolean actual = step.handover();

        assertThat(actual).isTrue();

        verify(taskMediator).pushPayload(nextStepPayload);
    }

    @Test
    public void testHandoverNextStepIsEnd()
            throws DefNotFoundException, InterruptedException {
        step.process();

        StepInfo stepInfo =
                modelFactory.createStepInfo("s1", "s2", "end", "clz");
        JobInfo jobInfo = modelFactory.createJobInfo(0, "acme", "group1",
                "task1", "dataDef1");
        Payload payload = modelFactory.createPayload(jobInfo, stepInfo, "data");
        step.setPayload(payload);

        boolean actual = step.handover();

        assertThat(actual).isTrue();

        verifyZeroInteractions(taskMediator, factory, taskProvider);
    }

    @Test
    public void testHandoverShouldThrowException()
            throws DefNotFoundException, InterruptedException {
        step.process();

        String group = "lite1";
        String stepName = "step1";
        String taskName = "simpleTask";

        given(taskProvider.getNextStep(group, taskName, stepName))
                .willThrow(DefNotFoundException.class);

        testRule.expect(StepRunException.class);
        step.handover();
    }
}
