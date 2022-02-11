package org.codetab.scoopi.metrics.serialize;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CounterTest {
    @InjectMocks
    private Counter counter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetCount() {
        long count = 1L;
        counter.setCount(count);
        long actual = counter.getCount();

        assertEquals(count, actual);
    }

    @Test
    public void testSetCount() {
        long count = 1L;
        counter.setCount(count);
    }

    @Test
    public void testAggregate() {
        long count = 1L;
        counter.setCount(count);

        Counter other = Mockito.mock(Counter.class);
        long otherCount = 2L;

        when(other.getCount()).thenReturn(otherCount);
        counter.aggregate(other);

        assertEquals(3, counter.getCount());
    }
}
