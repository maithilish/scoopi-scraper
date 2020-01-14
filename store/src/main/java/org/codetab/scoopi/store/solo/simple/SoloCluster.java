package org.codetab.scoopi.store.solo.simple;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Singleton;

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
    public boolean start() {
        return true;
    }

    @Override
    public boolean shutdown() {
        return true;
    }

    @Override
    public Object getInstance() {
        return Optional.empty();
    }

}
