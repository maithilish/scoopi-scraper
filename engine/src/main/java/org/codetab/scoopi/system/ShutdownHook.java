package org.codetab.scoopi.system;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.pool.WebDriverPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * JVM shutdown hook.
 * @author Maithilish
 *
 */
@Singleton
public class ShutdownHook extends Thread {

    /**
     * logger.
     */
    private final Logger logger = LoggerFactory.getLogger(ShutdownHook.class);

    /**
     * activity service.
     */
    @Inject
    private Stats stats;
    @Inject
    private WebDriverPool webDriverPool;

    /**
     * <p>
     * public constructor.
     */
    @Inject
    public ShutdownHook() {
        // cs - if private then class has to be final which is unable to mock
        logger.info("shutdown hook created");
    }

    /**
     * log activities and memory stats.
     */
    @Override
    public synchronized void start() {
        stats.outputStats();
        stats.outputMemStats();

        logger.debug("closing webdrivers");
        webDriverPool.close();
    }
}
