package org.codetab.scoopi.metrics;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.codahale.metrics.Counter;

public class ErrorsTest {
    @InjectMocks
    private Errors errors;

    @Mock
    private MetricsHelper metricsHelper;
    @Mock
    private Counter counter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testStart() {
        Counter c = Mockito.mock(Counter.class);

        when(c.getCount()).thenReturn(20L);
        when(metricsHelper.getCounter(this, "system", "error")).thenReturn(c);
        errors.start();

        assertEquals(c.getCount(), 20L);
    }

    @Test
    public void testInc() {
        errors.inc();

        verify(counter).inc();
    }

    @Test
    public void testGetCount() {
        long apple = 1L;

        when(counter.getCount()).thenReturn(apple);

        long actual = errors.getCount();

        assertEquals(apple, actual);
    }
}
