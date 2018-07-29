package org.codetab.scoopi.pool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PoolStatTest {

    @Mock
    private ThreadPoolExecutor threadPool;

    @InjectMocks
    private PoolStat poolStat;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        poolStat.setThreadPool(threadPool);
    }

    @Test
    public void testGetActiveCount() {
        given(threadPool.getActiveCount()).willReturn(10);
        assertThat(poolStat.getActiveCount()).isEqualTo(10);
    }

    @Test
    public void testGetPoolSize() {
        given(threadPool.getPoolSize()).willReturn(5);
        assertThat(poolStat.getPoolSize()).isEqualTo(5);
    }

    @Test
    public void testGetCompletedTaskCount() {
        given(threadPool.getCompletedTaskCount()).willReturn(13L);
        assertThat(poolStat.getCompletedTaskCount()).isEqualTo(13L);
    }

    @Test
    public void testGetTaskCount() {
        given(threadPool.getTaskCount()).willReturn(14L);
        assertThat(poolStat.getTaskCount()).isEqualTo(14L);
    }
}
