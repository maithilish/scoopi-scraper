package org.codetab.scoopi.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.codetab.scoopi.defs.ITaskProvider;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.shared.ConfigService;
import org.codetab.scoopi.shared.StatService;
import org.codetab.scoopi.shared.StepService;
import org.codetab.scoopi.step.extract.URLLoader;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Marker;

public class StepTest {

    @Mock
    protected ConfigService configService;
    @Mock
    protected StepService stepService;
    @Mock
    protected StatService activityService;
    @Mock
    protected MetricsHelper metricsHelper;
    @Mock
    protected ITaskProvider taskProvider;
    @Mock
    protected TaskMediator taskMediator;
    @Mock
    private ObjectFactory factory;

    @InjectMocks
    private URLLoader step;

    private Payload payload;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        payload = getTestPayload();
        step.setPayload(payload);
    }

    @Test
    public void testHandover() throws DefNotFoundException,
            InterruptedException, IllegalAccessException {
        String group = step.getJobInfo().getGroup();
        String stepName = step.getStepInfo().getStepName();
        String taskName = step.getJobInfo().getTask();
        String data = "test data";

        ObjectFactory objectFactory = new ObjectFactory();
        StepInfo nextStep =
                objectFactory.createStepInfo("s2", "s1", "s3", "class2");
        Payload nextStepPayload =
                objectFactory.createPayload(step.getJobInfo(), nextStep, data);
        step.setData(data);
        step.setConsistent(true);

        given(taskProvider.getNextStep(group, taskName, stepName))
                .willReturn(nextStep);
        given(factory.createPayload(step.getJobInfo(), nextStep, data))
                .willReturn(nextStepPayload);

        boolean actual = step.handover();

        assertThat(actual).isTrue();
        verify(taskMediator).pushPayload(nextStepPayload);
    }

    @Test
    public void testHandoverNextStepIsEnd() throws DefNotFoundException,
            InterruptedException, IllegalAccessException {
        ObjectFactory objectFactory = new ObjectFactory();
        // next step is end - case ignored
        StepInfo stepInfo =
                objectFactory.createStepInfo("s2", "s1", "ENd", "class2");
        payload = objectFactory.createPayload(step.getJobInfo(), stepInfo,
                step.getData());
        String data = "test data";
        step.setPayload(payload);
        step.setConsistent(true);
        step.setData(data);

        boolean actual = step.handover();

        assertThat(actual).isTrue();

        verifyZeroInteractions(taskMediator, factory, taskProvider);
    }

    @Test
    public void testHandoverShouldThrowException() throws DefNotFoundException,
            InterruptedException, IllegalAccessException {
        String group = step.getJobInfo().getGroup();
        String stepName = step.getStepInfo().getStepName();
        String taskName = step.getJobInfo().getTask();
        String data = "test data";

        step.setData(data);
        step.setConsistent(true);

        given(taskProvider.getNextStep(group, taskName, stepName))
                .willThrow(DefNotFoundException.class);

        testRule.expect(StepRunException.class);
        step.handover();
    }

    @Test
    public void testHandoverDataIllegalState() throws IllegalAccessException {
        step.setData(null);

        testRule.expect(IllegalStateException.class);
        step.handover();
    }

    @Test
    public void testHandoverConsitentIllegalState()
            throws IllegalAccessException {
        step.setData("test data");
        step.setConsistent(false);

        testRule.expect(IllegalStateException.class);
        step.handover();
    }

    @Test
    public void testGetData() {
        String data = "data";
        step.setData(data);

        assertThat(step.getData()).isEqualTo(data);
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
        step.setConsistent(true);
        assertThat(step.isConsistent()).isFalse();
        step.setConsistent(false);
        assertThat(step.isConsistent()).isFalse();

        String data = "test data";
        step.setData(data);
        step.setConsistent(true);
        assertThat(step.isConsistent()).isTrue();
    }

    @Test
    public void testGetMarker() {
        Marker marker = step.getMarker();

        assertThat(marker.toString()).isEqualTo("LOG_LOCATOR1_GROUP1_TASK1");

        marker = step.getMarker(); // existing marker - coverage
        assertThat(marker.toString()).isEqualTo("LOG_LOCATOR1_GROUP1_TASK1");
    }

    @Test
    public void testGetLabel() {
        String label = step.getLabel();

        assertThat(label).isEqualTo("[locator1:group1:dataDef1]");
    }

    @Test
    public void testGetLabeled() {
        String labeled = step.getLabeled("test message");

        assertThat(labeled)
                .isEqualTo("[locator1:group1:dataDef1] test message");
    }

    private Payload getTestPayload() {
        ObjectFactory objectFactory = new ObjectFactory();
        JobInfo jobInfo = objectFactory.createJobInfo(0, "locator1", "group1",
                "task1", "dataDef1");
        StepInfo stepInfo =
                objectFactory.createStepInfo("s1", "s0", "s2", "clzName1");
        String data = "data";
        return objectFactory.createPayload(jobInfo, stepInfo, data);
    }

}
