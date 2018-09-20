package org.codetab.scoopi.step.lite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.codetab.scoopi.defs.yml.TaskDefs;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.ObjectFactory;
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
    private TaskDefs taskDefs;
    @Mock
    private ObjectFactory objectFactory;

    @InjectMocks
    private LoopStep step;

    private ObjectFactory factory;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        factory = new ObjectFactory();
        StepInfo stepInfo = factory.createStepInfo("s1", "s2", "s3", "clz");
        JobInfo jobInfo = factory.createJobInfo(0, "acme", "group1", "task1",
                "steps1", "dataDef1");
        Payload payload = factory.createPayload(jobInfo, stepInfo, "data");
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
        assertThat(step.getOutput()).isEqualTo(step.getPayload().getData());
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
                factory.createStepInfo("step1", "step0", "step2", "clz");
        JobInfo jobInfo = factory.createJobInfo(0, "acme", "group1", "task1",
                "steps1", "dataDef1");
        Payload nextStepPayload =
                factory.createPayload(jobInfo, nextStep, "data");

        given(taskDefs.getNextStep(group, taskName, stepName))
                .willReturn(nextStep);
        given(objectFactory.createJobInfo(0, "acme", group, taskName,
                step.getPayload().getJobInfo().getSteps(),
                step.getPayload().getJobInfo().getDataDef()))
                        .willReturn(jobInfo);
        given(objectFactory.createPayload(jobInfo, nextStep, step.getOutput()))
                .willReturn(nextStepPayload);

        boolean actual = step.handover();

        assertThat(actual).isTrue();

        verify(taskMediator).pushPayload(nextStepPayload);
    }

    @Test
    public void testHandoverNextStepIsEnd()
            throws DefNotFoundException, InterruptedException {
        step.process();

        StepInfo stepInfo = factory.createStepInfo("s1", "s2", "end", "clz");
        JobInfo jobInfo = factory.createJobInfo(0, "acme", "group1", "task1",
                "steps1", "dataDef1");
        Payload payload = factory.createPayload(jobInfo, stepInfo, "data");
        step.setPayload(payload);

        boolean actual = step.handover();

        assertThat(actual).isTrue();

        verifyZeroInteractions(taskMediator, objectFactory, taskDefs);
    }

    @Test
    public void testHandoverShouldThrowException()
            throws DefNotFoundException, InterruptedException {
        step.process();

        String group = "lite1";
        String stepName = "step1";
        String taskName = "simpleTask";

        given(taskDefs.getNextStep(group, taskName, stepName))
                .willThrow(DefNotFoundException.class);

        testRule.expect(StepRunException.class);
        step.handover();
    }
}
