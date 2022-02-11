package org.codetab.scoopi.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import org.assertj.core.util.Arrays;
import org.codetab.scoopi.metrics.serialize.Serializer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

public class MetricsHelperTest {

    @InjectMocks
    private MetricsHelper metricsHelper;

    private MetricRegistry metricRegistry;

    @SuppressWarnings("static-access")
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        metricRegistry = metricsHelper.METRICS;
    }

    @Test
    public void testGetTimer() {
        Object clz = Mockito.mock(Object.class);
        String[] names = {"Foo", "Timer"};

        Timer actual = metricsHelper.getTimer(clz, names);

        SortedMap<String, Timer> map = metricRegistry.getTimers();

        assertEquals(1, map.size());
        assertSame(map.get("Object.Foo.Timer"), actual);
    }

    @Test
    public void testGetMeter() {
        Object clz = Mockito.mock(Object.class);
        String[] names = {"Foo", "Meter"};

        Meter actual = metricsHelper.getMeter(clz, names);

        SortedMap<String, Meter> map = metricRegistry.getMeters();

        assertEquals(1, map.size());
        assertSame(map.get("Object.Foo.Meter"), actual);
    }

    @Test
    public void testGetCounter() {
        Object clz = Mockito.mock(Object.class);
        String[] names = {"Foo", "Counter"};

        Counter actual = metricsHelper.getCounter(clz, names);

        SortedMap<String, Counter> map = metricRegistry.getCounters();

        assertEquals(1, map.size());
        assertSame(map.get("Object.Foo.Counter"), actual);
    }

    @Test
    public void testRegisterGuage() {
        Integer value = 5;
        Object clz = Mockito.mock(Object.class);

        String[] names = {"Foo", "Gauge"};

        metricsHelper.registerGuage(value, clz, names);

        @SuppressWarnings("rawtypes")
        SortedMap<String, Gauge> map = metricRegistry.getGauges();

        assertEquals(1, map.size());
        assertSame(map.get("Object.Foo.Gauge").getValue(), value);
    }

    @Test
    public void testClearGuages() {
        Integer value = 5;
        Object clz = Mockito.mock(Object.class);

        String[] names = {"Foo", "Gauge"};

        @SuppressWarnings("rawtypes")
        SortedMap<String, Gauge> map = metricRegistry.getGauges();
        if (map.size() == 0) {
            metricsHelper.registerGuage(value, clz, names);
        }

        map = metricRegistry.getGauges();
        assertEquals(1, map.size());
        assertSame(map.get("Object.Foo.Gauge").getValue(), value);

        metricsHelper.clearGuages();

        map = metricRegistry.getGauges();
        assertEquals(0, map.size());
    }

    @Test
    public void testInitMetrics() {

        metricRegistry.remove("Object.Foo.Counter");

        metricsHelper.initMetrics();

        SortedMap<String, Counter> map = metricRegistry.getCounters();

        assertEquals(4, map.size());
        assertNotNull(map.get("ParserCache.parser.cache.hit"));
        assertNotNull(map.get("ParserCache.parser.cache.miss"));
        assertNotNull(map.get("PageLoader.fetch.web"));
        assertNotNull(map.get("Task.system.error"));
    }

    @Test
    public void testStartJsonSerializer() throws IllegalAccessException {
        String memberId = "Foo";
        Map<String, byte[]> metricsMap = new HashMap<>();
        int period = 1;
        Exception e = Mockito.mock(Exception.class);
        String apple = "Bar";

        when(e.getMessage()).thenReturn(apple);

        Serializer actual =
                metricsHelper.startJsonSerializer(memberId, metricsMap, period);

        actual.report();

        assertEquals(metricsMap.size(), 1);
        assertEquals(695, Arrays.asList(metricsMap.get(memberId)).size());
    }

    @Test
    public void testGetURL() throws Exception {
        File file = File.createTempFile("foo", ".bar");

        String path = file.getAbsolutePath();
        URL url = file.toURI().toURL();

        URL actual = metricsHelper.getURL(path);

        assertEquals(url, actual);
    }

    @Test
    public void testGetURLOfResource() throws Exception {
        String path = "/test-dummy.txt";
        URL url = MetricsHelper.class.getResource(path);

        URL actual = metricsHelper.getURL(path);

        assertEquals(url, actual);
    }

    @Test
    public void testGetURLNotFound() throws Exception {
        String path = "file:/tmp/xy.zz";

        assertThrows(FileNotFoundException.class,
                () -> metricsHelper.getURL(path));
    }

    @Test
    public void testPrintSnapshot() {
        String name = "Foo";
        long count = 1L;
        Snapshot ss = Mockito.mock(Snapshot.class);
        long divisor = 1L;
        long apple = 1L;
        long kiwi = 1L;
        double cherry = 1.0d;
        double fig = 1.0d;
        double ubachvff = 1.0d;
        double wwhfoyes = 1.0d;
        double feattgat = 1.0d;
        double mnrkmcev = 1.0d;
        double epzzgqvm = 1.0d;

        when(ss.getMin()).thenReturn(apple);
        when(ss.getMax()).thenReturn(kiwi);
        when(ss.getMean()).thenReturn(cherry);
        when(ss.getMedian()).thenReturn(fig);
        when(ss.get75thPercentile()).thenReturn(ubachvff);
        when(ss.get95thPercentile()).thenReturn(wwhfoyes);
        when(ss.get98thPercentile()).thenReturn(feattgat);
        when(ss.get99thPercentile()).thenReturn(mnrkmcev);
        when(ss.get999thPercentile()).thenReturn(epzzgqvm);

        String actual = metricsHelper.printSnapshot(name, count, ss, divisor);

        String line = System.lineSeparator();
        StringBuilder e = new StringBuilder();
        e.append("metrics snapshot: Foo");
        e.append(line);
        e.append("count: 1");
        e.append(line);
        e.append("min: 1.00");
        e.append(line);
        e.append("max: 1.00");
        e.append(line);
        e.append("mean: 1.00");
        e.append(line);
        e.append("median: 1.00");
        e.append(line);
        e.append("75p: 1.00");
        e.append(line);
        e.append("95p: 1.00");
        e.append(line);
        e.append("98p: 1.00");
        e.append(line);
        e.append("99p: 1.00");
        e.append(line);
        e.append("99.9p: 1.00");
        e.append(line);

        assertEquals(e.toString(), actual);
    }
}
