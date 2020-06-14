package org.codetab.scoopi.di;

import javax.inject.Singleton;

import org.codetab.scoopi.store.IBarricade;
import org.codetab.scoopi.store.ICluster;
import org.codetab.scoopi.store.IJobStore;
import org.codetab.scoopi.store.IShutdown;
import org.codetab.scoopi.store.IStore;

public class ClusterModule extends BaseModule {

    @Override
    protected void configure() {
        super.configure();

        // bind cluster specific classes
        bind(ICluster.class)
                .to(org.codetab.scoopi.store.cluster.hz.Cluster.class)
                .in(Singleton.class);
        bind(IStore.class).to(org.codetab.scoopi.store.cluster.hz.Store.class)
                .in(Singleton.class);
        bind(IJobStore.class)
                .to(org.codetab.scoopi.store.cluster.hz.JobStore.class)
                .in(Singleton.class);
        bind(IShutdown.class)
                .to(org.codetab.scoopi.store.cluster.hz.Shutdown.class)
                .in(Singleton.class);

        bind(IBarricade.class)
                .to(org.codetab.scoopi.store.cluster.hz.Barricade.class);
    }

}
