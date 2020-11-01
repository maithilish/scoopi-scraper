package org.codetab.scoopi.store.solo.simple;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.store.IJobStore;
import org.codetab.scoopi.store.IShutdown;

@Singleton
public class SoloShutdown implements IShutdown {

    @Inject
    private IJobStore jobStore;

    private AtomicBoolean done = new AtomicBoolean(false);

    private AtomicBoolean cancelled = new AtomicBoolean(false);

    @Override
    public void init() {
    }

    @Override
    public void setDone() {
        done.set(true);
    }

    @Override
    public void setTerminate() {
    }

    @Override
    public void terminate() {
    }

    @Override
    public void cancel() {
        cancelled.set(true);
    }

    @Override
    public boolean isCancelled() {
        return cancelled.get();
    }

    @Override
    public boolean allNodesDone() {
        return true;
    }

    @Override
    public boolean jobStoreDone() {
        return jobStore.isDone();
    }
}
