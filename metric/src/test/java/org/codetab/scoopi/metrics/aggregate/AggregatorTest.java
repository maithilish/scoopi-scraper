package org.codetab.scoopi.metrics.aggregate;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.assertj.core.util.Maps;
import org.codetab.scoopi.metrics.serialize.Counter;
import org.codetab.scoopi.metrics.serialize.Gauge;
import org.codetab.scoopi.metrics.serialize.Meter;
import org.codetab.scoopi.metrics.serialize.Metrics;
import org.codetab.scoopi.metrics.serialize.Timer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AggregatorTest {
    @InjectMocks
    private Aggregator aggregator;

    @Mock
    private ObjectMapper mapper;
    @Mock
    private Map<String, byte[]> metricsJsonData;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSetMetricsJsonData() throws IllegalAccessException {
        Map<String, byte[]> jsonData = new HashMap<>();
        aggregator.setMetricsJsonData(jsonData);

        Object actual = FieldUtils.readDeclaredField(aggregator,
                "metricsJsonData", true);
        assertSame(jsonData, actual);
    }

    @Test
    public void testAggregate() throws Exception {

        Metrics metrics = Mockito.mock(Metrics.class); // aggregated metrics
        Metrics memberMetrics = Mockito.mock(Metrics.class);

        Map<String, Gauge> grape = new HashMap<>();

        // gauge
        String gaugeId = "g1";
        Gauge gauge = new Gauge();
        String gaugeValueKey = "poolSize";
        gauge.setValue(Maps.newHashMap(gaugeValueKey, 64));
        grape.put(gaugeId, gauge);

        Map<String, Gauge> ag = new HashMap<>();
        Gauge agGauge = new Gauge();
        agGauge.setValue(Maps.newHashMap(gaugeValueKey, 32));
        ag.put(gaugeId, agGauge);

        // counter
        Map<String, Counter> orange = new HashMap<>();
        String counterId = "c1";
        Counter counter = new Counter();
        counter.setCount(5);
        orange.put(counterId, counter);

        Map<String, Counter> ag2 = new HashMap<>();
        Counter agCounter = new Counter();
        agCounter.setCount(2);
        ag2.put(counterId, agCounter);

        // meter
        Map<String, Meter> kiwi = new HashMap<>();
        Map<String, Meter> ag4 = new HashMap<>();

        String meterId = "m1";
        Meter meter = new Meter();
        meter.m15_rate = 12.1;
        kiwi.put(meterId, meter);

        Meter agMeter = new Meter();
        agMeter.m15_rate = 3.2;
        ag4.put(meterId, agMeter);

        // timer
        Map<String, Timer> mango = new HashMap<>();
        Map<String, Timer> ag6 = new HashMap<>();

        String timerId = "t1";
        Timer timer = new Timer();
        timer.m15_rate = 31.2;
        mango.put(timerId, timer);

        Timer agTimer = new Timer();
        agTimer.m15_rate = 5.3;
        ag6.put(timerId, agTimer);

        String memberId = "Foo";
        byte[] jsonDataAggregate = {};
        String memberId2 = "Bar";
        byte[] jsonData = {};

        Map<String, byte[]> jsonDataMap = new HashMap<>();
        jsonDataMap.put(memberId, jsonDataAggregate);
        jsonDataMap.put(memberId2, jsonData);

        when(memberMetrics.getCounters()).thenReturn(orange);
        when(metrics.getCounters()).thenReturn(ag2);

        aggregator.setMetricsJsonData(jsonDataMap);

        when(mapper.readValue(jsonData, Metrics.class)).thenReturn(metrics)
                .thenReturn(memberMetrics);
        when(memberMetrics.getGauges()).thenReturn(grape);
        when(metrics.getGauges()).thenReturn(ag);

        when(memberMetrics.getMeters()).thenReturn(kiwi);
        when(metrics.getMeters()).thenReturn(ag4);
        when(memberMetrics.getTimers()).thenReturn(mango);
        when(metrics.getTimers()).thenReturn(ag6);

        aggregator.aggregate();

        // assert aggregated metrics
        assertEquals(96, ag.get(gaugeId).getValue().get(gaugeValueKey));
        assertEquals(7, ag2.get(counterId).getCount());
        assertEquals(7.65, ag4.get(meterId).m15_rate, 0);
        assertEquals(18.25, ag6.get(timerId).m15_rate, 0);
    }

    @Test
    public void testAggregateNoEntryInAggregate() throws Exception {

        Metrics metrics = Mockito.mock(Metrics.class); // aggregated metrics
        Metrics memberMetrics = Mockito.mock(Metrics.class);

        Map<String, Gauge> grape = new HashMap<>();

        // gauge
        String gaugeId = "g1";
        Gauge gauge = new Gauge();
        String gaugeValueKey = "poolSize";
        gauge.setValue(Maps.newHashMap(gaugeValueKey, 64));
        grape.put(gaugeId, gauge);

        Map<String, Gauge> ag = new HashMap<>(); // no entry in aggregate

        // counter
        Map<String, Counter> orange = new HashMap<>();
        String counterId = "c1";
        Counter counter = new Counter();
        counter.setCount(5);
        orange.put(counterId, counter);

        Map<String, Counter> ag2 = new HashMap<>(); // no entry in aggregate

        // meter
        Map<String, Meter> kiwi = new HashMap<>();
        Map<String, Meter> ag4 = new HashMap<>(); // no entry in aggregate

        String meterId = "m1";
        Meter meter = new Meter();
        meter.m15_rate = 12.1;
        kiwi.put(meterId, meter);

        // timer
        Map<String, Timer> mango = new HashMap<>();
        Map<String, Timer> ag6 = new HashMap<>(); // no entry in aggregate

        String timerId = "t1";
        Timer timer = new Timer();
        timer.m15_rate = 31.2;
        mango.put(timerId, timer);

        String memberId = "Foo";
        byte[] jsonDataAggregate = {};
        String memberId2 = "Bar";
        byte[] jsonData = {};

        Map<String, byte[]> jsonDataMap = new HashMap<>();
        jsonDataMap.put(memberId, jsonDataAggregate);
        jsonDataMap.put(memberId2, jsonData);

        when(memberMetrics.getCounters()).thenReturn(orange);
        when(metrics.getCounters()).thenReturn(ag2);

        aggregator.setMetricsJsonData(jsonDataMap);

        when(mapper.readValue(jsonData, Metrics.class)).thenReturn(metrics)
                .thenReturn(memberMetrics);
        when(memberMetrics.getGauges()).thenReturn(grape);
        when(metrics.getGauges()).thenReturn(ag);

        when(memberMetrics.getMeters()).thenReturn(kiwi);
        when(metrics.getMeters()).thenReturn(ag4);
        when(memberMetrics.getTimers()).thenReturn(mango);
        when(metrics.getTimers()).thenReturn(ag6);

        aggregator.aggregate();

        // assert aggregated metrics
        assertEquals(64, ag.get(gaugeId).getValue().get(gaugeValueKey));
        assertEquals(5, ag2.get(counterId).getCount());
        assertEquals(12.1, ag4.get(meterId).m15_rate, 0);
        assertEquals(31.2, ag6.get(timerId).m15_rate, 0);
    }

    @Test
    public void testGetJson() throws Exception {
        byte[] apple = {};

        Metrics metrics = Mockito.mock(Metrics.class); // aggregated metrics
        FieldUtils.writeDeclaredField(aggregator, "metrics", metrics, true);

        when(mapper.writeValueAsBytes(metrics)).thenReturn(apple);

        byte[] actual = aggregator.getJson();

        assertArrayEquals(apple, actual);
    }
}
