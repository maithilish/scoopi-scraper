package org.codetab.scoopi.step.pool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.metrics.PoolStat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * <p>
 * for coverage.
 * @author Maithilish
 *
 */
public class TaskPoolServiceTest {

    @Mock
    private Configs configs;
    @Mock
    private MetricsHelper metricsHelper;
    @Mock
    private DInjector dInjector;
    @Mock
    private PoolStat poolStat;

    @InjectMocks
    private TaskPoolService pools;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSubmit() {
        String poolName = "x";
        Runnable task = () -> {
        };

        given(dInjector.instance(PoolStat.class)).willReturn(poolStat);

        boolean actual = pools.submit(poolName, task);

        assertThat(actual).isTrue();
    }
}
