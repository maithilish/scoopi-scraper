package org.codetab.scoopi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.ILocatorDef;
import org.codetab.scoopi.defs.yml.Def;
import org.codetab.scoopi.engine.SystemModule;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.helper.SystemHelper;
import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.metrics.SystemStat;
import org.codetab.scoopi.metrics.server.MetricsServer;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.stat.ShutdownHook;
import org.codetab.scoopi.step.PayloadFactory;
import org.codetab.scoopi.step.TaskMediator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ScoopiSystemTest {

    @Mock
    private Configs configs;
    @Mock
    private Def def;
    // @Inject
    // private DataDefService dataDefService;
    @Mock
    private TaskMediator taskMediator;
    @Mock
    private ILocatorDef locatorDef;
    @Mock
    private MetricsServer metricsServer;
    @Mock
    private MetricsHelper metricsHelper;
    @Mock
    private ErrorLogger errorLogger;
    @Mock
    private ShutdownHook shutdownHook;
    @Mock
    private Runtime runTime;
    @Mock
    private SystemStat systemStat;
    @Mock
    private SystemHelper systemHelper;
    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private PayloadFactory payloadFactory;

    @InjectMocks
    private SystemModule sSystem;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass() {
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testStartErrorLogger() {
        final boolean result = sSystem.startErrorLogger();

        assertThat(result).isTrue();
        verify(errorLogger).start();
    }

    @Test
    public void testAddShutdownHook() {
        final boolean result = sSystem.addShutdownHook();

        assertThat(result).isTrue();
        verify(runTime).addShutdownHook(shutdownHook);
    }

    @Test
    public void testStartMetricsServer() {
        final boolean result = sSystem.startMetrics();

        assertThat(result).isTrue();
        final InOrder inOrder = inOrder(metricsServer, metricsHelper);

        inOrder.verify(metricsServer).start();
        inOrder.verify(metricsHelper).initMetrics();
        inOrder.verify(metricsHelper).registerGuage(systemStat, sSystem,
                "system", "stats");
        verifyNoMoreInteractions(metricsServer, metricsHelper);
    }

    @Test
    public void testStopMetricsServer() {
        final boolean result = sSystem.stopMetrics();

        assertThat(result).isTrue();
        final InOrder inOrder = inOrder(metricsServer);

        inOrder.verify(metricsServer).stop();
        verifyNoMoreInteractions(metricsServer);
    }

    @Test
    public void testWaitForInput() throws ConfigNotFoundException, IOException {
        given(configs.getConfig("scoopi.wait")).willReturn("false")
                .willThrow(ConfigNotFoundException.class).willReturn("true");
        sSystem.waitForInput();
        sSystem.waitForInput();

        sSystem.waitForInput();
        verify(systemHelper).gc();
        verify(systemHelper).readLine();
    }

}
