package org.codetab.scoopi.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class PayloadTest {

    private Payload payload;
    private JobInfo jobInfo;
    private StepInfo stepInfo;
    private String data;

    @Before
    public void setUp() throws Exception {
        jobInfo = new JobInfo("locator", "group", "task", "steps", "dataDef");
        jobInfo.setId(1);
        stepInfo = new StepInfo("stepName", "priviousStepName", "nextStepName",
                "className");
        data = "data";
        payload = new Payload(jobInfo, stepInfo, data);
    }

    @Test
    public void testGetJobInfo() {
        assertThat(payload.getJobInfo()).isEqualTo(jobInfo);
    }

    @Test
    public void testGetStepInfo() {
        assertThat(payload.getStepInfo()).isEqualTo(stepInfo);
    }

    @Test
    public void testGetData() {
        assertThat(payload.getData()).isEqualTo(data);
    }

    @Test
    public void testToString() {
        final StringBuilder expected = new StringBuilder().append(
                "Payload [jobInfo=JobInfo [id=1, locator=locator, group=group, ")
                .append("task=task, steps=steps, dataDef=dataDef], stepInfo=StepInfo [stepName=stepName, ")
                .append("priviousStepName=priviousStepName, nextStepName=nextStepName]]");
        assertThat(payload.toString()).isEqualTo(expected.toString());
    }

    @Test
    public void testHashCode() {
        final int actual = payload.hashCode();
        assertThat(actual).isEqualTo(1753122452);
    }

    @Test
    public void testEqual() {
        final JobInfo aj =
                new JobInfo("locator", "group", "task", "steps", "dataDef");
        aj.setId(1);
        final StepInfo as = new StepInfo("stepName", "priviousStepName",
                "nextStepName", "className");
        final Object ad = "data";
        final Payload another = new Payload(aj, as, ad);
        assertThat(payload).isEqualTo(another);
    }
}
