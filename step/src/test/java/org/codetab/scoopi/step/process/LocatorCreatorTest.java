package org.codetab.scoopi.step.process;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.JobStateException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.step.base.PayloadFactory;
import org.codetab.scoopi.step.mediator.JobMediator;
import org.codetab.scoopi.step.mediator.TaskMediator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class LocatorCreatorTest {
    @InjectMocks
    private LocatorCreator locatorCreator;

    @Mock
    private LocatorGroupFactory locatorGroupFactory;
    @Mock
    private PayloadFactory payloadFactory;
    @Mock
    private Data data;
    @Mock
    private Configs configs;
    @Mock
    private MetricsHelper metricsHelper;
    @Mock
    private ITaskDef taskDef;
    @Mock
    private TaskMediator taskMediator;
    @Mock
    private JobMediator jobMediator;
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
    @Mock
    private List<LocatorGroup> locatorGroups;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testProcess() {
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String locatorName = "Foo";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String dataDef = "Bar";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        long jobId = 1L;
        List<Item> list = new ArrayList<>();
        List<LocatorGroup> locatorGroups2 = new ArrayList<>();

        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo3);
        when(jobInfo.getName()).thenReturn(locatorName);
        when(jobInfo2.getDataDef()).thenReturn(dataDef);
        when(jobInfo3.getId()).thenReturn(jobId);
        when(data.getItems()).thenReturn(list);
        when(locatorGroupFactory.createLocatorGroups(dataDef, list,
                locatorName)).thenReturn(locatorGroups2);
        locatorCreator.process();

        assertSame(locatorGroups2, locatorCreator.getOutput());
    }

    @Test
    public void testHandoverTryTry() throws Exception {
        String stepName = "start";
        String seederClzName = "Foo";
        List<Payload> payloads = new ArrayList<>();
        Payload payload2 = Mockito.mock(Payload.class);
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        long kiwi = 1L;
        long jobId = 0L;
        payloads.add(payload2);

        when(configs.getConfig("scoopi.seeder.class"))
                .thenReturn(seederClzName);
        when(payloadFactory.createSeedPayloads(locatorGroups, stepName,
                seederClzName)).thenReturn(payloads);
        when(payload2.getJobInfo()).thenReturn(jobInfo);
        when(jobMediator.getJobIdSequence()).thenReturn(kiwi);
        locatorCreator.handover();

        verify(jobInfo).setId(kiwi);
        verify(jobMediator).pushJobs(payloads, jobId);
    }

    @Test
    public void testHandoverTryCatchInterruptedExceptionIf() throws Exception {
        String stepName = "start";
        String seederClzName = "Foo";
        List<Payload> payloads = new ArrayList<>();
        Payload payload2 = Mockito.mock(Payload.class);
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        long kiwi = 1L;
        String mango = "Bar";
        long jobId = 0L;
        payloads.add(payload2);

        when(configs.getConfig("scoopi.seeder.class"))
                .thenReturn(seederClzName);
        when(payloadFactory.createSeedPayloads(locatorGroups, stepName,
                seederClzName)).thenReturn(payloads);
        when(payload2.getJobInfo()).thenReturn(jobInfo);
        when(jobMediator.getJobIdSequence()).thenReturn(kiwi);
        when(payload2.toString()).thenReturn(mango);

        doThrow(InterruptedException.class).when(jobMediator).pushJobs(payloads,
                jobId);

        assertThrows(StepRunException.class,
                () -> locatorCreator.handover());

        assertTrue(Thread.currentThread().isInterrupted());

        verify(jobInfo).setId(kiwi);


    }

    @Test
    public void testHandoverTryCatchJobStateException()
            throws Exception {
        String stepName = "start";
        String seederClzName = "Foo";
        List<Payload> payloads = new ArrayList<>();
        Payload payload2 = Mockito.mock(Payload.class);
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        long kiwi = 1L;
        String mango = "Bar";
        long jobId = 0L;
        payloads.add(payload2);


        when(configs.getConfig("scoopi.seeder.class"))
                .thenReturn(seederClzName);
        when(payloadFactory.createSeedPayloads(locatorGroups, stepName,
                seederClzName)).thenReturn(payloads);
        when(payload2.getJobInfo()).thenReturn(jobInfo);
        when(jobMediator.getJobIdSequence()).thenReturn(kiwi);
        when(payload2.toString()).thenReturn(mango);

        doThrow(JobStateException.class).when(jobMediator).pushJobs(payloads,
                jobId);

        assertThrows(StepRunException.class,
                () -> locatorCreator.handover());

        assertFalse(Thread.currentThread().isInterrupted());

        verify(jobInfo).setId(kiwi);
        verify(jobMediator).pushJobs(payloads, jobId);
    }

    @Test
    public void testHandoverTryCatchConfigNotFoundExceptionTry()
            throws Exception {
        String stepName = "start";
        String seederClzName = "Foo";
        List<Payload> payloads = new ArrayList<>();
        Payload payload2 = Mockito.mock(Payload.class);
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        long kiwi = 1L;
        long jobId = 0L;
        payloads.add(payload2);
        payloads.add(payload2);

        when(configs.getConfig("scoopi.seeder.class"))
                .thenThrow(ConfigNotFoundException.class);
        when(payloadFactory.createSeedPayloads(locatorGroups, stepName,
                seederClzName)).thenReturn(payloads);
        when(payload2.getJobInfo()).thenReturn(jobInfo);
        when(jobMediator.getJobIdSequence()).thenReturn(kiwi);

        assertThrows(StepRunException.class, () -> locatorCreator.handover());

        verify(jobInfo, never()).setId(kiwi);
        verify(jobMediator, never()).pushJobs(payloads, jobId);
    }
}
