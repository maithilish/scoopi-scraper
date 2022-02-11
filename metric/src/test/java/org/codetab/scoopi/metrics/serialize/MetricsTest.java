package org.codetab.scoopi.metrics.serialize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class MetricsTest {
    @InjectMocks
    private Metrics metrics;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetVersion() {
        String version = "4.0.0";

        String actual = metrics.getVersion();

        assertEquals(version, actual);
    }

    @Test
    public void testSetVersion() {
        String version = "Foo";
        metrics.setVersion(version);

        String actual = metrics.getVersion();

        assertEquals(version, actual);
    }

    @Test
    public void testSetGauges() {
        Map<String, Gauge> gauges = new HashMap<>();
        metrics.setGauges(gauges);

        Map<String, Gauge> actual = metrics.getGauges();

        assertSame(gauges, actual);
    }

    @Test
    public void testSetCounters() {
        Map<String, Counter> counters = new HashMap<>();
        metrics.setCounters(counters);

        Map<String, Counter> actual = metrics.getCounters();

        assertSame(counters, actual);
    }

    @Test
    public void testSetHistograms() {
        Map<String, Histogram> histograms = new HashMap<>();
        metrics.setHistograms(histograms);

        Map<String, Histogram> actual = metrics.getHistograms();

        assertSame(histograms, actual);
    }

    @Test
    public void testSetMeters() {
        Map<String, Meter> meters = new HashMap<>();
        metrics.setMeters(meters);

        Map<String, Meter> actual = metrics.getMeters();

        assertSame(meters, actual);
    }

    @Test
    public void testSetTimers() {
        Map<String, Timer> timers = new HashMap<>();
        metrics.setTimers(timers);

        Map<String, Timer> actual = metrics.getTimers();

        assertSame(timers, actual);
    }

}
