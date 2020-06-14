package org.codetab.scoopi.di;

import javax.inject.Singleton;

import org.codetab.scoopi.store.IBarricade;
import org.codetab.scoopi.store.ICluster;
import org.codetab.scoopi.store.IJobStore;
import org.codetab.scoopi.store.IShutdown;
import org.codetab.scoopi.store.IStore;

public class SoloModule extends BaseModule {

    @Override
    protected void configure() {

        super.configure();

        // bind solo specific classes
        bind(IStore.class).to(org.codetab.scoopi.store.solo.simple.Store.class)
                .in(Singleton.class);
        bind(IJobStore.class)
                .to(org.codetab.scoopi.store.solo.simple.JobStore.class)
                .in(Singleton.class);
        bind(IShutdown.class)
                .to(org.codetab.scoopi.store.solo.simple.Shutdown.class)
                .in(Singleton.class);

        // solo - dummy cluster, barricade
        bind(ICluster.class)
                .to(org.codetab.scoopi.store.solo.simple.SoloCluster.class)
                .in(Singleton.class);

        bind(IBarricade.class)
                .to(org.codetab.scoopi.store.solo.simple.Barricade.class);

    }

}
