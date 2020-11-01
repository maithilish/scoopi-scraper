package org.codetab.scoopi;

import static java.util.Objects.nonNull;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.bootstrap.Bootstrap;
import org.codetab.scoopi.config.BootConfigs;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.engine.ScoopiEngine;

public final class Scoopi {

    @Inject
    private ScoopiEngine scoopiEngine;

    public static void main(final String[] args) {
        Bootstrap bootstrap = null;
        try {
            // don't create logger (static or instance) before this
            BootConfigs bootConfigs = new BootConfigs();
            bootConfigs.configureLogPath();

            // bootstrap solo or cluster DI
            bootstrap = new Bootstrap(bootConfigs);
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
            // don't create static logger in this class
            Logger log = LogManager.getLogger();
            log.error("Scoopi terminated", e);
            if (nonNull(bootstrap)) {
                bootstrap.shutdown();
            }
            LogManager.shutdown();
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
            scoopiEngine.shutdown();
        }
    }

}
