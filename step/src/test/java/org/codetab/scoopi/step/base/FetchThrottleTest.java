package org.codetab.scoopi.step.base;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.Semaphore;

import org.codetab.scoopi.config.Configs;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class FetchThrottleTest {
    @InjectMocks
    private FetchThrottle fetchThrottle;

    @Mock
    private Configs configs;
    @Mock
    private Semaphore semaphore;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInit() {
        long fetchDelay = 1L;
        int permits = 1;

        when(configs.getInt("scoopi.loader.fetch.delay", "1000"))
                .thenReturn((int) fetchDelay);
        when(configs.getInt("scoopi.loader.fetch.parallelism", "1"))
                .thenReturn(permits);
        fetchThrottle.init();
    }

    @Test
    public void testAcquirePermit() {
        fetchThrottle.acquirePermit();

        verify(semaphore).acquireUninterruptibly();
    }

    @Test
    public void testReleasePermit() {
        fetchThrottle.releasePermit();

        verify(semaphore).release();
    }
}
