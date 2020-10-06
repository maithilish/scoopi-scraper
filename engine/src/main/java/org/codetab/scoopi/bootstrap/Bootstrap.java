package org.codetab.scoopi.bootstrap;

import java.lang.management.ManagementFactory;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.config.BootConfigs;
import org.codetab.scoopi.di.BaseModule;
import org.codetab.scoopi.di.ClusterModule;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.di.SoloModule;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.store.IBarricade;
import org.codetab.scoopi.store.ICluster;
import org.codetab.scoopi.store.IStore;

public class Bootstrap {

    private static final Logger LOG = LogManager.getLogger();

    private DInjector dInjector; // actual injector to run scoopi
    private IStore store;
    private ICluster cluster;
    private BootConfigs bootConfigs;
    private BaseModule module;

    public Bootstrap(final BootConfigs bootConfigs) {
        this.bootConfigs = bootConfigs;
    }

    /**
     * create DI, store and cluster
     */
    public void bootDi() {
        LOG.debug("jvm process id {}",
                ManagementFactory.getRuntimeMXBean().getName());
        if (bootConfigs.isSolo()) {
            LOG.info("Scoopi [solo/cluster]: solo"); //$NON-NLS-1$
            LOG.info("initialize solo injector");
            module = new SoloModule();
            dInjector = new DInjector(module).instance(DInjector.class);
        } else {
            LOG.info("Scoopi [solo/cluster]: cluster"); //$NON-NLS-1$
            LOG.info("initialize cluster injector");
            module = new ClusterModule();
            dInjector = new DInjector(module).instance(DInjector.class);
        }
    }

    public void bootCluster() {
        if (!bootConfigs.isSolo()) {
            LOG.info("bootup cluster");
        }

        store = dInjector.instance(IStore.class);
        cluster = dInjector.instance(ICluster.class);
        cluster.start();

        LOG.info("open store");
        store.open();
        module.setStore(store);
    }

    public void waitForQuorum() {
        if (!bootConfigs.isSolo()) {
            String qSizeKey = "scoopi.cluster.quorum.size";
            int qSize = Integer.parseInt(bootConfigs.getConfig(qSizeKey, "3"));
            String qTimeoutKey = "scoopi.cluster.quorum.timeout";
            int qTimeout =
                    Integer.parseInt(bootConfigs.getConfig(qTimeoutKey, "60"));

            LOG.info("wait for cluster quorum of {} nodes, timeout {}", qSize,
                    qTimeout);
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                while (true) {
                    if (cluster.getSize() >= qSize) {
                        break;
                    }
                }
                LOG.info("cluster quorum of {} formed", qSize);
            });
            try {
                future.get(qTimeout, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException
                    | TimeoutException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                LOG.error("failed to get quorum of {} after {} seconds", qSize,
                        qTimeout);
                LOG.info(
                        "timeout and quorum size are set with {} and {} properties, {}",
                        qTimeoutKey, qSizeKey,
                        "set them via system property or scoopi.properties");
                cluster.shutdown();
                throw new CriticalException("failed to get cluster quorum", e);
            }
        }
    }

    /**
     * create config and defs data and push to store
     */
    public void setup() {
        String barricadeName = "setupBarricade";
        IBarricade barricade = dInjector.instance(IBarricade.class);
        barricade.setup(barricadeName);
        barricade.await();

        // only one node is allowed to init system
        if (barricade.isAllowed()) {
            try {
                LOG.info("setup system ...");
                // prepare ConfigComposer before DefComposer creation,
                // so can't inject it.
                LOG.info("compose configs");
                dInjector.instance(ConfigsComposer.class).compose();
                LOG.info("compose defs");
                dInjector.instance(DefsComposer.class).compose();
            } finally {
                LOG.info("release {}", barricadeName);
                barricade.finish();
                LOG.debug("{} released", barricadeName);
            }
        } else {
            LOG.info("system initialized by another node, cross {}",
                    barricadeName);
        }
    }

    /**
     * Shutdown bootstrap, in case of errors before engine start.
     */
    public void shutdown() {
        cluster.shutdown();
    }

    public DInjector getdInjector() {
        return dInjector;
    }

    public IStore getStore() {
        return store;
    }

}
