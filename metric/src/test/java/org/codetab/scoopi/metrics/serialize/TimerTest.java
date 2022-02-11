package org.codetab.scoopi.metrics.serialize;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class TimerTest {
    @InjectMocks
    private Timer timer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAggregate() {

        timer.count = 1;
        timer.m15_rate = 3;
        timer.m1_rate = 4;
        timer.m5_rate = 5;
        timer.max = 6;
        timer.mean = 7;
        timer.mean_rate = 8;
        timer.min = 9;
        timer.p50 = 10;
        timer.p75 = 11;
        timer.p95 = 12;
        timer.p98 = 13;
        timer.p99 = 14;
        timer.p999 = 15;
        timer.stddev = 16;

        Timer other = new Timer();

        other.count = 11;
        other.m15_rate = 13;
        other.m1_rate = 14;
        other.m5_rate = 15;
        other.max = 16;
        other.mean = 17;
        other.mean_rate = 18;
        other.min = 19;
        other.p50 = 20;
        other.p75 = 21;
        other.p95 = 22;
        other.p98 = 23;
        other.p99 = 24;
        other.p999 = 25;
        other.stddev = 26;

        timer.aggregate(other);

        assertEquals(12, timer.count);
        assertEquals(8, timer.m15_rate, 0);
        assertEquals(9, timer.m1_rate, 0);
        assertEquals(10, timer.m5_rate, 0);
        assertEquals(16, timer.max, 0);
        assertEquals(11.782608695652174, timer.mean, 0);
        assertEquals(13, timer.mean_rate, 0);
        assertEquals(9, timer.min, 0);
        assertEquals(15, timer.p50, 0);
        assertEquals(16, timer.p75, 0);
        assertEquals(17, timer.p95, 0);
        assertEquals(18, timer.p98, 0);
        assertEquals(19, timer.p99, 0);
        assertEquals(20, timer.p999, 0);
        assertEquals(16, timer.stddev, 0);
    }

    @Test
    public void testAggregateMaxMin() {

        timer.max = 20;
        timer.min = 30;

        Timer other = new Timer();
        other.max = 15;
        other.min = 16;

        timer.aggregate(other);

        assertEquals(20, timer.max, 0);
        assertEquals(16, timer.min, 0);
    }
}
