package org.codetab.scoopi.metrics.serialize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.scoopi.metrics.serialize.Serializer.Builder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.UniformReservoir;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SerializerTest {

    @Before
    public void setUp() throws Exception {

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testForRegistry() throws IllegalAccessException {
        MetricRegistry registry = Mockito.mock(MetricRegistry.class);

        Builder actual = Serializer.forRegistry(registry);

        assertSame(registry, readField(actual, "registry", true));
        assertNull(readField(actual, "consumer", true));
        assertEquals(TimeUnit.SECONDS, readField(actual, "rateUnit", true));
        assertEquals(TimeUnit.MILLISECONDS,
                readField(actual, "durationUnit", true));
        assertSame(MetricFilter.ALL, readField(actual, "filter", true));
        assertNull(readField(actual, "executor", true));
        assertTrue((Boolean) readField(actual, "shutdownExecutorOnStop", true));
        assertEquals(0,
                ((Set) readField(actual, "disabledMetricAttributes", true))
                        .size());
    }

    @SuppressWarnings({"rawtypes"})
    @Test
    public void testReport() throws IllegalAccessException, IOException {

        SortedMap<String, Gauge> gauges = new TreeMap<>();
        gauges.put("g1", () -> 5);

        SortedMap<String, Counter> counters = new TreeMap<>();
        Counter counter = new Counter();
        counter.inc(10);
        counters.put("c1", counter);

        SortedMap<String, Histogram> histograms = new TreeMap<>();
        Histogram histogram = new Histogram(new UniformReservoir());
        histogram.update(20);
        histograms.put("h1", histogram);

        SortedMap<String, Meter> meters = new TreeMap<>();
        Meter meter = new Meter();
        meter.mark(40);
        meters.put("m1", meter);

        SortedMap<String, Timer> timers = new TreeMap<>();
        Timer timer = new Timer();
        timer.update(Duration.ofNanos(25));
        timers.put("t1", timer);

        ObjectMapper mapper = new ObjectMapper();
        List<byte[]> result = new ArrayList<>();

        Consumer<byte[]> consumer = r -> result.add(r);

        MetricRegistry metricRegistry = new MetricRegistry();
        Serializer serializer = Serializer.forRegistry(metricRegistry)
                .consumer(consumer).build();

        FieldUtils.writeDeclaredField(serializer, "mapper", mapper, true);

        serializer.report(gauges, counters, histograms, meters, timers);

        JsonNode actual = mapper.readTree(result.get(0));

        assertEquals(1, actual.at("/gauges").size());
        assertEquals(5, actual.at("/gauges/g1/value").asInt());

        assertEquals(1, actual.at("/counters").size());
        assertEquals(10, actual.at("/counters/c1/count").asInt());

        assertEquals(1, actual.at("/histograms").size());
        assertEquals(20, actual.at("/histograms/h1/snapshot/values/0").asInt());

        assertEquals(1, actual.at("/meters").size());
        assertEquals(40, actual.at("/meters/m1/count").asInt());

        assertEquals(1, actual.at("/timers").size());
        assertEquals(25, actual.at("/timers/t1/snapshot/values/0").asInt());
    }

    @Test
    public void testReportThrowException()
            throws IllegalAccessException, IOException {

        ObjectMapper mapper = Mockito.mock(ObjectMapper.class);
        List<byte[]> result = new ArrayList<>();

        Consumer<byte[]> consumer = r -> result.add(r);

        MetricRegistry metricRegistry = new MetricRegistry();
        Serializer serializer = Serializer.forRegistry(metricRegistry)
                .consumer(consumer).build();

        FieldUtils.writeDeclaredField(serializer, "mapper", mapper, true);

        when(mapper.writeValueAsBytes(any(HashMap.class)))
                .thenThrow(JsonProcessingException.class);

        serializer.report(null, null, null, null, null);

        assertTrue(result.isEmpty());
    }

    private Object readField(final Object target, final String fieldName,
            final boolean forceAccess) throws IllegalAccessException {
        return FieldUtils.readDeclaredField(target, fieldName, forceAccess);
    }
}
