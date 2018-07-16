package org.codetab.scoopi.model;

public class Payload {

    private JobInfo jobInfo;
    private StepInfo stepInfo;
    private Object data;

    public JobInfo getJobInfo() {
        return jobInfo;
    }

    public void setJobInfo(final JobInfo jobInfo) {
        this.jobInfo = jobInfo;
    }

    public StepInfo getStepInfo() {
        return stepInfo;
    }

    public void setStepInfo(final StepInfo stepInfo) {
        this.stepInfo = stepInfo;
    }

    public Object getData() {
        return data;
    }

    public void setData(final Object data) {
        this.data = data;
    }
}
