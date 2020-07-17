package org.codetab.scoopi;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.bootstrap.Bootstrap;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.engine.ScoopiEngine;

public final class Scoopi {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private ScoopiEngine scoopiEngine;

    public static void main(final String[] args) {
        try {
            // bootstrap solo or cluster DI
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.bootDi();

            bootstrap.bootCluster();
            bootstrap.waitForQuorum();

            // setup config and defs
            bootstrap.setup();

            // start scoopi
            DInjector dInjector = bootstrap.getdInjector();
            Scoopi scoopi = dInjector.instance(Scoopi.class);
            scoopi.start();
        } catch (Exception e) {
            LOG.error("Scoopi terminated, {}", e);
        }
    }

    public void start() {
        try {
            scoopiEngine.initSystem();
            scoopiEngine.runJobs();
            scoopiEngine.waitForShutdown();
        } catch (Exception e) {
            // ignore, error logged in scoopiEngine
        } finally {
            scoopiEngine.shutdown(true);
        }
    }

}
