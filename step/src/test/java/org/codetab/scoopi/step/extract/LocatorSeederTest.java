package org.codetab.scoopi.step.extract;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.exception.JobStateException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.step.base.PayloadFactory;
import org.codetab.scoopi.step.mediator.JobMediator;
import org.codetab.scoopi.step.mediator.TaskMediator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.codahale.metrics.Meter;
import com.google.common.collect.Lists;

public class LocatorSeederTest {

    @InjectMocks
    private LocatorSeeder locatorSeeder;

    @Mock
    private LocatorGroup locatorGroup;
    @Mock
    private PayloadFactory payloadFactory;
    @Mock
    private Errors errors;
    @Mock
    private JobMediator jobMediator;
    @Mock
    private JobSeeder jobSeeder;
    @Mock
    private Configs configs;
    @Mock
    private MetricsHelper metricsHelper;
    @Mock
    private ITaskDef taskDef;
    @Mock
    private TaskMediator taskMediator;
    @Mock
    private ObjectFactory factory;
    @Mock
    private Object output;
    @Mock
    private Payload payload;
    @Mock
    private Marker jobMarker;
    @Mock
    private Marker jobAbortedMarker;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInitialize() {
        LocatorGroup pData = Mockito.mock(LocatorGroup.class);
        Meter meter = Mockito.mock(Meter.class);
        List<Locator> grape = new ArrayList<>();

        when(payload.getData()).thenReturn(pData);
        when(metricsHelper.getMeter(locatorSeeder, "locator", "provided"))
                .thenReturn(meter);
        when(locatorGroup.getLocators()).thenReturn(grape);

        locatorSeeder.initialize();

        verify(meter).mark(grape.size());
    }

    @Test
    public void testInitializeException() {
        Meter meter = Mockito.mock(Meter.class);
        List<Locator> grape = new ArrayList<>();

        when(payload.getData()).thenReturn("not instance of LocatorGroup");
        when(metricsHelper.getMeter(locatorSeeder, "locator", "provided"))
                .thenReturn(meter);
        when(locatorGroup.getLocators()).thenReturn(grape);

        assertThrows(StepRunException.class, () -> locatorSeeder.initialize());

        verifyNoInteractions(metricsHelper, meter);
    }

    @Test
    public void testHandover() throws Exception {
        Meter meter = Mockito.mock(Meter.class);
        String group = "Foo";

        boolean kiwi = true;
        int seedRetryTimes = 1;

        List<Locator> grape = new ArrayList<>();
        List<Locator> mango = new ArrayList<>();
        Locator locator = Mockito.mock(Locator.class);
        mango.add(locator);

        Optional<String> firstTask = Optional.of("seed");

        ArrayList<String> firstTaskName = Lists.newArrayList(firstTask.get());
        StepInfo apricot = Mockito.mock(StepInfo.class);
        String peach = "Bar";

        List<Payload> payloads = new ArrayList<>();
        payloads.add(payload);

        JobInfo uacccgra = Mockito.mock(JobInfo.class);
        JobInfo gtcttssg = Mockito.mock(JobInfo.class);
        JobInfo osqtoulp = Mockito.mock(JobInfo.class);
        JobInfo ehjouqji = Mockito.mock(JobInfo.class);
        JobInfo nxlubvbt = Mockito.mock(JobInfo.class);

        String csfbfynn = "Baz";
        String yntwarsy = "Corge";
        String xodtktfz = "Grault";

        boolean nstdptuf = true;
        long kwzqyxyr = 1L;
        long linkJobId = 1L;

        List<Locator> oztbavho = new ArrayList<>();
        boolean xrcfmjjb = true;

        when(metricsHelper.getMeter(locatorSeeder, "locator", "seeded"))
                .thenReturn(meter);
        when(locatorGroup.getGroup()).thenReturn(group).thenReturn(xodtktfz);

        when(locatorGroup.getLocators()).thenReturn(grape).thenReturn(mango)
                .thenReturn(oztbavho);

        when(payloadFactory.createPayloads(group, firstTaskName, apricot, peach,
                locator)).thenReturn(payloads);

        when(locatorGroup.isByDef()).thenReturn(kiwi).thenReturn(xrcfmjjb);
        when(configs.getInt("scoopi.seeder.seedRetryTimes", "3"))
                .thenReturn(seedRetryTimes);
        when(taskDef.getFirstTaskName(group)).thenReturn(firstTask);
        when(payload.getStepInfo()).thenReturn(apricot);
        when(locator.getName()).thenReturn(peach).thenReturn(yntwarsy);

        when(locatorGroup.isByDef()).thenReturn(nstdptuf);
        when(payload.getJobInfo()).thenReturn(uacccgra).thenReturn(gtcttssg)
                .thenReturn(ehjouqji).thenReturn(nxlubvbt);
        when(uacccgra.getLabel()).thenReturn(csfbfynn);
        when(jobMediator.getJobIdSequence()).thenReturn(kwzqyxyr);

        locatorSeeder.handover();

        verify(gtcttssg).setId(kwzqyxyr);
        verify(jobMediator, times(1)).pushJob(payload);
        verify(osqtoulp, never()).setId(linkJobId);
        verify(taskMediator, never()).pushPayload(payload);
        verify(meter, times(1)).mark();
        verify(errors, never()).inc();
        verify(jobSeeder).countDownSeedLatch();
    }

