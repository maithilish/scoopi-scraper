package org.codetab.scoopi.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class StepInfoTest {

    private StepInfo stepInfo;

    @Before
    public void setUp() throws Exception {
        stepInfo = new StepInfo("stepName", "priviousStepName", "nextStepName",
                "className");
    }

    @Test
    public void testGetStepName() {
        assertThat(stepInfo.getStepName()).isEqualTo("stepName");
    }

    @Test
    public void testGetNextStepName() {
        assertThat(stepInfo.getNextStepName()).isEqualTo("nextStepName");
    }

    @Test
    public void testGetPriviousStepName() {
        assertThat(stepInfo.getPriviousStepName())
                .isEqualTo("priviousStepName");
    }

    @Test
    public void testGetClassName() {
        assertThat(stepInfo.getClassName()).isEqualTo("className");
    }

    @Test
    public void testToString() {
        StringBuilder expected = new StringBuilder().append(
                "StepInfo [stepName=stepName, priviousStepName=priviousStepName, ")
                .append("nextStepName=nextStepName]");
        assertThat(stepInfo.toString()).isEqualTo(expected.toString());
    }

    @Test
    public void testHashCode() {
        int actual = stepInfo.hashCode();
        assertThat(actual).isEqualTo(2003484491);
    }

    @Test
    public void testEqual() {
        StepInfo another = new StepInfo("stepName", "priviousStepName",
                "nextStepName", "className");
        assertThat(stepInfo).isEqualTo(another);
    }
}
