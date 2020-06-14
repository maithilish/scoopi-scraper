package org.codetab.scoopi;

import javax.inject.Inject;

import org.codetab.scoopi.bootstrap.Bootstrap;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.engine.ScoopiEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Scoopi {

    static final Logger LOGGER = LoggerFactory.getLogger(Scoopi.class);

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
            LOGGER.error("Scoopi terminated, {}", e.getMessage());
        }
    }

    public void start() {
        try {
            scoopiEngine.initSystem();
            scoopiEngine.runJobs();
        } catch (Exception e) {
            // ignore, handled in scoopiEngine
        } finally {
            scoopiEngine.shutdown();
        }
    }

}
