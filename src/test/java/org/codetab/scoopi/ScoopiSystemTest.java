package org.codetab.scoopi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codetab.scoopi.defs.ILocatorDef;
import org.codetab.scoopi.defs.yml.Def;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.InvalidDefException;
import org.codetab.scoopi.helper.SystemHelper;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.metrics.MetricsServer;
import org.codetab.scoopi.metrics.SystemStat;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.factory.PayloadFactory;
import org.codetab.scoopi.step.TaskMediator;
import org.codetab.scoopi.system.ConfigService;
import org.codetab.scoopi.system.ErrorLogger;
import org.codetab.scoopi.system.ShutdownHook;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

public class ScoopiSystemTest {

    @Mock
    private ConfigService configService;
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
    private ScoopiSystem sSystem;

    private static ObjectFactory factory;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass() {
        factory = new ObjectFactory();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testStartErrorLogger() {
        boolean result = sSystem.startErrorLogger();

        assertThat(result).isTrue();
        verify(errorLogger).start();
    }

    @Test
    public void testAddShutdownHook() {
        boolean result = sSystem.addShutdownHook();

        assertThat(result).isTrue();
        verify(runTime).addShutdownHook(shutdownHook);
    }

    @Test
    public void testInitConfigService() {
        String defaultConfigFile = "a.xml";
        String userConfigFile = "b.properties";
        boolean result =
                sSystem.initConfigService(defaultConfigFile, userConfigFile);

        assertThat(result).isTrue();
        verify(configService).init(userConfigFile, defaultConfigFile);
    }

    @Test
    public void testInitDefsProvider()
            throws DefNotFoundException, InvalidDefException {
        boolean result = sSystem.initDefs();

        assertThat(result).isTrue();

        InOrder inOrder = inOrder(def);

        inOrder.verify(def).init();
        inOrder.verify(def).initDefProviders();
        verifyNoMoreInteractions(def);
    }

    @Test
    public void testStartMetricsServer() {
        boolean result = sSystem.startMetricsServer();

        assertThat(result).isTrue();
        InOrder inOrder = inOrder(metricsServer, metricsHelper);

        inOrder.verify(metricsServer).start();
        inOrder.verify(metricsHelper).initMetrics();
        inOrder.verify(metricsHelper).registerGuage(systemStat, sSystem,
                "system", "stats");
        verifyNoMoreInteractions(metricsServer, metricsHelper);
    }

    @Test
    public void testStopMetricsServer() {
        boolean result = sSystem.stopMetricsServer();

        assertThat(result).isTrue();
        InOrder inOrder = inOrder(metricsServer);

        inOrder.verify(metricsServer).stop();
        verifyNoMoreInteractions(metricsServer);
    }

    @Test
    public void testSeedLocatorGroups()
            throws ConfigNotFoundException, InterruptedException {
        String stepName = "start";
        String seederClzName = "seeder.class";

        List<LocatorGroup> lGroups = getTestLocatorGroups();

        Payload payload1 = Mockito.mock(Payload.class);
        Payload payload2 = Mockito.mock(Payload.class);
        List<Payload> payloads = Lists.newArrayList(payload1, payload2);

        given(configService.getConfig("scoopi.seederClass"))
                .willReturn(seederClzName);
        given(locatorDef.getLocatorGroups()).willReturn(lGroups);
        given(payloadFactory.createSeedPayloads(lGroups, stepName,
                seederClzName)).willReturn(payloads);

        boolean result = sSystem.seedLocatorGroups();

        assertThat(result).isTrue();

        InOrder inOrder = inOrder(taskMediator);

        inOrder.verify(taskMediator).pushPayload(payload1);
        inOrder.verify(taskMediator).pushPayload(payload2);
        verifyNoMoreInteractions(taskMediator);
    }

    @Test
    public void testSeedLocatorGroupsInterrupted()
            throws ConfigNotFoundException, InterruptedException {
        String stepName = "start";
        String seederClzName = "seeder.class";

        List<LocatorGroup> lGroups = getTestLocatorGroups();

        JobInfo jobInfo1 = factory.createJobInfo(0, "acme", "group1", "task1",
                "steps", "def1");
        JobInfo jobInfo2 = factory.createJobInfo(0, "acme", "group2", "task2",
                "steps", "def2");
        Payload payload1 = factory.createPayload(jobInfo1, null, null);
        Payload payload2 = factory.createPayload(jobInfo2, null, null);
        List<Payload> payloads = Lists.newArrayList(payload1, payload2);

        given(configService.getConfig("scoopi.seederClass"))
                .willReturn(seederClzName);
        given(locatorDef.getLocatorGroups()).willReturn(lGroups);
        given(payloadFactory.createSeedPayloads(lGroups, stepName,
                seederClzName)).willReturn(payloads);

        given(taskMediator.pushPayload(payload1))
                .willThrow(InterruptedException.class);

        boolean result = sSystem.seedLocatorGroups();

        assertThat(result).isTrue();

        InOrder inOrder = inOrder(taskMediator, errorLogger);

        inOrder.verify(taskMediator).pushPayload(payload1);
        inOrder.verify(errorLogger).log(eq(CAT.INTERNAL), any(String.class),
                any(InterruptedException.class));
        inOrder.verify(taskMediator).pushPayload(payload2);

        verifyNoMoreInteractions(taskMediator, errorLogger);
    }

    @Test
    public void testSeedLocatorGroupsThrowsException()
            throws ConfigNotFoundException, InterruptedException {

        given(configService.getConfig("scoopi.seederClass"))
                .willThrow(ConfigNotFoundException.class);

        testRule.expect(CriticalException.class);
        sSystem.seedLocatorGroups();
    }

    @Test
    public void testGetPropertyFileName() throws IOException {
        String propFile = "scoopi.properties";
        String devPropFile = "scoopi-dev.properties";

        System.setProperty("scoopi.propertyFile", propFile);
        String result = sSystem.getPropertyFileName();
        assertThat(result).isEqualTo(propFile);

        System.clearProperty("scoopi.propertyFile");
        System.setProperty("scoopi.mode", "dev");
        result = sSystem.getPropertyFileName();
        assertThat(result).isEqualTo(devPropFile);

        // can't test env - skipped
        System.clearProperty("scoopi.mode");
        result = sSystem.getPropertyFileName();
        assertThat(result).isEqualTo(propFile);
    }

    @Test
    public void testGetModeInfo() {
        given(configService.isTestMode()).willReturn(true);
        assertThat(sSystem.getModeInfo()).isEqualTo("mode: test");

        given(configService.isDevMode()).willReturn(true);
        assertThat(sSystem.getModeInfo()).isEqualTo("mode: dev");

        given(configService.isTestMode()).willReturn(false);
        given(configService.isDevMode()).willReturn(false);
        assertThat(sSystem.getModeInfo()).isEqualTo("mode: production");
    }

    @Test
    public void testWaitForInput() throws ConfigNotFoundException {
        given(configService.getConfig("scoopi.wait")).willReturn("false")
                .willThrow(ConfigNotFoundException.class).willReturn("true");
        sSystem.waitForInput();
        sSystem.waitForInput();

        sSystem.waitForInput();
        verify(systemHelper).gc();
        verify(systemHelper).readLine();
    }

    public List<LocatorGroup> getTestLocatorGroups() {
        ObjectFactory mf = new ObjectFactory();

        List<LocatorGroup> lGroups = new ArrayList<>();

        Locator l = mf.createLocator("l1", "lg1", "url1");
        LocatorGroup lg = mf.createLocatorGroup("lg1");
        lg.getLocators().add(l);
        lGroups.add(lg);

        l = mf.createLocator("l2", "lg2", "url2");
        l.setName("l2");
        lg = mf.createLocatorGroup("lg2");
        lg.getLocators().add(l);
        lGroups.add(lg);

        return lGroups;
    }

}
