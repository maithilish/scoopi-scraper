package org.codetab.scoopi.store.local.simple;

import java.util.HashMap;
import java.util.Map;

import org.codetab.scoopi.store.local.ILocalStore;

public class LocalStore implements ILocalStore {

    private String name = "Local store";

    private StoreStatus status = StoreStatus.STARTED;

    Map<String, Object> cache = new HashMap<>();

    @Override
    public StoreStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(StoreStatus status) {
        this.status = status;
    }

    @Override
    public boolean put(String key, Object value) {
        cache.put(key, value);
        return true;
    }

    @Override
    public Object get(String key) {
        return cache.get(key);
    }

    @Override
    public String getName() {
        return name;
    }
}
