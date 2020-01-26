package org.codetab.scoopi.store.solo.simple;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.store.IJobStore;
import org.codetab.scoopi.store.IShutdown;
import org.codetab.scoopi.store.cluster.hz.CrashCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class Shutdown implements IShutdown {

    static final Logger LOGGER = LoggerFactory.getLogger(CrashCleaner.class);

    @Inject
    private IJobStore jobStore;

    private AtomicBoolean done = new AtomicBoolean(false);

    @Override
    public void init() {
    }

    @Override
    public void setDone() {
        done.set(true);
    }

    @Override
    public <T> boolean tryShutdown(final Function<T, Boolean> func, final T t) {
        if (!done.get()) {
            return false;
        }
        System.out.println("tryShutdown all done");
        if (jobStore.isDone()) {
            return func.apply(t);
        } else {
            System.out.println("tryShutdown jobStore not done");
            return false;
        }
    }

    @Override
    public void setTerminate() {
    }

    @Override
    public void tryTerminate() {
        System.out.println("try terminate");
    }
}
