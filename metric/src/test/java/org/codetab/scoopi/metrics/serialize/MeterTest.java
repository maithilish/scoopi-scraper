package org.codetab.scoopi.metrics.serialize;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class MeterTest {
    @InjectMocks
    private Meter meter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAggregate() {
        meter.count = 1;
        meter.m15_rate = 2;
        meter.m1_rate = 3;
        meter.m5_rate = 4;
        meter.mean_rate = 5;

        Meter other = new Meter();
        other.count = 11;
        other.m15_rate = 12;
        other.m1_rate = 13;
        other.m5_rate = 14;
        other.mean_rate = 15;

        meter.aggregate(other);

        assertEquals(12, meter.count);
        assertEquals(7.0, meter.m15_rate, 0);
        assertEquals(8, meter.m1_rate, 0);
        assertEquals(9.0, meter.m5_rate, 0);
        assertEquals(10.0, meter.mean_rate, 0);
    }
}
