package org.codetab.scoopi.engine;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.model.ERRORCAT;
import org.codetab.scoopi.step.JobMediator;
import org.codetab.scoopi.step.TaskMediator;

public class ScoopiEngine {

    static final Logger LOG = LogManager.getLogger();

    @Inject
    private SystemModule systemModule;
    @Inject
    private TaskMediator taskMediator;
    @Inject
    private JobMediator jobMediator;

    /*
     * single thread env throws CriticalException and terminates the app and
     * multi thread env may also throw CriticalException but they terminates
     * just the executing thread
     *
     */
    public void initSystem() {
        try {
            // single thread
            LOG.info("Start scoopi ..."); //$NON-NLS-1$

            LOG.info("initialize basic system");
            systemModule.startStats();
            systemModule.startErrorLogger();
            systemModule.addShutdownHook();
            systemModule.startMetrics();

            systemModule.initCluster();
            jobMediator.init();
            systemModule.initClusterListeners();

            systemModule.seedJobs();

            LOG.info("scoopi initialized");
            systemModule.waitForInput();
        } catch (final CriticalException e) {
            LOG.error("terminate scoopi [{}]", ERRORCAT.FATAL, e);
            throw e;
        }
    }

    public void runJobs() {
        try {
            // multi thread
            LOG.info("--- switch to multi thread system ---");
            taskMediator.start();
            jobMediator.start();

            jobMediator.waitForFinish();
            systemModule.waitForFinish();

            systemModule.waitForInput();
            LOG.info("shutdown ...");
        } catch (final CriticalException e) {
            LOG.error("terminate scoopi [{}]", ERRORCAT.FATAL, e);
            throw e;
        }
    }

    public void shutdown() {
        systemModule.stopMetrics();
        if (systemModule.stopCluster()) {
            systemModule.stopStats();
            LOG.info("scoopi run finished");
            systemModule.setCleanShutdown();
        } else {
            LOG.error("exit scoopi [{}]", ERRORCAT.FATAL);
            System.exit(1);
        }
    }
}
