package org.codetab.scoopi.store.cluster.hz;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.store.ICluster;
import org.codetab.scoopi.store.cluster.IClusterStore;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

@Singleton
public class Store implements IClusterStore {

    @Inject
    private ICluster cluster;

    private IMap<String, Object> objectMap;

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
        objectMap.set(key, value);
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
