package org.codetab.scoopi.store.cluster.ignite;

import javax.inject.Singleton;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;

@Singleton
public class Cluster {

    private Ignite ignite;

    public void start() {
        System.setProperty("IGNITE_QUIET", "true");
        System.setProperty("IGNITE_NO_ASCII", "true");
        System.setProperty("IGNITE_PERFORMANCE_SUGGESTIONS_DISABLED", "true");
        ignite = Ignition.start("config/scoopi-cache.xml");
    }

    public void stop() {
        ignite.close();
    }

}
