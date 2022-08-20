package org.codetab.scoopi.step.parse.cache;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.codetab.scoopi.metrics.MetricsHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;

public class ParserCacheTest {
    @InjectMocks
    private ParserCache parserCache;

    @Mock
    private MetricsHelper metricsHelper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetIfIsNull() {
        int key = 1;
        String value = null;
        Counter counter = Mockito.mock(Counter.class);
        Counter counter2 = Mockito.mock(Counter.class);

        when(metricsHelper.getCounter(parserCache, "parser", "cache", "miss"))
                .thenReturn(counter);

        String actual = parserCache.get(key);

        assertEquals(value, actual);
        verify(counter).inc();
        verify(metricsHelper, never()).getCounter(this, "parser", "cache",
                "hit");
        verify(counter2, never()).inc();
    }

    @Test
    public void testGetElseIsNull() {
        int key = 1;
        String value = "Foo";
        Counter counter = Mockito.mock(Counter.class);
        Counter counter2 = Mockito.mock(Counter.class);
        Meter meter = Mockito.mock(Meter.class);

        when(metricsHelper.getCounter(parserCache, "parser", "cache", "hit"))
                .thenReturn(counter2);
        when(metricsHelper.getMeter(parserCache, "parser", "cache"))
                .thenReturn(meter);
        parserCache.put(key, value);

        String actual = parserCache.get(key);

        assertEquals(value, actual);
        verify(metricsHelper, never()).getCounter(this, "parser", "cache",
                "miss");
        verify(counter, never()).inc();
        verify(counter2).inc();
    }

    @Test
    public void testPutIfNonNull() {
        int key = 1;
        String value = "Foo";
        Meter meter = Mockito.mock(Meter.class);

        when(metricsHelper.getMeter(parserCache, "parser", "cache"))
                .thenReturn(meter);
        parserCache.put(key, value);

        verify(meter).mark();
    }

    @Test
    public void testPutElseNonNull() {
        int key = 1;
        String value = null;
        Meter meter = Mockito.mock(Meter.class);
        parserCache.put(key, value);

        verify(metricsHelper, never()).getMeter(parserCache, "parser", "cache");
        verify(meter, never()).mark();
    }

    @Test
    public void testGetKey() {
        Map<String, String> map = new HashMap<>();
        int apple = Arrays.hashCode(map.values().toArray());

        int actual = parserCache.getKey(map);

        assertEquals(apple, actual);
    }
}
