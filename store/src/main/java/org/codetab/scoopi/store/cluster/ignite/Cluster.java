package org.codetab.scoopi.store.cluster.ignite;

import javax.inject.Singleton;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicLong;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;

@Singleton
public class Cluster {

    private Ignite ignite;
    private IgniteAtomicLong jobIdSeq;
    private String nodeId;
    private IgniteCache<String, Object> cache;

    public void start() {
        System.setProperty("IGNITE_QUIET", "true");
        System.setProperty("IGNITE_NO_ASCII", "true");
        System.setProperty("IGNITE_PERFORMANCE_SUGGESTIONS_DISABLED", "true");
        System.setProperty("IGNITE_CONSOLE_APPENDER", "false");

        // ignite = Ignition.start();
        ignite = Ignition.start("config/scoopi-cfg.xml");

        nodeId = ignite.cluster().localNode().id().toString();

        jobIdSeq = ignite.atomicLong("job_id_seq", 0, true);
        cache = ignite.getOrCreateCache("scoopi");
    }

    public String getNodeId() {
        return nodeId;
    }

    public void stop() {
        ignite.close();
    }

    public IgniteAtomicLong getJobIdSeq() {
        return jobIdSeq;
    }

    public IgniteCache<String, Object> getCache() {
        return cache;
    }

}
