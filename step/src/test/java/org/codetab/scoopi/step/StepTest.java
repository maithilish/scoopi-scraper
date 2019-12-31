package org.codetab.scoopi.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.step.process.DataFilter;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Marker;

public class StepTest {

    @Mock
    protected Configs configs;
    @Mock
    protected TaskFactory taskFactory;
    @Mock
    protected MetricsHelper metricsHelper;
    @Mock
    protected ITaskDef taskDef;
    @Mock
    protected TaskMediator taskMediator;
    @Mock
    private ObjectFactory objectFactory;

    @InjectMocks
    private DataFilter step;

    private Payload payload;

    private static ObjectFactory factory;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass() {
        factory = new ObjectFactory();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        payload = getTestPayload();
        step.setPayload(payload);
    }

    @Test
    public void testHandover() throws DefNotFoundException,
            InterruptedException, IllegalAccessException {
        final String group = step.getJobInfo().getGroup();
        final String stepName = step.getStepInfo().getStepName();
        final String taskName = step.getJobInfo().getTask();
        final String output = "test output";

        final StepInfo nextStep =
                factory.createStepInfo("s2", "s1", "s3", "class2");
        final Payload nextStepPayload =
                factory.createPayload(step.getJobInfo(), nextStep, output);
        step.setOutput(output);
        step.setConsistent(true);

        given(taskDef.getNextStep(group, taskName, stepName))
                .willReturn(nextStep);
        given(objectFactory.createPayload(step.getJobInfo(), nextStep, output))
                .willReturn(nextStepPayload);

        final boolean actual = step.handover();

        assertThat(actual).isTrue();
        verify(taskMediator).pushPayload(nextStepPayload);
    }

    @Test
    public void testHandoverNextStepIsEnd() throws DefNotFoundException,
            InterruptedException, IllegalAccessException {
        // next step is end - case ignored
        final StepInfo stepInfo =
                factory.createStepInfo("s2", "s1", "ENd", "class2");
        payload = factory.createPayload(step.getJobInfo(), stepInfo,
                step.getOutput());
        final String output = "test output";
        step.setPayload(payload);
        step.setConsistent(true);
        step.setOutput(output);

        final boolean actual = step.handover();

        assertThat(actual).isTrue();

        verifyNoInteractions(taskMediator, objectFactory, taskDef);
    }

    @Test
    public void testHandoverShouldThrowException() throws DefNotFoundException,
            InterruptedException, IllegalAccessException {
        final String group = step.getJobInfo().getGroup();
        final String stepName = step.getStepInfo().getStepName();
        final String taskName = step.getJobInfo().getTask();
        final String output = "test output";

        step.setOutput(output);
        step.setConsistent(true);

        given(taskDef.getNextStep(group, taskName, stepName))
                .willThrow(DefNotFoundException.class);

        testRule.expect(StepRunException.class);
        step.handover();
    }

    @Test
    public void testHandoverDataIllegalState() throws IllegalAccessException {
        step.setOutput(null);

        testRule.expect(IllegalStateException.class);
        step.handover();
    }

    @Test
    public void testHandoverConsitentIllegalState()
            throws IllegalAccessException {
        step.setOutput("test data");
        step.setConsistent(false);

        testRule.expect(IllegalStateException.class);
        step.handover();
    }

    @Test
    public void testGetData() {
        final String data = "data";
        step.setOutput(data);

        assertThat(step.getOutput()).isEqualTo(data);
    }

    @Test
    public void testGetPayload() {
        assertThat(step.getPayload()).isEqualTo(payload);
    }

    @Test
    public void testGetJobInfo() {
        assertThat(step.getJobInfo()).isEqualTo(payload.getJobInfo());
    }

    @Test
    public void testGetStepInfo() {
        assertThat(step.getStepInfo()).isEqualTo(payload.getStepInfo());
    }

    @Test
    public void testGetStepName() {
        assertThat(step.getStepName())
                .isEqualTo(payload.getStepInfo().getStepName());
    }

    @Test
    public void testIsConsistent() throws IllegalAccessException {
        step.setOutput(null);
        step.setConsistent(true);
        assertThat(step.isConsistent()).isFalse();
        step.setConsistent(false);
        assertThat(step.isConsistent()).isFalse();

        final String data = "test data";
        step.setOutput(data);

        step.setConsistent(false);
        assertThat(step.isConsistent()).isFalse();
        step.setConsistent(true);
        assertThat(step.isConsistent()).isTrue();
    }

    @Test
    public void testGetMarker() {
        final JobInfo jobInfo =
                factory.createJobInfo("acme", "bs", "bsTask", "steps", "bs");
        final StepInfo stepInfo =
                factory.createStepInfo("s1", "s0", "s2", "cls");
        final Payload payload1 =
                factory.createPayload(jobInfo, stepInfo, "data");
        step.setPayload(payload1);

        Marker marker = step.getMarker();
        assertThat(marker).isNull();

        step.setup();
        marker = step.getMarker();

        assertThat(marker.toString()).isEqualTo("task-acme-bs-bsTask");
    }

    @Test
    public void testGetLabel() {
        final String label = step.getLabel();

        assertThat(label).isEqualTo("step: s1, job: [locator1:task1:dataDef1]");
    }

    @Test
    public void testGetLabeled() {
        final String labeled = step.getLabeled("test message");

        assertThat(labeled).isEqualTo(
                "step: s1, job: [locator1:task1:dataDef1], test message");
    }

    private Payload getTestPayload() {
        final JobInfo jobInfo = factory.createJobInfo("locator1", "group1",
                "task1", "steps1", "dataDef1");
        final StepInfo stepInfo =
                factory.createStepInfo("s1", "s0", "s2", "clzName1");
        final String data = "data";
        return factory.createPayload(jobInfo, stepInfo, data);
    }

}
