package org.codetab.scoopi.store.cluster.ignite;

import javax.inject.Singleton;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.codetab.scoopi.store.cluster.ICluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class Cluster implements ICluster {

    static final Logger LOGGER = LoggerFactory.getLogger(Cluster.class);

    private Ignite ignite;
    private String memberId;

    @Override
    public boolean start() {
        LOGGER.info("start Ignite cluster");

        System.setProperty("IGNITE_QUIET", "true");
        System.setProperty("IGNITE_NO_ASCII", "true");
        System.setProperty("IGNITE_PERFORMANCE_SUGGESTIONS_DISABLED", "true");
        System.setProperty("IGNITE_CONSOLE_APPENDER", "false");

        ignite = Ignition.start("config/scoopi-cfg.xml");
        memberId = ignite.cluster().localNode().id().toString();
        return true;
    }

    @Override
    public boolean shutdown() {
        ignite.close();
        return true;
    }

    @Override
    public Object getInstance() {
        return ignite;
    }

    @Override
    public String getMemberId() {
        return memberId;
    }
}
