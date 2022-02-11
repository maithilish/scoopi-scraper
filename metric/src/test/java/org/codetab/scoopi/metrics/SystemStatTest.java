package org.codetab.scoopi.metrics;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class SystemStatTest {

    private static final long MB_DIVISOR = 1048576; // in MB

    @InjectMocks
    private SystemStat systemStat;

    @Spy
    private Runtime runtime;
    @Mock
    private OperatingSystemMXBean osMx;
    @Mock
    private RuntimeMXBean rtMx;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetMaxMemory() {
        long apple = Runtime.getRuntime().maxMemory() / MB_DIVISOR;

        long actual = systemStat.getMaxMemory();

        assertEquals(apple, actual);
    }

    @Test
    public void testGetFreeMemory() {
        long apple = Runtime.getRuntime().freeMemory() / MB_DIVISOR;

        long actual = systemStat.getFreeMemory();

        assertEquals(apple, actual);
    }

    @Test
    public void testGetTotalMemory() {
        long apple = Runtime.getRuntime().totalMemory() / MB_DIVISOR;

        long actual = systemStat.getTotalMemory();

        assertEquals(apple, actual);
    }

    @Test
    public void testGetSystemLoad() {
        double apple = 1.0d;

        when(osMx.getSystemLoadAverage()).thenReturn(apple);

        double actual = systemStat.getSystemLoad();

        assertEquals(apple, actual, 0);
    }

    @Test
    public void testGetUptime() {
        long apple = 1L;
        String grape = DurationFormatUtils.formatDuration(apple, "H:m:s");

        when(rtMx.getUptime()).thenReturn(apple);

        String actual = systemStat.getUptime();

        assertEquals(grape, actual);
    }
}
