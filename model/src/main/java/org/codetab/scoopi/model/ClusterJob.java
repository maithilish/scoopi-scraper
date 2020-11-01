package org.codetab.scoopi.model;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ClusterJob implements Serializable {

    private static final long serialVersionUID = 1416824813601110840L;

    private final long jobId;
    private boolean taken;
    private String memberId;

    ClusterJob(final long jobId) {
        this.jobId = jobId;
        taken = false;
    }

    public boolean isTaken() {
        return taken;
    }

    public void setTaken(final boolean taken) {
        this.taken = taken;
    }

    public long getJobId() {
        return jobId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(final String memberId) {
        this.memberId = memberId;
    }

    @Override
    public boolean equals(final Object obj) {
        ClusterJob rhs = (ClusterJob) obj;
        return new EqualsBuilder().append(this.jobId, rhs.jobId).build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobId).build();
    }
}
