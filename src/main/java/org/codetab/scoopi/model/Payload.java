package org.codetab.scoopi.model;

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
    public String toString() {
        return "Payload [jobInfo=" + jobInfo + ", stepInfo=" + stepInfo + "]";
    }
}
