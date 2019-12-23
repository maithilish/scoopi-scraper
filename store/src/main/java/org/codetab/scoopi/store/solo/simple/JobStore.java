package org.codetab.scoopi.store.solo.simple;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.inject.Singleton;

import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.store.solo.ISoloJobStore;

@Singleton
public class JobStore implements ISoloJobStore {

    private static final int QUEUE_SIZE = 32768;

    private BlockingQueue<Payload> jobs = new ArrayBlockingQueue<>(QUEUE_SIZE);

    private State state = State.NEW;

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public boolean createTables() {
        return true;
    }

    @Override
    public Payload takeJob() throws InterruptedException {
        return jobs.take();
    }

    @Override
    public boolean putJob(final Payload payload) throws InterruptedException {
        notNull(payload, "payload must not be null");
        jobs.put(payload);
        return true;
    }

    @Override
    public State getState() {
        return state;
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
    public int getJobCount() {
        return jobs.size();
    }

    @Override
    public boolean isDone() {
        return jobs.size() == 0;
    }

    @Override
    public boolean changeStateToInitialize() {
        if (state.equals(State.NEW)) {
            state = State.INITIALIZE;
            return true;
        } else {
            return false;
        }
    }
}
