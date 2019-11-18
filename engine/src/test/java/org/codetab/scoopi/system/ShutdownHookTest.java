package org.codetab.scoopi.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;

import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.pool.WebDriverPool;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * <p>
 * ShutdownHook tests.
 * @author Maithilish
 *
 */
public class ShutdownHookTest {

    @Mock
    private Stats stats;
    @Mock
    private WebDriverPool webDriverPool;

    @InjectMocks
    private ShutdownHook shutdownHook;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testStart() throws InterruptedException {
        shutdownHook.start();
        shutdownHook.join();

        InOrder inOrder = inOrder(stats, webDriverPool);
        inOrder.verify(stats).outputStats();
        inOrder.verify(stats).outputMemStats();
        inOrder.verify(webDriverPool).close();
    }

    @Test
    public void testShutdownHook() {
        assertThat(shutdownHook).isInstanceOf(Thread.class);
    }

    @Test
    public void testShutdownHookSingleton() {
        DInjector dInjector = new DInjector();
        assertThat(dInjector.instance(ShutdownHook.class))
                .isSameAs(dInjector.instance(ShutdownHook.class));
    }

}
