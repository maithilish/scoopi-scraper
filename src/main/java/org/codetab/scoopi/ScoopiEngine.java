package org.codetab.scoopi;

import javax.inject.Inject;

import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.step.TaskMediator;
import org.codetab.scoopi.system.ErrorLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScoopiEngine {

    static final Logger LOGGER = LoggerFactory.getLogger(ScoopiEngine.class);

    @Inject
    private ScoopiSystem scoopiSystem;
    @Inject
    private TaskMediator taskMediator;
    @Inject
    private ErrorLogger errorLogger;

    /*
     * single thread env throws CriticalException and terminates the app and
     * multi thread env may also throw CriticalException but they terminates
     * just the executing thread
     *
     */
    public void start() {
        try {
            // single thread
            LOGGER.info("Start scoopi ..."); //$NON-NLS-1$

            LOGGER.info("initialize basic system");
            scoopiSystem.startStats();

            scoopiSystem.startErrorLogger();

            scoopiSystem.addShutdownHook();

            String defaultConfigFile = "scoopi-default.xml"; //$NON-NLS-1$
            String userConfigFile = scoopiSystem.getPropertyFileName();
            scoopiSystem.initConfigService(defaultConfigFile, userConfigFile);

            scoopiSystem.startMetricsServer();

            scoopiSystem.initDefs();

            scoopiSystem.seedLocatorGroups();

            LOGGER.info("scoopi initialized");

            scoopiSystem.waitForHeapDump();

            // multi thread

            LOGGER.info("switch to multi thread enviornment");

            taskMediator.start();

            taskMediator.waitForFinish();

            scoopiSystem.waitForFinish();

            scoopiSystem.waitForHeapDump();

            LOGGER.info("shutdown ...");
        } catch (CriticalException e) {
            String message = "terminate scoopi";
            errorLogger.log(CAT.FATAL, message, e);
        } finally {
            scoopiSystem.stopMetricsServer();
            scoopiSystem.stopStats();
            LOGGER.info("scoopi completed");
        }
    }
}
