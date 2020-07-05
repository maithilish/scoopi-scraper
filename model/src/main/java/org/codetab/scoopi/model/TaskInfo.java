package org.codetab.scoopi.model;

import javax.inject.Singleton;

import org.apache.logging.log4j.Marker;

@Singleton
public class TaskInfo {

    private ThreadLocal<JobInfo> jobInfo = new ThreadLocal<>();

    public void setJobInfo(final JobInfo jobInfo) {
        this.jobInfo.set(jobInfo);
    }

    public Marker getMarker() {
        return jobInfo.get().getJobMarker();
    }

    public String getLabel() {
        return jobInfo.get().getLabel();
    }

    public String getName() {
        return jobInfo.get().getName();
    }

    public String getGroup() {
        return jobInfo.get().getGroup();
    }
}
