package org.codetab.scoopi.metrics.serialize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class GaugeTest {
    @InjectMocks
    private Gauge gauge;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSetValue() {
        Map<String, Object> value = new HashMap<>();
        gauge.setValue(value);

        Map<String, Object> actual = gauge.getValue();

        assertEquals(value, actual);
    }

    @Test
    public void testAggregate() {
        Gauge other = Mockito.mock(Gauge.class);

        String activeCountKey = "activeCount";
        String poolSizeKey = "poolSize";
        String completedTaskCountKey = "completedTaskCount";
        String taskCountKey = "taskCount";
        String uptimeKey = "uptime";
        String systemLoadKey = "systemLoad";
        String freeMemoryKey = "freeMemory";
        String totalMemoryKey = "totalMemory";
        String maxMemoryKey = "maxMemory";

        Map<String, Object> value = new HashMap<>();
        gauge.setValue(value);
        value.put(activeCountKey, 1);
        value.put(poolSizeKey, 2);
        value.put(completedTaskCountKey, 3);
        value.put(taskCountKey, 4);
        value.put(uptimeKey, "01:02:03"); // h:m:s
        value.put(systemLoadKey, 5.0);
        value.put(freeMemoryKey, 6);
        value.put(totalMemoryKey, 7);
        value.put(maxMemoryKey, 8);

        Map<String, Object> apple = new HashMap<>();
        apple.put(activeCountKey, 11);
        apple.put(poolSizeKey, 12);
        apple.put(completedTaskCountKey, 13);
        apple.put(taskCountKey, 14);
        apple.put(uptimeKey, "11:02:03"); // h:m:s
        apple.put(systemLoadKey, 15.0);
        apple.put(freeMemoryKey, 16);
        apple.put(totalMemoryKey, 17);
        apple.put(maxMemoryKey, 18);

        when(other.getValue()).thenReturn(apple);

        gauge.aggregate(other);

        assertEquals(12, value.get(activeCountKey));
        assertEquals(14, value.get(poolSizeKey));
        assertEquals(16, value.get(completedTaskCountKey));
        assertEquals(18, value.get(taskCountKey));
        assertEquals("11:02:03", value.get(uptimeKey));
        assertEquals(10.0, value.get(systemLoadKey));
        assertEquals(22, value.get(freeMemoryKey));
        assertEquals(24, value.get(totalMemoryKey));
        assertEquals(26, value.get(maxMemoryKey));
    }

    @Test
    public void testAggregateEmptyGauge() {
        Gauge other = Mockito.mock(Gauge.class);

        String activeCountKey = "activeCount";
        String poolSizeKey = "poolSize";
        String completedTaskCountKey = "completedTaskCount";
        String taskCountKey = "taskCount";
        String uptimeKey = "uptime";
        String systemLoadKey = "systemLoad";
        String freeMemoryKey = "freeMemory";
        String totalMemoryKey = "totalMemory";
        String maxMemoryKey = "maxMemory";

        Map<String, Object> value = new HashMap<>(); // gauge is empty
        gauge.setValue(value);

        Map<String, Object> apple = new HashMap<>();
        apple.put(activeCountKey, 11);
        apple.put(poolSizeKey, 12);
        apple.put(completedTaskCountKey, 13);
        apple.put(taskCountKey, 14);
        apple.put(uptimeKey, "10:02:03"); // h:m:s
        apple.put(systemLoadKey, 15.0);
        apple.put(freeMemoryKey, 16);
        apple.put(totalMemoryKey, 17);
        apple.put(maxMemoryKey, 18);

        when(other.getValue()).thenReturn(apple);

        gauge.aggregate(other);

        assertEquals(11, value.get(activeCountKey));
        assertEquals(12, value.get(poolSizeKey));
        assertEquals(13, value.get(completedTaskCountKey));
        assertEquals(14, value.get(taskCountKey));
        assertEquals("10:02:03", value.get(uptimeKey));
        assertEquals(15.0, value.get(systemLoadKey));
        assertEquals(16, value.get(freeMemoryKey));
        assertEquals(17, value.get(totalMemoryKey));
        assertEquals(18, value.get(maxMemoryKey));
    }

    @Test
    public void testAggregateUptimeLessThan() {
        Gauge other = Mockito.mock(Gauge.class);

        String uptimeKey = "uptime";

        Map<String, Object> value = new HashMap<>();
        gauge.setValue(value);
        value.put(uptimeKey, "00:05:03"); // h:m:s

        Map<String, Object> apple = new HashMap<>();
        apple.put(uptimeKey, "00:02:03"); // h:m:s

        when(other.getValue()).thenReturn(apple);

        gauge.aggregate(other);

        assertEquals("00:05:03", value.get(uptimeKey));
    }

    @Test
    public void testAggregateUptimeParseException() {
        Gauge other = Mockito.mock(Gauge.class);

        String uptimeKey = "uptime";

        Map<String, Object> value = new HashMap<>();
        gauge.setValue(value);
        value.put(uptimeKey, "00.05.03"); // h:m:s

        Map<String, Object> apple = new HashMap<>();
        apple.put(uptimeKey, "00.02.03"); // h:m:s

        when(other.getValue()).thenReturn(apple);

        assertThrows(IllegalArgumentException.class,
                () -> gauge.aggregate(other));
    }

    @Test
    public void testAggregateIllegalKey() {
        Gauge other = Mockito.mock(Gauge.class);

        String invalidKey = "invalid";

        Map<String, Object> value = new HashMap<>();
        gauge.setValue(value);
        value.put(invalidKey, 1);

        Map<String, Object> apple = new HashMap<>();
        apple.put(invalidKey, 2);

        when(other.getValue()).thenReturn(apple);

        assertThrows(IllegalStateException.class, () -> gauge.aggregate(other));
    }
}
