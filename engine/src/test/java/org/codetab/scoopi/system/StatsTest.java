package org.codetab.scoopi.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Timer;

import org.apache.commons.lang3.time.StopWatch;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.di.InitModule;
import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.log.Log;
import org.codetab.scoopi.metrics.SystemStat;
import org.codetab.scoopi.stat.MemoryTask;
import org.codetab.scoopi.stat.Stats;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class StatsTest {

    @Mock
    private Timer timer;
    @Mock
    private StopWatch stopWatch;
    @Mock
    private MemoryTask memoryTask;

    @Mock
    private List<Log> logs;
    @Mock
    private LongSummaryStatistics totalMem;
    @Mock
    private LongSummaryStatistics freeMem;
    @Mock
    private SystemStat systemStat;
    @Mock
    private ErrorLogger errorLogger;

    @InjectMocks
    private Stats stats;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSingleton() {
        DInjector dInjector =
                new DInjector(new InitModule()).instance(DInjector.class);

        Stats instanceA = dInjector.instance(Stats.class);
        Stats instanceB = dInjector.instance(Stats.class);

        assertThat(instanceA).isNotNull();
        assertThat(instanceA).isSameAs(instanceB);
    }

    @Test
    public void testStart() {
        // when
        stats.start();

        // then
        verify(stopWatch).start();
        verify(timer).schedule(memoryTask, 0L, 5000L);
    }

    @Test
    public void testEnd() {
        // when
        stats.stop();

        // then
        verify(stopWatch).stop();
        verify(timer).cancel();
    }

    @Test
    public void testCollectMemoryStat() {

        long tm = 10;
        long fm = 20;

        given(systemStat.getTotalMemory()).willReturn(tm);
        given(systemStat.getFreeMemory()).willReturn(fm);

        // when
        stats.collectMemStats();

        // then
        verify(totalMem).accept(tm);
        verify(freeMem).accept(fm);
    }

    // for coverage
    @Test
    public void testOutputLog() throws IllegalAccessException {
        given(errorLogger.getErrorCount()).willReturn(0L, 1L);
        stats.outputStats(); // size zero
        stats.outputStats();
    }

    @Test
    public void testLogMemoryUsage() {
        stats.collectMemStats();
        stats.outputMemStats();
    }
}
