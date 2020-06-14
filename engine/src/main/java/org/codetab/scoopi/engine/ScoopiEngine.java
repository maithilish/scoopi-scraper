package org.codetab.scoopi.engine;

import javax.inject.Inject;

import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.log.Log.CAT;
import org.codetab.scoopi.step.JobMediator;
import org.codetab.scoopi.step.TaskMediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScoopiEngine {

    static final Logger LOGGER = LoggerFactory.getLogger(ScoopiEngine.class);

    @Inject
    private SystemModule systemModule;
    @Inject
    private TaskMediator taskMediator;
    @Inject
    private JobMediator jobMediator;
    @Inject
    private ErrorLogger errorLogger;

    /*
     * single thread env throws CriticalException and terminates the app and
     * multi thread env may also throw CriticalException but they terminates
     * just the executing thread
     *
     */
    public void initSystem() {
        try {
            // single thread
            LOGGER.info("Start scoopi ..."); //$NON-NLS-1$

            LOGGER.info("initialize basic system");
            systemModule.startStats();
            systemModule.startErrorLogger();
            systemModule.addShutdownHook();
            systemModule.startMetrics();

            systemModule.initCluster();
            jobMediator.init();
            systemModule.initClusterListeners();

            systemModule.seedJobs();

            LOGGER.info("scoopi initialized");
            systemModule.waitForInput();
        } catch (final CriticalException e) {
            final String message = "terminate scoopi";
            errorLogger.log(CAT.FATAL, message, e);
            throw e;
        }
    }

    public void runJobs() {
        try {
            // multi thread
            LOGGER.info("--- switch to multi thread system ---");
            taskMediator.start();
            jobMediator.start();

            jobMediator.waitForFinish();
            systemModule.waitForFinish();

            systemModule.waitForInput();
            LOGGER.info("shutdown ...");
        } catch (final CriticalException e) {
            final String message = "terminate scoopi";
            errorLogger.log(CAT.FATAL, message, e);
            throw e;
        }
    }

    public void shutdown() {
        systemModule.stopMetrics();
        if (systemModule.stopCluster()) {
            systemModule.stopStats();
            LOGGER.info("scoopi completed");
        } else {
            LOGGER.info("scoopi exit");
            System.exit(1);
        }
    }
}
