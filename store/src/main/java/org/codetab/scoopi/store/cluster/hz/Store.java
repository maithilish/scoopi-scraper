package org.codetab.scoopi.store.cluster.hz;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.codetab.scoopi.store.cluster.IClusterStore;

@Singleton
public class Store implements IClusterStore {

    private Map<String, Object> cache = new HashMap<>();

    @Override
    public boolean put(final String key, final Object value) {
        cache.put(key, value);
        return true;
    }

    @Override
    public Object get(final String key) {
        return cache.get(key);
    }
}
