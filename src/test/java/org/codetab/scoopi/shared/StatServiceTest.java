package org.codetab.scoopi.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Timer;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.misc.MemoryTask;
import org.codetab.scoopi.model.Log;
import org.codetab.scoopi.model.Log.CAT;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class StatServiceTest {

    @Mock
    private Timer timer;
    @Mock
    private StopWatch stopWatch;
    @Mock
    private MemoryTask memoryTask;

    @Mock
    private List<Log> logs;
    @Mock
    private LongSummaryStatistics totalMemory;
    @Mock
    private LongSummaryStatistics freeMemory;
    @Spy
    private Runtime runtime;

    @InjectMocks
    private StatService statService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSingleton() {
        DInjector dInjector = new DInjector().instance(DInjector.class);

        StatService instanceA = dInjector.instance(StatService.class);
        StatService instanceB = dInjector.instance(StatService.class);

        assertThat(instanceA).isNotNull();
        assertThat(instanceA).isSameAs(instanceB);
    }

    @Test
    public void testStart() {
        // when
        statService.start();

        // then
        verify(stopWatch).start();
        verify(timer).schedule(memoryTask, 0L, 5000L);
    }

    @Test
    public void testEnd() {
        // when
        statService.stop();

        // then
        verify(stopWatch).stop();
        verify(timer).cancel();
    }

    @Test
    public void testAddLog() throws IllegalAccessException {
        // when
        statService.log(CAT.ERROR, "tmessage");

        // then
        ArgumentCaptor<Log> argument = ArgumentCaptor.forClass(Log.class);
        verify(logs).add(argument.capture());
        assertThat(argument.getValue().getCat()).isEqualTo(CAT.ERROR);
        assertThat(argument.getValue().getMessage()).isEqualTo("tmessage");
        assertThat(argument.getValue().getThrowable()).isNull();
    }

    @Test
    public void testAddLogWithLabel() throws IllegalAccessException {

        // when
        statService.log(CAT.ERROR, "tlabel", "tmessage");

        // then
        ArgumentCaptor<Log> argument = ArgumentCaptor.forClass(Log.class);
        verify(logs).add(argument.capture());
        assertThat(argument.getValue().getCat()).isEqualTo(CAT.ERROR);
        assertThat(argument.getValue().getMessage()).isEqualTo("tmessage");
        assertThat(argument.getValue().getLabel()).isEqualTo("tlabel");
        assertThat(argument.getValue().getThrowable()).isNull();
    }

    @Test
    public void testAddLogWithThrowable() throws IllegalAccessException {
        // given
        Throwable throwable = new Throwable("foo");

        // when
        statService.log(CAT.ERROR, "tmessage", throwable); // when

        // then
        ArgumentCaptor<Log> argument = ArgumentCaptor.forClass(Log.class);
        verify(logs).add(argument.capture());
        assertThat(argument.getValue().getCat()).isEqualTo(CAT.ERROR);
        assertThat(argument.getValue().getMessage()).isEqualTo("tmessage");
        assertThat(argument.getValue().getThrowable()).isSameAs(throwable);
    }

    @Test
    public void testAddLogWithThrowableAndLabel()
            throws IllegalAccessException {
        // given
        Throwable throwable = new Throwable("foo");

        // when
        statService.log(CAT.ERROR, "tlabel", "tmessage", throwable); // when

        // then
        ArgumentCaptor<Log> argument = ArgumentCaptor.forClass(Log.class);
        verify(logs).add(argument.capture());
        assertThat(argument.getValue().getCat()).isEqualTo(CAT.ERROR);
        assertThat(argument.getValue().getMessage()).isEqualTo("tmessage");
        assertThat(argument.getValue().getLabel()).isEqualTo("tlabel");
        assertThat(argument.getValue().getThrowable()).isSameAs(throwable);
    }

    @Test
    public void testCollectMemoryStat() {

        long tm = runtime.totalMemory() / 1048576;
        long fm = runtime.freeMemory() / 1048576;

        // when
        statService.collectMemoryStat();

        // then
        verify(totalMemory).accept(tm);
        verify(freeMemory).accept(fm);
    }

    // for coverage
    @Test
    public void testOutputLog() throws IllegalAccessException {
        FieldUtils.writeDeclaredField(statService, "logs", new ArrayList<>(),
                true);
        statService.outputLog(); // size zero

        Throwable throwable = new Throwable("foo");
        statService.log(CAT.ERROR, "tlabel", "tmessage", throwable);
        throwable = new Throwable("foo", new NullPointerException());
        statService.log(CAT.ERROR, "tlabel1", "tmessage1", throwable);
        statService.log(CAT.INTERNAL, "tlabel2", "tmessage2");

        statService.outputLog();
    }

    @Test
    public void testLogMemoryUsage() {
        statService.collectMemoryStat();
        statService.logMemoryUsage();
    }
}
