package org.codetab.scoopi.model;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PrintPayload implements Serializable {

    private static final long serialVersionUID = 2892947121542770327L;

    private final JobInfo jobInfo;
    private final Object data;
    private final CountDownLatch finished;
    private final AtomicBoolean processed;

    PrintPayload(final JobInfo jobInfo, final Object data) {
        this.jobInfo = jobInfo;
        this.data = data;
        finished = new CountDownLatch(1);
        processed = new AtomicBoolean(false);
    }

    public JobInfo getJobInfo() {
        return jobInfo;
    }

    public Object getData() {
        return data;
    }

    public void finished() {
        finished.countDown();
    }

    public boolean isFinished() throws InterruptedException {
        finished.await();
        return processed.get();
    }

    public void setProcessed(final boolean value) {
        processed.set(value);
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
        return "PrintPayload [jobInfo=" + jobInfo + "]";
    }
}