    @Test
    public void testHandoverRetrySuccess() throws Exception {
        Meter meter = Mockito.mock(Meter.class);
        String group = "Foo";

        boolean kiwi = true;
        int seedRetryTimes = 4;

        List<Locator> grape = new ArrayList<>();
        List<Locator> mango = new ArrayList<>();
        Locator locator = Mockito.mock(Locator.class);
        mango.add(locator);

        Optional<String> firstTask = Optional.of("seed");

        ArrayList<String> firstTaskName = Lists.newArrayList(firstTask.get());
        StepInfo apricot = Mockito.mock(StepInfo.class);
        String peach = "Bar";

        List<Payload> payloads = new ArrayList<>();
        payloads.add(payload);

        JobInfo uacccgra = Mockito.mock(JobInfo.class);
        JobInfo gtcttssg = Mockito.mock(JobInfo.class);
        JobInfo osqtoulp = Mockito.mock(JobInfo.class);
        JobInfo ehjouqji = Mockito.mock(JobInfo.class);
        JobInfo nxlubvbt = Mockito.mock(JobInfo.class);

        String csfbfynn = "Baz";
        String yntwarsy = "Corge";
        String xodtktfz = "Grault";

        boolean nstdptuf = true;
        long kwzqyxyr = 1L;
        long linkJobId = 1L;

        List<Locator> oztbavho = new ArrayList<>();
        boolean xrcfmjjb = true;

        when(metricsHelper.getMeter(locatorSeeder, "locator", "seeded"))
                .thenReturn(meter);
        when(locatorGroup.getGroup()).thenReturn(group).thenReturn(xodtktfz);

        when(locatorGroup.getLocators()).thenReturn(grape).thenReturn(mango)
                .thenReturn(oztbavho);

        when(payloadFactory.createPayloads(group, firstTaskName, apricot, peach,
                locator)).thenReturn(payloads);

        when(locatorGroup.isByDef()).thenReturn(kiwi).thenReturn(xrcfmjjb);
        when(configs.getInt("scoopi.seeder.seedRetryTimes", "3"))
                .thenReturn(seedRetryTimes);
        when(taskDef.getFirstTaskName(group)).thenReturn(firstTask);
        when(payload.getStepInfo()).thenReturn(apricot);
        when(locator.getName()).thenReturn(peach).thenReturn(yntwarsy);

        when(locatorGroup.isByDef()).thenReturn(nstdptuf);
        when(payload.getJobInfo()).thenReturn(uacccgra).thenReturn(gtcttssg)
                .thenReturn(ehjouqji).thenReturn(nxlubvbt);
        when(uacccgra.getLabel()).thenReturn(csfbfynn);
        when(jobMediator.getJobIdSequence()).thenReturn(kwzqyxyr);

        doThrow(InterruptedException.class).doThrow(JobStateException.class)
                .doNothing().when(jobMediator).pushJob(payload);

        locatorSeeder.handover();

        verify(gtcttssg).setId(kwzqyxyr);
        // retry allowed 4, try 3 times
        verify(jobMediator, times(3)).pushJob(payload);
        verify(osqtoulp, never()).setId(linkJobId);
        verify(taskMediator, never()).pushPayload(payload);
        verify(meter, times(1)).mark();
        verify(errors, never()).inc();
        verify(jobSeeder).countDownSeedLatch();
    }

