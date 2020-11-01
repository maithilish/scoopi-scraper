package org.codetab.scoopi.engine.module;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.engine.ShutdownHook;

public class ShutdownModule {

    @Inject
    private Runtime runTime;
    @Inject
    private ShutdownHook shutdownHook;
    @Inject
    private Configs configs;

    private CountDownLatch shutdownLatch = new CountDownLatch(1);

    public boolean addShutdownHook() {
        runTime.addShutdownHook(shutdownHook);
        return true;
    }

    public void setNormalShutdown(final boolean normalShutdown) {
        shutdownHook.setNormalShutdown(normalShutdown);
    }

    public void downShutdownLatch() {
        shutdownLatch.countDown();
    }

    public void awaitShutdown() throws InterruptedException {

        int timeout = configs.getInt("scoopi.shutdown.timeout", "10");
        TimeUnit timeUnit = TimeUnit.valueOf(
                configs.getConfig("scoopi.shutdown.timeoutUnit", "SECONDS")
                        .toUpperCase());

        shutdownLatch.await(timeout, timeUnit);
    }
}
