package org.codetab.scoopi.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class PayloadTest {

    private Payload payload;

    @Before
    public void setUp() throws Exception {
        payload = new Payload();
    }

    @Test
    public void testGetJobInfo() {
        JobInfo jobInfo = new JobInfo(1, "locator", "group", "task", "dataDef");
        payload.setJobInfo(jobInfo);

        assertThat(payload.getJobInfo()).isEqualTo(jobInfo);
    }

    @Test
    public void testGetStepInfo() {
        StepInfo stepInfo = new StepInfo("stepName", "priviousStepName",
                "nextStepName", "className");
        payload.setStepInfo(stepInfo);

        assertThat(payload.getStepInfo()).isEqualTo(stepInfo);
    }

    @Test
    public void testGetData() {
        String data = "data";
        payload.setData(data);
        assertThat(payload.getData()).isEqualTo(data);
    }

    @Test
    public void testToString() {
        JobInfo jobInfo = new JobInfo(1, "locator", "group", "task", "dataDef");
        payload.setJobInfo(jobInfo);
        StepInfo stepInfo = new StepInfo("stepName", "priviousStepName",
                "nextStepName", "className");
        payload.setStepInfo(stepInfo);
        String data = "data";
        payload.setData(data);

        StringBuilder expected = new StringBuilder().append(
                "Payload [jobInfo=JobInfo [id=1, locator=locator, group=group, ")
                .append("task=task, dataDef=dataDef], stepInfo=StepInfo [stepName=stepName, ")
                .append("priviousStepName=priviousStepName, nextStepName=nextStepName]]");
        assertThat(payload.toString()).isEqualTo(expected.toString());
    }

}
