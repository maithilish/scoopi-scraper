package org.codetab.scoopi.metrics.aggregate;

import java.io.IOException;
import java.util.Map;

import org.codetab.scoopi.metrics.serialize.Counter;
import org.codetab.scoopi.metrics.serialize.Gauge;
import org.codetab.scoopi.metrics.serialize.Meter;
import org.codetab.scoopi.metrics.serialize.Metrics;
import org.codetab.scoopi.metrics.serialize.Timer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Aggregator {

    private ObjectMapper mapper;
    private Metrics metrics; // aggregated metrics
    private Map<String, byte[]> metricsJsonData;

    public Aggregator() {
        mapper = new ObjectMapper();
    }

    public void setMetricsJsonData(final Map<String, byte[]> metricsJsonData) {
        this.metricsJsonData = metricsJsonData;
    }

    public void aggregate()
            throws JsonParseException, JsonMappingException, IOException {
        metrics = null;
        for (String memberId : metricsJsonData.keySet()) {
            byte[] jsonData = metricsJsonData.get(memberId);
            Metrics memberMetrics = mapper.readValue(jsonData, Metrics.class);
            if (metrics == null) {
                metrics = memberMetrics;
            } else {
                aggregate(memberMetrics);
            }
        }
    }

    public byte[] getJson() throws JsonProcessingException {
        return mapper.writeValueAsBytes(metrics);
    }

    private void aggregate(final Metrics memberMetrics) {
        aggregateGauges(memberMetrics.getGauges());
        aggregateCounters(memberMetrics.getCounters());
        aggregateMeters(memberMetrics.getMeters());
        aggregateTimers(memberMetrics.getTimers());
    }

    private void aggregateGauges(final Map<String, Gauge> gauges) {
        Map<String, Gauge> ag = metrics.getGauges();
        for (String key : gauges.keySet()) {
            if (ag.containsKey(key)) {
                ag.get(key).aggregate(gauges.get(key));
            } else {
                ag.put(key, gauges.get(key));
            }
        }
    }

    private void aggregateCounters(final Map<String, Counter> counters) {
        Map<String, Counter> ag = metrics.getCounters();
        for (String key : counters.keySet()) {
            if (ag.containsKey(key)) {
                ag.get(key).aggregate(counters.get(key));
            } else {
                ag.put(key, counters.get(key));
            }
        }
    }

    private void aggregateMeters(final Map<String, Meter> meters) {
        Map<String, Meter> ag = metrics.getMeters();
        for (String key : meters.keySet()) {
            if (ag.containsKey(key)) {
                ag.get(key).aggregate(meters.get(key));
            } else {
                ag.put(key, meters.get(key));
            }
        }
    }

    private void aggregateTimers(final Map<String, Timer> timers) {
        Map<String, Timer> ag = metrics.getTimers();
        for (String key : timers.keySet()) {
            if (ag.containsKey(key)) {
                ag.get(key).aggregate(timers.get(key));
            } else {
                ag.put(key, timers.get(key));
            }
        }
    }
}
