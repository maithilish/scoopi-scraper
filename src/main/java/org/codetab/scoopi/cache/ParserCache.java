package org.codetab.scoopi.cache;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.messages.Messages;
import org.codetab.scoopi.metrics.MetricsHelper;

public class ParserCache {

    private Map<Integer, String> cache;

    @Inject
    private MetricsHelper metricsHelper;

    public ParserCache() {
        cache = new HashMap<Integer, String>();
    }

    public String get(final int key) {
        String value = cache.get(key);
        if (value == null) {
            count("miss");
        } else {
            count("hit");
        }
        return value;
    }

    public void put(final int key, final String value) {
        if (value != null) {
            cache.put(key, value);
        }
    }

    public int getKey(final Map<String, String> map) {
        Validate.notNull(map, Messages.getString("ParserCache.0")); //$NON-NLS-1$
        return Arrays.hashCode(map.values().toArray());
    }

    private void count(final String type) {
        metricsHelper.getCounter(this, "parser", "cache", type).inc();
    }
}
