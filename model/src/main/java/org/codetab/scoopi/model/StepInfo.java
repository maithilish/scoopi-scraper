package org.codetab.scoopi.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class StepInfo {

    private final String stepName;
    private final String priviousStepName;
    private final String nextStepName;
    private final String className;

    StepInfo(final String stepName, final String priviousStepName,
            final String nextStepName, final String className) {
        this.stepName = stepName;
        this.nextStepName = nextStepName;
        this.priviousStepName = priviousStepName;
        this.className = className;
    }

    public String getStepName() {
        return stepName;
    }

    public String getNextStepName() {
        return nextStepName;
    }

    public String getPriviousStepName() {
        return priviousStepName;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return "StepInfo [stepName=" + stepName + ", priviousStepName="
                + priviousStepName + ", nextStepName=" + nextStepName + "]";
    }
}
