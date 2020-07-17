package org.codetab.scoopi.engine;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.engine.module.ClusterModule;
import org.codetab.scoopi.engine.module.JobSeedModule;
import org.codetab.scoopi.engine.module.MediatorModule;
import org.codetab.scoopi.engine.module.MetricsModule;
import org.codetab.scoopi.engine.module.ShutdownModule;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.model.ERROR;

public class ScoopiEngine {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private ClusterModule clusterModule;
    @Inject
    private MetricsModule metricsModule;
    @Inject
    private JobSeedModule jobSeedModule;
    @Inject
    private MediatorModule mediatorModule;
    @Inject
    private ShutdownModule shutdownModule;

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
            metricsModule.startStats();
            metricsModule.startErrors();
            shutdownModule.addShutdownHook();
            metricsModule.startMetrics();

            clusterModule.initCluster();

            mediatorModule.initJobMediator();

            clusterModule.initClusterListeners();

            jobSeedModule.seedJobs();

            LOG.info("scoopi initialized");
        } catch (final CriticalException e) {
            LOG.error("terminate scoopi [{}]", ERROR.FATAL, e);
            throw e;
        }
    }

    public void runJobs() {
        try {
            // multi thread
            LOG.info("--- switch to multi thread system ---");

            mediatorModule.startTaskMediator();

            // job seed is async, wait else monitor may trigger early shutdown
            jobSeedModule.awaitForJobSeed();
            mediatorModule.startJobMediator();

        } catch (final CriticalException e) {
            LOG.error("terminate scoopi [{}]", ERROR.FATAL, e);
            throw e;
        }
    }

    public void waitForShutdown() {
        try {
            LOG.info("wait for JobMediator");
            mediatorModule.waitForJobMediator();
            LOG.info("wait for AppenderMediator");
            mediatorModule.waitForAppenderMediator();
        } catch (final CriticalException e) {
            LOG.error("terminate scoopi [{}]", ERROR.FATAL, e);
            throw e;
        }
    }

    public void shutdown(final boolean cleanShutdown) {
        if (shutdownModule.hasShutdownStarted()) {
            return;
        }
        shutdownModule.setCleanShutdown(cleanShutdown);
        if (cleanShutdown) {
            LOG.info("start normal shutdown ...");
        } else {
            LOG.info("cancel requested, start shutdown ...");
        }
        metricsModule.stopMetrics();
        if (clusterModule.stopCluster()) {
            metricsModule.stopStats();
            LOG.info("scoopi run finished");
        } else {
            LOG.error("exit scoopi [{}]", ERROR.FATAL);
            System.exit(1);
        }
    }

    public void cancel() {
        mediatorModule.cancelJobMediator();
    }
}
