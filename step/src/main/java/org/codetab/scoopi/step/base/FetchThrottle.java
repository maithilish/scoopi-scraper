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

    // delay between fetches
    private long fetchDelayNano;

    private long lastFetchNano;

    public void init() {
        fetchDelayNano = TimeUnit.MILLISECONDS
                .toNanos(configs.getInt("scoopi.loader.fetchDelay", "1000"));
        semaphore = new Semaphore(1, true);
    }

    public void acquirePermit() {
        semaphore.acquireUninterruptibly();
        long remainingNano = lastFetchNano + fetchDelayNano - System.nanoTime();
        if (remainingNano > 0) {
            Uninterruptibles.sleepUninterruptibly(remainingNano,
                    TimeUnit.NANOSECONDS);
        }
    }

    public void releasePermit() {
        lastFetchNano = System.nanoTime();
        semaphore.release();
    }
}
