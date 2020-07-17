package org.codetab.scoopi.engine;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.stat.Stats;
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
    private Stats stats;
    @Inject
    private WebDriverPool webDriverPool;
    @Inject
    private ScoopiEngine scoopiEngine;

    private boolean cleanShutdown = false;

    @Inject
    public ShutdownHook() {
        // cs - if private then class has to be final which is unable to mock
        LOG.info("shutdown hook created");
    }

    /**
     * log activities and memory stats.
     */
    @Override
    public synchronized void start() {

        if (cleanShutdown) {
            stats.outputStats();
        } else {
            LOG.info("cancel requested");
            try {
                scoopiEngine.cancel();
                scoopiEngine.waitForShutdown();
            } catch (Exception e) {
                LOG.error("cancel run", e);
            } finally {
                scoopiEngine.shutdown(false);
            }
            stats.outputCancelled();
        }

        stats.outputMemStats();

        LOG.debug("close webdrivers");
        webDriverPool.close();

        LOG.debug("shutdown log manager");
        LogManager.shutdown();
    }

    public void setCleanShutdown(final boolean cleanShutdown) {
        this.cleanShutdown = cleanShutdown;
    }

}
