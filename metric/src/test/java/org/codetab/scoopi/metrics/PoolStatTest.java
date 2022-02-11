package org.codetab.scoopi.metrics;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class PoolStatTest {
    @InjectMocks
    private PoolStat poolStat;

    @Mock
    private ThreadPoolExecutor threadPool;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSetThreadPool() {
        ThreadPoolExecutor thPool = Mockito.mock(ThreadPoolExecutor.class);
        poolStat.setThreadPool(thPool);

        int apple = 5;
        when(thPool.getActiveCount()).thenReturn(apple);
        int actual = poolStat.getActiveCount();

        assertEquals(apple, actual);
    }

    @Test
    public void testGetActiveCount() {
        int apple = 2;

        when(threadPool.getActiveCount()).thenReturn(apple);

        int actual = poolStat.getActiveCount();

        assertEquals(apple, actual);
    }

    @Test
    public void testGetPoolSize() {
        int apple = 3;

        when(threadPool.getPoolSize()).thenReturn(apple);

        int actual = poolStat.getPoolSize();

        assertEquals(apple, actual);
    }

    @Test
    public void testGetCompletedTaskCount() {
        long apple = 4L;

        when(threadPool.getCompletedTaskCount()).thenReturn(apple);

        long actual = poolStat.getCompletedTaskCount();

        assertEquals(apple, actual);
    }

    @Test
    public void testGetTaskCount() {
        long apple = 5L;

        when(threadPool.getTaskCount()).thenReturn(apple);

        long actual = poolStat.getTaskCount();

        assertEquals(apple, actual);
    }
}
