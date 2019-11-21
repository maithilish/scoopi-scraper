package org.codetab.scoopi.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class SystemStatTest {

    @Spy
    private Runtime runtime; // can't mock runtime
    @Mock
    private OperatingSystemMXBean osMx;
    @Mock
    private RuntimeMXBean rtMx;

    @InjectMocks
    private SystemStat systemStat;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetMaxMemory() {
        long expected = Runtime.getRuntime().maxMemory() / 1048576;
        long actual = systemStat.getMaxMemory();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetFreeMemory() {
        // can't mock runtime, for coverage
        systemStat.getFreeMemory();
    }

    @Test
    public void testGetTotalMemory() {
        // can't mock runtime, for coverage
        systemStat.getTotalMemory();
    }

    @Test
    public void testGetSystemLoad() {
        given(osMx.getSystemLoadAverage()).willReturn(10.2);
        assertThat(systemStat.getSystemLoad()).isEqualTo(10.2);

    }

    @Test
    public void testGetUptime() {
        given(rtMx.getUptime()).willReturn(11500000L);
        assertThat(systemStat.getUptime()).isEqualTo("3:11:40");
    }
}
