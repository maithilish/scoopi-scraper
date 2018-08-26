package org.codetab.scoopi.cache;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.messages.Messages;
import org.codetab.scoopi.metrics.MetricsHelper;

public class ParserCache {

    private Map<Integer, String> cache = new HashMap<>();

    @Inject
    private MetricsHelper metricsHelper;

    public String get(final int key) {
        String value = cache.get(key);
        if (isNull(value)) {
            count("miss");
        } else {
            count("hit");
        }
        return value;
    }

    public void put(final int key, final String value) {
        if (nonNull(value)) {
            cache.put(key, value);
        }
    }

    // TODO - perf
    public int getKey(final Map<String, String> map) {
        Validate.notNull(map, Messages.getString("ParserCache.0")); //$NON-NLS-1$
        return Arrays.hashCode(map.values().toArray());
    }

    private void count(final String type) {
        metricsHelper.getCounter(this, "parser", "cache", type).inc();
    }
}
