package org.codetab.scoopi.store.solo.simple;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Singleton;

import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.store.solo.ISoloJobStore;

@Singleton
public class JobStore implements ISoloJobStore {

    // private static final int QUEUE_SIZE = 32768;

    private Queue<Payload> jobs = new ConcurrentLinkedQueue<>();

    private AtomicLong jobIdCounter = new AtomicLong();

    @SuppressWarnings("unused")
    private State state = State.NEW;

    @Override
    public void open() {
    }

    @Override
    public Payload takeJob() throws InterruptedException {
        Payload payload = jobs.poll();
        if (Objects.isNull(payload)) {
            throw new NoSuchElementException("jobs queue is empty");
        }
        return payload;
    }

    @Override
    public boolean putJob(final Payload payload) throws InterruptedException {
        notNull(payload, "payload must not be null");
        jobs.offer(payload);
        return true;
    }

    @Override
    public void setState(final State state) {
        this.state = state;
    }

    @Override
    public boolean markFinished(final long id) {
        // not implemented
        return true;
    }

    @Override
    public boolean isDone() {
        return jobs.size() == 0;
    }

    @Override
    public long getJobIdSeq() {
        return jobIdCounter.getAndIncrement();
    }

    @Override
    public void close() {
    }

    @Override
    public boolean putJobs(final List<Payload> payloads, final long jobId)
            throws InterruptedException {
        for (Payload payload : payloads) {
            putJob(payload);
        }
        markFinished(jobId);
        return true;
    }

    @Override
    public void resetCrashedJobs() {
    }

    @Override
    public boolean resetTakenJob(final long jobId) {
        return true;
    }
}
