package org.codetab.scoopi.engine.module;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.store.ICluster;
import org.codetab.scoopi.store.IShutdown;
import org.codetab.scoopi.store.cluster.hz.CrashCleaner;

public class ClusterModule {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private Configs configs;
    @Inject
    private ICluster cluster;
    @Inject
    private IShutdown shutdown;
    @Inject
    private CrashCleaner crashCleaner;

    /*
     * bootstrap starts cluster and any other init is done here
     */
    public void initCluster() {
        configs.setProperty("scoopi.cluster.memberId", cluster.getMemberId());
    }

    public boolean stopCluster() {

        final int timeoutDefault = 60;
        int clusterShutdownTimeout = configs
                .getInt("scoopi.cluster.shutdown.timeout", timeoutDefault);
        TimeUnit timeUnit = TimeUnit.valueOf(configs
                .getConfig("scoopi.cluster.shutdown.timeoutUnit", "SECONDS")
                .toUpperCase());

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            shutdown.setTerminate();
            shutdown.terminate();
        });

        try {
            future.get(clusterShutdownTimeout, timeUnit);
            return true;
        } catch (Exception e) {
            LOG.error("failed to shutdown cluster {}", e);
            return false;
        }
    }

    public void initClusterListeners() {
        if (configs.isCluster()) {
            crashCleaner.init();
        }
    }
}
