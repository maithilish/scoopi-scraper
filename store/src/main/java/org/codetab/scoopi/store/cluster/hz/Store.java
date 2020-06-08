package org.codetab.scoopi.store.cluster.hz;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.store.ICluster;
import org.codetab.scoopi.store.cluster.IClusterStore;

import com.hazelcast.core.HazelcastInstance;

@Singleton
public class Store implements IClusterStore {

    @Inject
    private ICluster cluster;

    private Map<String, Object> objectMap;

    @Override
    public void open() {
        HazelcastInstance hz = (HazelcastInstance) cluster.getInstance();
        objectMap = hz.getMap(DsName.STORE_MAP.toString());
    }

    @Override
    public void close() {
        // ScoopiEngine stops cluster
    }

    @Override
    public boolean put(final String key, final Object value) {
        objectMap.put(key, value);
        return true;
    }

    @Override
    public Object get(final String key) {
        return objectMap.get(key);
    }

    @Override
    public boolean contains(final String key) {
        return objectMap.containsKey(key);
    }
}