    @Test
    public void testHandoverRetryFail() throws Exception {
        Meter meter = Mockito.mock(Meter.class);
        String group = "Foo";

        boolean kiwi = true;
        int seedRetryTimes = 4;

        List<Locator> grape = new ArrayList<>();
        List<Locator> mango = new ArrayList<>();
        Locator locator = Mockito.mock(Locator.class);
        mango.add(locator);

        Optional<String> firstTask = Optional.of("seed");

        ArrayList<String> firstTaskName = Lists.newArrayList(firstTask.get());
        StepInfo apricot = Mockito.mock(StepInfo.class);
        String peach = "Bar";

        List<Payload> payloads = new ArrayList<>();
        payloads.add(payload);

        JobInfo uacccgra = Mockito.mock(JobInfo.class);
        JobInfo gtcttssg = Mockito.mock(JobInfo.class);
        JobInfo osqtoulp = Mockito.mock(JobInfo.class);
        JobInfo ehjouqji = Mockito.mock(JobInfo.class);
        JobInfo nxlubvbt = Mockito.mock(JobInfo.class);

        String csfbfynn = "Baz";
        String yntwarsy = "Corge";
        String xodtktfz = "Grault";

        boolean nstdptuf = true;
        long kwzqyxyr = 1L;
        long linkJobId = 1L;

        List<Locator> oztbavho = new ArrayList<>();
        boolean xrcfmjjb = true;

        when(metricsHelper.getMeter(locatorSeeder, "locator", "seeded"))
                .thenReturn(meter);
        when(locatorGroup.getGroup()).thenReturn(group).thenReturn(xodtktfz);

        when(locatorGroup.getLocators()).thenReturn(grape).thenReturn(mango)
                .thenReturn(oztbavho);

        when(payloadFactory.createPayloads(group, firstTaskName, apricot, peach,
                locator)).thenReturn(payloads);

        when(locatorGroup.isByDef()).thenReturn(kiwi).thenReturn(xrcfmjjb);
        when(configs.getInt("scoopi.seeder.seedRetryTimes", "3"))
                .thenReturn(seedRetryTimes);
        when(taskDef.getFirstTaskName(group)).thenReturn(firstTask);
        when(payload.getStepInfo()).thenReturn(apricot);
        when(locator.getName()).thenReturn(peach).thenReturn(yntwarsy);

        when(locatorGroup.isByDef()).thenReturn(nstdptuf);
        when(payload.getJobInfo()).thenReturn(uacccgra).thenReturn(gtcttssg)
                .thenReturn(ehjouqji).thenReturn(nxlubvbt);
        when(uacccgra.getLabel()).thenReturn(csfbfynn);
        when(jobMediator.getJobIdSequence()).thenReturn(kwzqyxyr);

        doThrow(InterruptedException.class).doThrow(JobStateException.class)
                .doThrow(JobStateException.class).when(jobMediator)
                .pushJob(payload);

        locatorSeeder.handover();

        verify(gtcttssg).setId(kwzqyxyr);
        // allowed retry 4, try 4 times and fail
        verify(jobMediator, times(4)).pushJob(payload);
        verify(osqtoulp, never()).setId(linkJobId);
        verify(taskMediator, never()).pushPayload(payload);
        verify(meter, never()).mark();
        verify(errors).inc();
        verify(jobSeeder).countDownSeedLatch();
    }

