package org.codetab.scoopi.engine;

import javax.inject.Inject;

import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.log.Log.CAT;
import org.codetab.scoopi.step.JobMediator;
import org.codetab.scoopi.step.TaskMediator;
import org.codetab.scoopi.step.extract.JobSeeder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScoopiEngine {

    static final Logger LOGGER = LoggerFactory.getLogger(ScoopiEngine.class);

    @Inject
    private ScoopiSystem scoopiSystem;
    @Inject
    private TaskMediator taskMediator;
    @Inject
    private JobMediator jobMediator;
    @Inject
    private ErrorLogger errorLogger;
    @Inject
    private JobSeeder jobSeeder;

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
            scoopiSystem.startMetrics();

            scoopiSystem.initCluster();
            jobMediator.init();
            scoopiSystem.initClusterListeners();

            if (jobSeeder.acquirePermitToSeed()) {
                jobSeeder.clearDanglingJobs();
                jobSeeder.seedLocatorGroups();
            }
            LOGGER.info("scoopi initialized");

            scoopiSystem.waitForInput();

            // multi thread
            LOGGER.info("--- switch to multi thread system ---");
            taskMediator.start();
            jobMediator.start();

            jobMediator.waitForFinish();
            scoopiSystem.waitForFinish();

            scoopiSystem.waitForInput();
            LOGGER.info("shutdown ...");
        } catch (final CriticalException e) {
            final String message = "terminate scoopi";
            errorLogger.log(CAT.FATAL, message, e);
        } finally {
            scoopiSystem.stopMetrics();
            scoopiSystem.stopCluster();
            scoopiSystem.stopStats();
            LOGGER.info("scoopi completed");
        }
    }
}
