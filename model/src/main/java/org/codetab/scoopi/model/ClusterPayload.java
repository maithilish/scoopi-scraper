package org.codetab.scoopi.model;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ClusterPayload implements Serializable {

    private static final long serialVersionUID = 1416824813601110840L;

    private final Payload payload;
    private final long jobId;
    private boolean taken;
    private String memberId;

    ClusterPayload(final Payload payload, final long jobId) {
        this.payload = payload;
        this.jobId = jobId;
        taken = false;
    }

    public boolean isTaken() {
        return taken;
    }

    public void setTaken(final boolean taken) {
        this.taken = taken;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(final String memberId) {
        this.memberId = memberId;
    }

    public Payload getPayload() {
        return payload;
    }

    public long getJobId() {
        return jobId;
    }

    @Override
    public boolean equals(final Object obj) {
        String[] excludes = {"payload", "taken", "memberId"};
        return EqualsBuilder.reflectionEquals(this, obj, excludes);
    }

    @Override
    public int hashCode() {
        String[] excludes = {"payload", "taken", "memberId"};
        return HashCodeBuilder.reflectionHashCode(this, excludes);
    }
}
