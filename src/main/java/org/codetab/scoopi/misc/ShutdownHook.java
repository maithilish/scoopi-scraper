package org.codetab.scoopi.misc;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.messages.Messages;
import org.codetab.scoopi.shared.StatService;
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
    private StatService activityService;

    /**
     * <p>
     * public constructor.
     */
    @Inject
    public ShutdownHook() {
        // cs - if private then class has to be final which is unable to mock
        logger.info(Messages.getString("ShutdownHook.0")); //$NON-NLS-1$
    }

    /**
     * log activities and memory stats.
     */
    @Override
    public synchronized void start() {
        activityService.outputLog();
        activityService.logMemoryUsage();
    }
}