    @Test
    public void testHandoverNotByDef() throws Exception {
        Meter meter = Mockito.mock(Meter.class);
        String group = "Foo";

        boolean kiwi = true;
        int seedRetryTimes = 1;

        List<Locator> grape = new ArrayList<>();
        List<Locator> mango = new ArrayList<>();
        Locator locator = Mockito.mock(Locator.class);
        mango.add(locator);

        Optional<String> firstTask = Optional.of("seed");

        ArrayList<String> firstTaskName = Lists.newArrayList(firstTask.get());
        StepInfo apricot = Mockito.mock(StepInfo.class);
        String peach = "Bar";

        List<Payload> payloads = new ArrayList<>();
        payloads.add(payload);

        JobInfo uacccgra = Mockito.mock(JobInfo.class);
        JobInfo gtcttssg = Mockito.mock(JobInfo.class);
        JobInfo osqtoulp = Mockito.mock(JobInfo.class);
        JobInfo ehjouqji = Mockito.mock(JobInfo.class);
        JobInfo nxlubvbt = Mockito.mock(JobInfo.class);
        JobInfo hbvcfjrh = Mockito.mock(JobInfo.class);

        String csfbfynn = "Baz";
        String yntwarsy = "Corge";
        String xodtktfz = "Grault";

        boolean nstdptuf = false; // locatorGroup.isByDef() is false
        long linkJobId = 1L;

        List<Locator> oztbavho = new ArrayList<>();
        boolean xrcfmjjb = true;

        when(metricsHelper.getMeter(locatorSeeder, "locator", "seeded"))
                .thenReturn(meter);
        when(locatorGroup.getGroup()).thenReturn(group).thenReturn(xodtktfz);

        when(locatorGroup.getLocators()).thenReturn(grape).thenReturn(mango)
                .thenReturn(oztbavho);

        when(payloadFactory.createPayloads(group, firstTaskName, apricot, peach,
                locator)).thenReturn(payloads);

        when(locatorGroup.isByDef()).thenReturn(kiwi).thenReturn(xrcfmjjb);
        when(configs.getInt("scoopi.seeder.seedRetryTimes", "3"))
                .thenReturn(seedRetryTimes);
        when(taskDef.getFirstTaskName(group)).thenReturn(firstTask);
        when(payload.getStepInfo()).thenReturn(apricot);
        when(locator.getName()).thenReturn(peach).thenReturn(yntwarsy);

        when(locatorGroup.isByDef()).thenReturn(nstdptuf);

        when(payload.getJobInfo()).thenReturn(uacccgra).thenReturn(gtcttssg)
                .thenReturn(ehjouqji).thenReturn(nxlubvbt).thenReturn(hbvcfjrh);
        when(uacccgra.getLabel()).thenReturn(csfbfynn);
        when(gtcttssg.getId()).thenReturn(linkJobId);
        when(nxlubvbt.getLabel()).thenReturn(yntwarsy);
        when(hbvcfjrh.getId()).thenReturn(linkJobId);

        locatorSeeder.handover();

        verify(ehjouqji).setId(linkJobId);
        // not by def - pushed to taksMediator not to jobMediator
        verify(jobMediator, never()).pushJob(payload);
        verify(taskMediator, times(1)).pushPayload(payload);
        verify(osqtoulp, never()).setId(linkJobId);
        verify(meter, times(1)).mark();
        verify(errors, never()).inc();
        // not by def - latch not downed
        verify(jobSeeder, never()).countDownSeedLatch();
    }

    @Test
    public void testHandoverNoFirstTask() throws Exception {
        Meter meter = Mockito.mock(Meter.class);
        String group = "Foo";

        boolean kiwi = true;
        int seedRetryTimes = 1;

        List<Locator> grape = new ArrayList<>();
        List<Locator> mango = new ArrayList<>();
        Locator locator = Mockito.mock(Locator.class);
        mango.add(locator);

        Optional<String> firstTask = Optional.empty(); // no first task

        List<Locator> oztbavho = new ArrayList<>();
        boolean xrcfmjjb = true;
        String xodtktfz = "Grault";

        when(metricsHelper.getMeter(locatorSeeder, "locator", "seeded"))
                .thenReturn(meter);
        when(locatorGroup.getGroup()).thenReturn(group).thenReturn(xodtktfz);

        when(locatorGroup.getLocators()).thenReturn(grape).thenReturn(mango)
                .thenReturn(oztbavho);

        when(locatorGroup.isByDef()).thenReturn(kiwi).thenReturn(xrcfmjjb);
        when(configs.getInt("scoopi.seeder.seedRetryTimes", "3"))
                .thenReturn(seedRetryTimes);
        when(taskDef.getFirstTaskName(group)).thenReturn(firstTask);

        locatorSeeder.handover();

        verify(errors).inc();
        verify(jobSeeder).countDownSeedLatch();

        verifyNoInteractions(payloadFactory);
        verify(jobMediator, never()).pushJob(payload);
        verify(taskMediator, never()).pushPayload(payload);
        verify(meter, never()).mark();
    }

