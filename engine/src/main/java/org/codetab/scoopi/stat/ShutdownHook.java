package org.codetab.scoopi.stat;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private boolean cleanShutdown = false;

    /**
     * <p>
     * public constructor.
     */
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
            outputTerminated();
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

    private void outputTerminated() {
        LOG.info("{}", "");
        LOG.info("{}", "--- Summary ---");
        LOG.error("Scoopi terminated");
    }
}
