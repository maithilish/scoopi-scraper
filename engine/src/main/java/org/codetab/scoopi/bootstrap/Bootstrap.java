package org.codetab.scoopi.bootstrap;

import org.codetab.scoopi.config.BootConfigs;
import org.codetab.scoopi.di.ClusterModule;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.di.SoloModule;
import org.codetab.scoopi.store.ICluster;
import org.codetab.scoopi.store.IStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bootstrap {

    static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    private DInjector dInjector; // actual injector to run scoopi
    private IStore store;
    private ICluster cluster;

    public void boot() {
        BootConfigs bootConfigs = new BootConfigs();

        if (bootConfigs.isSolo()) {

            LOGGER.info("Scoopi [solo/cluster]: solo"); //$NON-NLS-1$
            LOGGER.info("initialize solo injector");
            SoloModule soloModule = new SoloModule();
            dInjector = new DInjector(soloModule).instance(DInjector.class);
            store = dInjector.instance(IStore.class);
            store.open();
            soloModule.setStore(store);
        } else {

            LOGGER.info("Scoopi [solo/cluster]: cluster"); //$NON-NLS-1$
            LOGGER.info("initialize cluster injector");
            ClusterModule clusterModule = new ClusterModule();
            dInjector = new DInjector(clusterModule).instance(DInjector.class);
            store = dInjector.instance(IStore.class);

            LOGGER.info("bootup cluster");
            cluster = dInjector.instance(ICluster.class);
            cluster.start();

            store.open();
            clusterModule.setStore(store);
        }
    }

    public DInjector getdInjector() {
        return dInjector;
    }

    public IStore getStore() {
        return store;
    }

}
