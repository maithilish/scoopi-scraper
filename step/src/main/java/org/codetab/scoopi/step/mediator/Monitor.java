package org.codetab.scoopi.step.mediator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.step.pool.TaskPoolService;
import org.codetab.scoopi.store.IPayloadStore;

public class Monitor implements Runnable {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private Configs configs;
    @Inject
    private StateFliper stateFliper;
    @Inject
    private TaskPoolService poolService;
    @Inject
    private IPayloadStore payloadStore;

    private ScheduledExecutorService scheduler;

    public void start() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        final int initialDelay = 10;
        int delay = configs.getInt("scoopi.monitor.timerPeriod", "1000");
        scheduler.scheduleWithFixedDelay(this, initialDelay, delay,
                TimeUnit.MILLISECONDS);
    }

    public void stop() throws InterruptedException {
        final int timeout = 1000;
        scheduler.shutdownNow();
        scheduler.awaitTermination(timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        if (payloadStore.getPayloadsCount() == 0 && poolService.isDone()) {
            stateFliper.setTMState(TMState.DONE);
            if (stateFliper.tryTMShutdown()) {
                LOG.info("task mediator state change {}",
                        stateFliper.getTMState());
            } else {
                LOG.debug("unable to shudown task mediator, state fliped to {}",
                        stateFliper.getTMState());
            }
        }
    }
}
