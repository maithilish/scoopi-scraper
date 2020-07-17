package org.codetab.scoopi.store.solo.simple;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.store.IJobStore;
import org.codetab.scoopi.store.IShutdown;

@Singleton
public class Shutdown implements IShutdown {

    private static final Logger LOG = LogManager.getLogger();

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
    public <T> boolean tryShutdown(final Function<T, Boolean> func, final T t) {

        if (cancelled.get()) {
            LOG.debug("cancel requrested, ignore pending jobs");
            return func.apply(t);
        }

        if (!done.get()) {
            return false;
        }

        if (jobStore.isDone()) {
            return func.apply(t);
        } else {
            return false;
        }
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
}
