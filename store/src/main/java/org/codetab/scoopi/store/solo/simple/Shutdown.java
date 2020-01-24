package org.codetab.scoopi.store.solo.simple;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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

    AtomicInteger doneCount = new AtomicInteger();
    AtomicInteger tryCount = new AtomicInteger();
    AtomicInteger tryDoneCount = new AtomicInteger();
    AtomicInteger shutdownCount = new AtomicInteger();

    @Override
    public void init() {
    }

    @Override
    public void setDone() {
        done.set(true);
        doneCount.getAndIncrement();
    }

    @Override
    public <T, R> void tryShutdown(final Function<T, R> func, final T t) {
        tryCount.getAndIncrement();
        if (!done.get()) {
            return;
        }
        tryDoneCount.getAndIncrement();
        if (jobStore.isDone()) {

            func.apply(t);
            shutdownCount.getAndIncrement();
            System.out.printf("%d %d %d %d\n", doneCount.get(), tryCount.get(),
                    tryDoneCount.get(), shutdownCount.get());
        }
    }

    @Override
    public void setTerminate() {
    }

    @Override
    public void tryTerminate() {
    }
}
