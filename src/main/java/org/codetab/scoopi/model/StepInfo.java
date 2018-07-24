package org.codetab.scoopi.model;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;

public class StepInfo {

    private final String stepName;
    private final String priviousStepName;
    private final String nextStepName;
    private final String className;

    @Inject
    public StepInfo(@Assisted("stepName") final String stepName,
            @Assisted("previousStepName") final String priviousStepName,
            @Assisted("nextStepName") final String nextStepName,
            @Assisted("className") final String className) {
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
    public String toString() {
        return "StepInfo [stepName=" + stepName + ", priviousStepName="
                + priviousStepName + ", nextStepName=" + nextStepName + "]";
    }
}
