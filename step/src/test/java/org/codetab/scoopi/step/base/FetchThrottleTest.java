package org.codetab.scoopi.step.base;

import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.codetab.scoopi.config.Configs;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.util.concurrent.Uninterruptibles;

public class FetchThrottleTest {

    @Mock
    private Configs configs;

    @InjectMocks
    private FetchThrottle fetchThrottle;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test() throws InterruptedException {
        int delay = 1000;
        when(configs.getInt("scoopi.loader.fetchDelay", "1000"))
                .thenReturn(delay);
        fetchThrottle.init();

        Thread t = new Thread(() -> {
            for (int c = 0; c < 3; c++) {
                fetchThrottle.acquirePermit();
                System.out.println("t0 " + new Date());
                Uninterruptibles.sleepUninterruptibly(10,
                        TimeUnit.MILLISECONDS);
                fetchThrottle.releasePermit();
            }
        });

        Thread t1 = new Thread(() -> {
            for (int c = 0; c < 5; c++) {
                fetchThrottle.acquirePermit();
                System.out.println("t1 " + new Date());
                Uninterruptibles.sleepUninterruptibly(10,
                        TimeUnit.MILLISECONDS);
                fetchThrottle.releasePermit();
            }
        });

        t.start();
        t1.start();

        t.join();
        t1.join();
    }

}
