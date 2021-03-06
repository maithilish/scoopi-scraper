package org.codetab.scoopi.store.solo.simple;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.apache.commons.lang3.NotImplementedException;
import org.codetab.scoopi.store.solo.ISoloStore;

@Singleton
public class Store implements ISoloStore {

    private Map<String, Object> cache;

    @Override
    public void open() {
        cache = new HashMap<>();
    }

    @Override
    public void close() {
        throw new NotImplementedException("");
    }

    @Override
    public boolean put(final String key, final Object value) {
        cache.put(key, value);
        return true;
    }

    @Override
    public Object get(final String key) {
        return cache.get(key);
    }

    @Override
    public boolean contains(final String key) {
        return cache.containsKey(key);
    }
}
