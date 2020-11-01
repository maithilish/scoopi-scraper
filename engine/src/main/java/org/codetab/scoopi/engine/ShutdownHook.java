package org.codetab.scoopi.engine;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.status.ScoopiStatus;
import org.codetab.scoopi.step.webdriver.WebDriverPool;

/**
 * <p>
 * JVM shutdown hook.
 * @author Maithilish
 *
 */
@Singleton
public class ShutdownHook extends Thread {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private ScoopiStatus scoopiStatus;
    @Inject
    private WebDriverPool webDriverPool;
    @Inject
    private ScoopiEngine scoopiEngine;

    private boolean normalShutdown = false;

    public ShutdownHook() {
        // cs - if private then class has to be final which is unable to mock
        LOG.info("shutdown hook added");
    }

    @Override
    public synchronized void start() {

        if (normalShutdown) {
            scoopiStatus.outputMemStats();
            scoopiStatus.outputStats(false);
        } else {
            final int[] surrogates = {0xD83D, 0xDC7D};
            String emoji = new String(surrogates, 0, surrogates.length);
            LOG.info("{} {} {} cancel requested", emoji, emoji, emoji);
            try {
                scoopiEngine.cancel();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.error("cancel wait", e);
            }
            scoopiStatus.outputMemStats();
            scoopiStatus.outputStats(true);
        }

        LOG.debug("close webdrivers");
        webDriverPool.close();

        LOG.debug("shutdown log manager");
        LogManager.shutdown();
    }

    public void setNormalShutdown(final boolean normalShutdown) {
        this.normalShutdown = normalShutdown;
    }
}
