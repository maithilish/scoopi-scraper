package org.codetab.scoopi.step.extract;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.ILocatorDef;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.step.base.PayloadFactory;
import org.codetab.scoopi.step.mediator.TaskMediator;
import org.codetab.scoopi.store.cluster.hz.CrashCleaner;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class JobSeederTest {
    @InjectMocks
    private JobSeeder jobSeeder;

    @Mock
    private Configs configs;
    @Mock
    private ILocatorDef locatorDef;
    @Mock
    private TaskMediator taskMediator;
    @Mock
    private PayloadFactory payloadFactory;
    @Mock
    private Errors errors;
    @Mock
    private CrashCleaner crashCleaner;
    @Mock
    private CountDownLatch seedLatch;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSeedLocatorGroups() throws Exception {
        String stepName = "start";
        String seederClzName = "Foo";
        List<LocatorGroup> locatorGroups = new ArrayList<>();
        List<Payload> payloads = new ArrayList<>();
        Payload payload = Mockito.mock(Payload.class);
        payloads.add(payload);

        JobInfo kiwi = Mockito.mock(JobInfo.class);
        String group = "Bar";

        when(configs.getConfig("scoopi.seeder.class"))
                .thenReturn(seederClzName);
        when(locatorDef.getLocatorGroups()).thenReturn(locatorGroups);
        when(payloadFactory.createSeedPayloads(locatorGroups, stepName,
                seederClzName)).thenReturn(payloads);
        when(payload.getJobInfo()).thenReturn(kiwi);
        when(kiwi.getGroup()).thenReturn(group);
        jobSeeder.seedLocatorGroups();

        verify(taskMediator).pushPayload(payload);
        verify(errors, never()).inc();
    }

    @Test
    public void testSeedLocatorGroupsConfigNotFound() throws Exception {
        List<Payload> payloads = new ArrayList<>();
        Payload payload = Mockito.mock(Payload.class);
        payloads.add(payload);

        when(configs.getConfig("scoopi.seeder.class"))
                .thenThrow(ConfigNotFoundException.class);

        assertThrows(CriticalException.class,
                () -> jobSeeder.seedLocatorGroups());

        verify(taskMediator, never()).pushPayload(payload);
        verify(errors, never()).inc();
    }

    @Test
    public void testSeedLocatorGroupsException() throws Exception {
        String stepName = "start";
        String seederClzName = "Foo";
        List<LocatorGroup> locatorGroups = new ArrayList<>();
        List<Payload> payloads = new ArrayList<>();
        Payload payload = Mockito.mock(Payload.class);
        payloads.add(payload);

        JobInfo kiwi = Mockito.mock(JobInfo.class);
        String group = "Bar";

        when(configs.getConfig("scoopi.seeder.class"))
                .thenReturn(seederClzName);
        when(locatorDef.getLocatorGroups()).thenReturn(locatorGroups);
        when(payloadFactory.createSeedPayloads(locatorGroups, stepName,
                seederClzName)).thenReturn(payloads);
        when(payload.getJobInfo()).thenReturn(kiwi);
        when(kiwi.getGroup()).thenReturn(group);
        doThrow(InterruptedException.class).when(taskMediator)
                .pushPayload(payload);

        jobSeeder.seedLocatorGroups();

        assertTrue(Thread.currentThread().isInterrupted());
        verify(errors).inc();
    }

    @Test
    public void testClearDanglingJobs() {
        boolean apple = true;

        when(configs.isCluster()).thenReturn(apple);
        jobSeeder.clearDanglingJobs();

        verify(crashCleaner).clearDanglingJobs();
    }

    @Test
    public void testClearDanglingJobsNotCluster() {
        boolean apple = false;

        when(configs.isCluster()).thenReturn(apple);
        jobSeeder.clearDanglingJobs();

        verify(crashCleaner, never()).clearDanglingJobs();
    }

    @Test
    public void testAwaitForSeedDone() throws Exception {
        jobSeeder.awaitForSeedDone();

        verify(seedLatch).await();
    }

    @Test
    public void testAwaitForSeedDoneException() throws Exception {
        doThrow(InterruptedException.class).when(seedLatch).await();
        assertThrows(CriticalException.class,
                () -> jobSeeder.awaitForSeedDone());
    }

    @Test
    public void testCountDownSeedLatch() {
        jobSeeder.countDownSeedLatch();

        verify(seedLatch).countDown();
    }
}
