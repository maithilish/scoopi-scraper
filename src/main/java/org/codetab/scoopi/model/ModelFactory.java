package org.codetab.scoopi.model;

public class ModelFactory {

    public StepInfo createStepInfo(final String stepName,
            final String priviousStepName, final String nextStepName,
            final String className) {
        return new StepInfo(stepName, priviousStepName, nextStepName,
                className);
    }

    public JobInfo createJobInfo(final long id, final String locator,
            final String group, final String task, final String dataDef) {
        return new JobInfo(id, locator, group, task, dataDef);
    }

    public Payload createPayload(final JobInfo jobInfo, final StepInfo stepInfo,
            final Object data) {
        return new Payload(jobInfo, stepInfo, data);
    }
}
