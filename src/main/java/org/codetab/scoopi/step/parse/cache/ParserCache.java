package org.codetab.scoopi.step.parse.cache;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.notNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.codetab.scoopi.metrics.MetricsHelper;

public class ParserCache {

    private Map<Integer, String> cache = new HashMap<>();

    @Inject
    private MetricsHelper metricsHelper;

    public String get(final int key) {
        String value = cache.get(key);
        if (isNull(value)) {
            metricsHelper.getCounter(this, "parser", "cache", "miss").inc();
        } else {
            metricsHelper.getCounter(this, "parser", "cache", "hit").inc();
        }
        return value;
    }

    public void put(final int key, final String value) {
        if (nonNull(value)) {
            cache.put(key, value);
            metricsHelper.getMeter(this, "parser", "cache").mark();
        }
    }

    // TODO optimise
    public int getKey(final Map<String, String> map) {
        notNull(map, "map must not be null");
        return Arrays.hashCode(map.values().toArray());
    }
}