    @Test
    public void testHandoverNoPayload() throws Exception {
        Meter meter = Mockito.mock(Meter.class);
        String group = "Foo";

        StepInfo apricot = Mockito.mock(StepInfo.class);
        String peach = "Bar";

        boolean kiwi = true;
        int seedRetryTimes = 1;

        List<Locator> grape = new ArrayList<>();
        List<Locator> mango = new ArrayList<>();
        Locator locator = Mockito.mock(Locator.class);
        mango.add(locator);

        Optional<String> firstTask = Optional.of("seed");
        ArrayList<String> firstTaskName = Lists.newArrayList(firstTask.get());

        List<Payload> payloads = new ArrayList<>();
        payloads.add(payload);

        List<Locator> oztbavho = new ArrayList<>();
        boolean xrcfmjjb = true;
        String xodtktfz = "Grault";

        when(locatorGroup.isByDef()).thenReturn(kiwi).thenReturn(xrcfmjjb);
        when(configs.getInt("scoopi.seeder.seedRetryTimes", "3"))
                .thenReturn(seedRetryTimes);
        when(taskDef.getFirstTaskName(group)).thenReturn(firstTask);
        when(metricsHelper.getMeter(locatorSeeder, "locator", "seeded"))
                .thenReturn(meter);
        when(locatorGroup.getGroup()).thenReturn(group).thenReturn(xodtktfz);

        when(locatorGroup.getLocators()).thenReturn(grape).thenReturn(mango)
                .thenReturn(oztbavho);

        when(payloadFactory.createPayloads(group, firstTaskName, apricot, peach,
                locator)).thenReturn(payloads);

        locatorSeeder.handover();

        verify(errors).inc();
        verify(jobSeeder).countDownSeedLatch();

        verify(jobMediator, never()).pushJob(payload);
        verify(taskMediator, never()).pushPayload(payload);
        verify(meter, never()).mark();
    }

    @Test
    public void testHandoverMoreThanonePayloads() throws Exception {
        Meter meter = Mockito.mock(Meter.class);
        String group = "Foo";

        StepInfo apricot = Mockito.mock(StepInfo.class);
        String peach = "Bar";

        boolean kiwi = true;
        int seedRetryTimes = 1;

        List<Locator> grape = new ArrayList<>();
        List<Locator> mango = new ArrayList<>();
        Locator locator = Mockito.mock(Locator.class);
        mango.add(locator);

        Optional<String> firstTask = Optional.of("seed");
        ArrayList<String> firstTaskName = Lists.newArrayList(firstTask.get());

        List<Payload> payloads = new ArrayList<>();
        payloads.add(payload);
        payloads.add(Mockito.mock(Payload.class));

        List<Locator> oztbavho = new ArrayList<>();
        boolean xrcfmjjb = true;
        String xodtktfz = "Grault";

        when(locatorGroup.isByDef()).thenReturn(kiwi).thenReturn(xrcfmjjb);
        when(configs.getInt("scoopi.seeder.seedRetryTimes", "3"))
                .thenReturn(seedRetryTimes);
        when(taskDef.getFirstTaskName(group)).thenReturn(firstTask);
        when(metricsHelper.getMeter(locatorSeeder, "locator", "seeded"))
                .thenReturn(meter);
        when(locatorGroup.getGroup()).thenReturn(group).thenReturn(xodtktfz);

        when(locatorGroup.getLocators()).thenReturn(grape).thenReturn(mango)
                .thenReturn(oztbavho);

        when(payloadFactory.createPayloads(group, firstTaskName, apricot, peach,
                locator)).thenReturn(payloads);

        locatorSeeder.handover();

        verify(errors).inc();
        verify(jobSeeder).countDownSeedLatch();

        verify(jobMediator, never()).pushJob(payload);
        verify(taskMediator, never()).pushPayload(payload);
        verify(meter, never()).mark();
    }
}
