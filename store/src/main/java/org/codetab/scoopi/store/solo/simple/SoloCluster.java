package org.codetab.scoopi.store.solo.simple;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Singleton;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.store.ICluster;

/**
 * Dummy Cluster
 * @author m
 *
 */
@Singleton
public class SoloCluster implements ICluster {

    @Override
    public String getMemberId() {
        return "solo";
    }

    @Override
    public Map<String, byte[]> getMetricsHolder() {
        return new HashMap<>();
    }

    // ignore methods
    @Override
    public void start() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public Object getInstance() {
        return Optional.empty();
    }

    @Override
    public String getLeader() {
        return "solo";
    }

    @Override
    public Object getTxOptions(final Configs configs) {
        return Optional.empty();
    }

}
