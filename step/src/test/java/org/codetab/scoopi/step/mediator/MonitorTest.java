package org.codetab.scoopi.step.mediator;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.codetab.scoopi.config.Configs;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MonitorTest {
    @InjectMocks
    private Monitor monitor;

    @Mock
    private Configs configs;
    @Mock
    private StateFliper stateFliper;
    @Mock
    private Factory factory;
    @Mock
    private ScheduledExecutorService scheduler;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testStart() {

        int initialDelay = 1;
        int delay = 1;

        when(factory.newSingleThreadScheduledExecutor()).thenReturn(scheduler);
        when(configs.getInt("scoopi.monitor.timerPeriod", "1000"))
                .thenReturn(delay);
        monitor.start();

        verify(scheduler).scheduleWithFixedDelay(monitor, initialDelay, delay,
                TimeUnit.MILLISECONDS);
    }

    @Test
    public void testStop() throws Exception {
        int timeout = 1000;
        monitor.stop();

        verify(scheduler).shutdownNow();
        verify(scheduler).awaitTermination(timeout, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testRun() {
        monitor.run();

        verify(stateFliper).tryTMShutdown();
    }
}
