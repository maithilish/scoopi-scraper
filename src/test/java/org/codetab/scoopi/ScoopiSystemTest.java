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

import org.codetab.scoopi.defs.ILocatorProvider;
import org.codetab.scoopi.defs.yml.DefsProvider;
import org.codetab.scoopi.di.BasicFactory;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.helper.SystemHelper;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.metrics.MetricsServer;
import org.codetab.scoopi.metrics.SystemStat;
import org.codetab.scoopi.misc.ShutdownHook;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.shared.ConfigService;
import org.codetab.scoopi.shared.StatService;
import org.codetab.scoopi.step.TaskMediator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ScoopiSystemTest {

    @Mock
    private ConfigService configService;
    @Mock
    private DefsProvider defsProvider;
    // @Inject
    // private DataDefService dataDefService;
    @Mock
    private TaskMediator taskMediator;
    @Mock
    private ILocatorProvider locatorProvider;
    @Mock
    private MetricsServer metricsServer;
    @Mock
    private MetricsHelper metricsHelper;
    @Mock
    private StatService statService;
    @Mock
    private ShutdownHook shutdownHook;
    @Mock
    private Runtime runTime;
    @Mock
    private SystemStat systemStat;
    @Mock
    private SystemHelper systemHelper;
    @Mock
    private BasicFactory factory;

    @InjectMocks
    private ScoopiSystem sSystem;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void teststartStatService() {
        boolean result = sSystem.startStatService();

        assertThat(result).isTrue();
        verify(statService).start();
    }

    @Test
    public void teststopStatService() {
        boolean result = sSystem.stopStatService();

        assertThat(result).isTrue();
        verify(statService).stop();
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
    public void testInitDefsProvider() {
        boolean result = sSystem.initDefsProvider();

        assertThat(result).isTrue();

        InOrder inOrder = inOrder(defsProvider);

        inOrder.verify(defsProvider).init();
        inOrder.verify(defsProvider).initProviders();
        verifyNoMoreInteractions(defsProvider);
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
    public void testPushInitialPayload()
            throws ConfigNotFoundException, InterruptedException {
        String stepName = "start";
        String seederClassName = "seeder.class";
        String undefined = "undefined";

        List<LocatorGroup> lGroups = getTestLocatorGroups();
        LocatorGroup lg1 = lGroups.get(0);
        LocatorGroup lg2 = lGroups.get(1);

        StepInfo stepInfo1 = Mockito.mock(StepInfo.class);
        JobInfo jobInfo1 = Mockito.mock(JobInfo.class);
        Payload payload1 = Mockito.mock(Payload.class);

        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        Payload payload2 = Mockito.mock(Payload.class);

        given(factory.getStepInfo(stepName, undefined, undefined,
                seederClassName)).willReturn(stepInfo1).willReturn(stepInfo2);
        given(factory.getJobInfo(0, undefined, lg1.getGroup(), undefined,
                undefined)).willReturn(jobInfo1);
        given(factory.getJobInfo(0, undefined, lg2.getGroup(), undefined,
                undefined)).willReturn(jobInfo2);
        given(factory.getPayload()).willReturn(payload1).willReturn(payload2);

        given(configService.getConfig("scoopi.seederClass"))
                .willReturn(seederClassName);
        given(locatorProvider.getLocatorGroups()).willReturn(lGroups);

        boolean result = sSystem.pushInitialPayload();

        assertThat(result).isTrue();

        InOrder inOrder = inOrder(payload1, payload2, taskMediator);

        inOrder.verify(payload1).setStepInfo(stepInfo1);
        inOrder.verify(payload1).setJobInfo(jobInfo1);
        inOrder.verify(payload1).setData(lg1);
        inOrder.verify(taskMediator).pushPayload(payload1);

        inOrder.verify(payload2).setStepInfo(stepInfo2);
        inOrder.verify(payload2).setJobInfo(jobInfo2);
        inOrder.verify(payload2).setData(lg2);
        inOrder.verify(taskMediator).pushPayload(payload2);
        verifyNoMoreInteractions(payload1, payload2, taskMediator);
    }

    @Test
    public void testPushInitialPayloadInterrupted()
            throws ConfigNotFoundException, InterruptedException {
        String stepName = "start";
        String seederClassName = "seeder.class";
        String undefined = "undefined";

        List<LocatorGroup> lGroups = getTestLocatorGroups();
        LocatorGroup lg1 = lGroups.get(0);
        LocatorGroup lg2 = lGroups.get(1);

        StepInfo stepInfo1 = Mockito.mock(StepInfo.class);
        JobInfo jobInfo1 = Mockito.mock(JobInfo.class);
        Payload payload1 = Mockito.mock(Payload.class);

        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        Payload payload2 = Mockito.mock(Payload.class);

        given(factory.getStepInfo(stepName, undefined, undefined,
                seederClassName)).willReturn(stepInfo1).willReturn(stepInfo2);
        given(factory.getJobInfo(0, undefined, lg1.getGroup(), undefined,
                undefined)).willReturn(jobInfo1);
        given(factory.getJobInfo(0, undefined, lg2.getGroup(), undefined,
                undefined)).willReturn(jobInfo2);
        given(factory.getPayload()).willReturn(payload1).willReturn(payload2);

        given(configService.getConfig("scoopi.seederClass"))
                .willReturn(seederClassName);
        given(locatorProvider.getLocatorGroups()).willReturn(lGroups);

        given(taskMediator.pushPayload(payload1))
                .willThrow(InterruptedException.class);

        boolean result = sSystem.pushInitialPayload();

        assertThat(result).isTrue();

        InOrder inOrder =
                inOrder(payload1, payload2, taskMediator, statService);

        inOrder.verify(payload1).setStepInfo(stepInfo1);
        inOrder.verify(payload1).setJobInfo(jobInfo1);
        inOrder.verify(payload1).setData(lg1);
        inOrder.verify(taskMediator).pushPayload(payload1);
        inOrder.verify(statService).log(eq(CAT.INTERNAL), any(String.class));

        inOrder.verify(payload2).setStepInfo(stepInfo2);
        inOrder.verify(payload2).setJobInfo(jobInfo2);
        inOrder.verify(payload2).setData(lg2);
        inOrder.verify(taskMediator).pushPayload(payload2);

        verifyNoMoreInteractions(payload1, payload2, taskMediator, statService);
    }

    @Test
    public void testPushInitialPayloadThrowsException()
            throws ConfigNotFoundException, InterruptedException {

        given(configService.getConfig("scoopi.seederClass"))
                .willThrow(ConfigNotFoundException.class);

        testRule.expect(CriticalException.class);
        sSystem.pushInitialPayload();
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
        assertThat(sSystem.getModeInfo()).isEqualTo("mode : Test");

        given(configService.isDevMode()).willReturn(true);
        assertThat(sSystem.getModeInfo()).isEqualTo("mode : Dev");

        given(configService.isTestMode()).willReturn(false);
        given(configService.isDevMode()).willReturn(false);
        assertThat(sSystem.getModeInfo()).isEqualTo("mode : Production");
    }

    @Test
    public void testWaitForHeapDump() throws ConfigNotFoundException {
        given(configService.getConfig("scoopi.waitForHeapDump"))
                .willReturn("false").willThrow(ConfigNotFoundException.class)
                .willReturn("true");
        sSystem.waitForHeapDump();
        sSystem.waitForHeapDump();

        sSystem.waitForHeapDump();
        verify(systemHelper).gc();
        verify(systemHelper).readLine();
    }

    public List<LocatorGroup> getTestLocatorGroups() {
        List<LocatorGroup> lGroups = new ArrayList<>();
        Locator l = new Locator();
        l.setName("l1");
        LocatorGroup lg = new LocatorGroup();
        lg.setGroup("lg1");
        lg.getLocators().add(l);
        lGroups.add(lg);

        l = new Locator();
        l.setName("l2");
        lg = new LocatorGroup();
        lg.setGroup("lg2");
        lg.getLocators().add(l);
        lGroups.add(lg);
        return lGroups;
    }

}
