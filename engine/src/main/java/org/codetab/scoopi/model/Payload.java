package org.codetab.scoopi.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Payload {

    private final JobInfo jobInfo;
    private final StepInfo stepInfo;
    private final Object data;

    Payload(final JobInfo jobInfo, final StepInfo stepInfo, final Object data) {
        this.jobInfo = jobInfo;
        this.stepInfo = stepInfo;
        this.data = data;
    }

    public JobInfo getJobInfo() {
        return jobInfo;
    }

    public StepInfo getStepInfo() {
        return stepInfo;
    }

    public Object getData() {
        return data;
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
        return "Payload [jobInfo=" + jobInfo + ", stepInfo=" + stepInfo + "]";
    }
}
