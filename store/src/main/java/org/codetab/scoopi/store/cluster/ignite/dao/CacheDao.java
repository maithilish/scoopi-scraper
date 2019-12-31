package org.codetab.scoopi.store.cluster.ignite.dao;

import javax.inject.Inject;

import org.apache.ignite.IgniteCache;
import org.codetab.scoopi.store.cluster.ignite.Cluster;

public class CacheDao {

    @Inject
    private Cluster cluster;

    public long getJobIdSeq() {
        return cluster.getJobIdSeq().getAndIncrement();
    }

    public void cachePut(final String key, final Integer val) {
        IgniteCache<String, Object> cache = cluster.getCache();
        Integer value = (Integer) cache.get(key);
        if (value == null) {
            value = val;
        } else {
            value += val;
        }
        cache.put(key, value);
    }
}
