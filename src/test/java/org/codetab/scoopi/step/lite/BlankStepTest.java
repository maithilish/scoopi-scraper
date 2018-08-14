package org.codetab.scoopi.step.lite;

import static org.assertj.core.api.Assertions.assertThat;

import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.junit.Before;
import org.junit.Test;

public class BlankStepTest {

    private BlankStep step;
    private ObjectFactory objectFactory;

    @Before
    public void setUp() throws Exception {
        step = new BlankStep();
        objectFactory = new ObjectFactory();
        StepInfo stepInfo =
                objectFactory.createStepInfo("s1", "s2", "s3", "clz");
        JobInfo jobInfo = objectFactory.createJobInfo(0, "acme", "group1",
                "task1", "dataDef1");
        Payload payload =
                objectFactory.createPayload(jobInfo, stepInfo, "data");
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

}
