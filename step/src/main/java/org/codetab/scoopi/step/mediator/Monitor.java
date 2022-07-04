package org.codetab.scoopi.step.mediator;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.config.Configs;

public class Monitor implements Runnable {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private Configs configs;
    @Inject
    private StateFliper stateFliper;
    @Inject
    private Factory factory;

    private ScheduledExecutorService scheduler;

    public void start() {
        scheduler = factory.newSingleThreadScheduledExecutor();
        final int initialDelay = 1;
        int delay = configs.getInt("scoopi.monitor.timerPeriod", "1000");

        scheduler.scheduleWithFixedDelay(this, initialDelay, delay,
                TimeUnit.MILLISECONDS);

        LOG.info("shutdown monitor service started");
    }

    public void stop() throws InterruptedException {
        final int timeout = 1000;
        scheduler.shutdownNow();
        scheduler.awaitTermination(timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        stateFliper.tryTMShutdown();
    }
}
