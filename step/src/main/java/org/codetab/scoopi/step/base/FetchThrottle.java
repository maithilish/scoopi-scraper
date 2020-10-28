package org.codetab.scoopi.step.base;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.config.Configs;

import com.google.common.util.concurrent.Uninterruptibles;

@Singleton
public class FetchThrottle {

    @Inject
    private Configs configs;

    private Semaphore semaphore;

    private long fetchDelay;

    public void init() {
        fetchDelay = configs.getInt("scoopi.loader.fetch.delay", "1000");
        int permits = configs.getInt("scoopi.loader.fetch.parallelism", "1");
        semaphore = new Semaphore(permits, true);
    }

    public void acquirePermit() {
        semaphore.acquireUninterruptibly();
        Uninterruptibles.sleepUninterruptibly(fetchDelay,
                TimeUnit.MILLISECONDS);
    }

    public void releasePermit() {
        semaphore.release();
    }
}
