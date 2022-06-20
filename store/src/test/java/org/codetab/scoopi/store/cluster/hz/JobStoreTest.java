package org.codetab.scoopi.store.cluster.hz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.exception.JobStateException;
import org.codetab.scoopi.exception.TransactionException;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.ClusterJob;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.store.ICluster;
import org.codetab.scoopi.store.IJobStore;
import org.codetab.scoopi.store.IJobStore.State;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.hazelcast.collection.IList;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.flakeidgen.FlakeIdGenerator;
import com.hazelcast.map.IMap;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;
import com.hazelcast.transaction.TransactionTimedOutException;
import com.hazelcast.transaction.TransactionalList;
import com.hazelcast.transaction.TransactionalMap;

public class JobStoreTest {
    @InjectMocks
    private JobStore jobStore;

    @Mock
    private Configs configs;
    @Mock
    private ICluster cluster;
    @Mock
    private ObjectFactory objFactory;
    @Mock
    private CrashCleaner crashCleaner;
    @Mock
    private MetricsHelper metricsHelper;
    @Mock
    private HazelcastInstance hz;
    @Mock
    private IList<ClusterJob> jobsList;
    @Mock
    private IMap<Long, ClusterJob> takenJobsMap;
    @Mock
    private IMap<String, String> keyStoreMap;
    @Mock
    private FlakeIdGenerator jobIdGenerator;
    @Mock
    private Semaphore jobTakeThrottle;
    @Mock
    private TransactionOptions txOptions;
    @Mock
    private Random random;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOpen() throws Exception {
        HazelcastInstance hz1 = Mockito.mock(HazelcastInstance.class);
        String memberId = "Foo";
        int jobTakeLimit = 1;
        int jobTakeTimeout = 1;
        FlakeIdGenerator jobIdGenerator1 = Mockito.mock(FlakeIdGenerator.class);
        int startCrashCleanerMinThreshold = 1;
        TransactionOptions txOptions1 = Mockito.mock(TransactionOptions.class);
        IList<Object> jobsList1 = Mockito.mock(IList.class);
        IMap<Object, Object> takenJobsMap1 = Mockito.mock(IMap.class);
        IMap<Object, Object> keyStoreMap1 = Mockito.mock(IMap.class);

        when(cluster.getInstance()).thenReturn(hz1);
        when(configs.getConfig("scoopi.cluster.memberId")).thenReturn(memberId);
        when(configs.getInt("scoopi.job.takeLimit", "4"))
                .thenReturn(jobTakeLimit);
        when(configs.getInt("scoopi.job.takeTimeout", "1000"))
                .thenReturn(jobTakeTimeout);
        when(hz1.getFlakeIdGenerator("job_id_seq")).thenReturn(jobIdGenerator1);
        when(configs.getInt("scoopi.cluster.startCrashCleaner.minThreshold",
                "10")).thenReturn(startCrashCleanerMinThreshold);
        when(cluster.getTxOptions(configs)).thenReturn(txOptions1);
        when(hz1.getList(DsName.JOBS_LIST.toString())).thenReturn(jobsList1);
        when(hz1.getMap(DsName.TAKEN_JOBS_MAP.toString()))
                .thenReturn(takenJobsMap1);
        when(hz1.getMap(DsName.KEYSTORE_MAP.toString()))
                .thenReturn(keyStoreMap1);
        jobStore.open();

        assertSame(hz1, FieldUtils.readDeclaredField(jobStore, "hz", true));
        assertSame(memberId,
                FieldUtils.readDeclaredField(jobStore, "memberId", true));
        assertEquals(jobTakeLimit,
                FieldUtils.readDeclaredField(jobStore, "jobTakeLimit", true));
        assertEquals(jobTakeTimeout,
                FieldUtils.readDeclaredField(jobStore, "jobTakeTimeout", true));
        assertSame(jobIdGenerator1,
                FieldUtils.readDeclaredField(jobStore, "jobIdGenerator", true));
        assertEquals(startCrashCleanerMinThreshold,
                FieldUtils.readDeclaredField(jobStore,
                        "startCrashCleanerMinThreshold", true));
        assertSame(txOptions1,
                FieldUtils.readDeclaredField(jobStore, "txOptions", true));
        assertSame(jobsList1,
                FieldUtils.readDeclaredField(jobStore, "jobsList", true));
        assertSame(takenJobsMap1,
                FieldUtils.readDeclaredField(jobStore, "takenJobsMap", true));
        assertSame(keyStoreMap1,
                FieldUtils.readDeclaredField(jobStore, "keyStoreMap", true));
        assertNotNull(FieldUtils.readDeclaredField(jobStore, "random", true));
        Semaphore jobTakeThrottle1 = (Semaphore) FieldUtils
                .readDeclaredField(jobStore, "jobTakeThrottle", true);
        assertEquals(jobTakeLimit, jobTakeThrottle1.availablePermits());
    }

    @Test
    public void testOpenException() throws Exception {

        when(configs.getConfig("scoopi.cluster.memberId"))
                .thenThrow(ConfigNotFoundException.class);
        when(configs.getInt("scoopi.job.takeLimit", "4"))
                .thenThrow(NumberFormatException.class);

        assertThrows(CriticalException.class, () -> jobStore.open());
        assertThrows(CriticalException.class, () -> jobStore.open());
    }

    @Test
    public void testClose() {
        long divisor = 1_000_000;
        long apple = 1L;
        Snapshot grape = Mockito.mock(Snapshot.class);
        String orange = "Foo";
        Timer t = Mockito.mock(Timer.class);
        long mango = 1L;
        Snapshot banana = Mockito.mock(Snapshot.class);
        String cherry = "Bar";

        when(metricsHelper.getTimer(jobStore, "job", "put", "time"))
                .thenReturn(t);
        when(t.getCount()).thenReturn(apple).thenReturn(mango);
        when(t.getSnapshot()).thenReturn(grape).thenReturn(banana);
        when(metricsHelper.printSnapshot("job put time", apple, grape, divisor))
                .thenReturn(orange);
        when(metricsHelper.getTimer(jobStore, "job", "take", "time"))
                .thenReturn(t);
        when(metricsHelper.printSnapshot("job take time", mango, banana,
                divisor)).thenReturn(cherry);
        jobStore.close();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPutJobDuplicateJob() throws Exception {
        Payload payload = Mockito.mock(Payload.class);
        Timer apple = Mockito.mock(Timer.class);
        Context timer = Mockito.mock(Context.class);
        JobInfo grape = Mockito.mock(JobInfo.class);
        long jobId = 1L;
        ClusterJob cluserJob = Mockito.mock(ClusterJob.class);
        TransactionContext tx = Mockito.mock(TransactionContext.class);
        TransactionalList<Object> txJobsList =
                Mockito.mock(TransactionalList.class);
        TransactionalMap<Object, Object> txPayloadsMap =
                Mockito.mock(TransactionalMap.class);
        boolean mango = true;
        JobInfo apricot = Mockito.mock(JobInfo.class);
        String peach = "Foo";

        when(metricsHelper.getTimer(jobStore, "job", "put", "time"))
                .thenReturn(apple);
        when(apple.time()).thenReturn(timer);
        when(payload.getJobInfo()).thenReturn(grape).thenReturn(apricot);
        when(grape.getId()).thenReturn(jobId);
        when(objFactory.createClusterJob(jobId)).thenReturn(cluserJob);
        when(hz.newTransactionContext(txOptions)).thenReturn(tx);
        when(tx.getList(DsName.JOBS_LIST.toString())).thenReturn(txJobsList);
        when(tx.getMap(DsName.PAYLOADS_MAP.toString()))
                .thenReturn(txPayloadsMap);
        when(txPayloadsMap.containsKey(jobId)).thenReturn(mango);
        when(apricot.getLabel()).thenReturn(peach);

        assertThrows(TransactionException.class,
                () -> jobStore.putJob(payload));

        verify(tx).beginTransaction();
        verify(txJobsList, never()).add(cluserJob);
        verify(txPayloadsMap, never()).set(jobId, payload);
        verify(tx, never()).commitTransaction();
        verify(tx).rollbackTransaction();
        verify(timer).stop();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPutJob() throws Exception {
        Payload payload = Mockito.mock(Payload.class);
        Timer apple = Mockito.mock(Timer.class);
        Context timer = Mockito.mock(Context.class);
        JobInfo grape = Mockito.mock(JobInfo.class);
        long jobId = 1L;
        ClusterJob cluserJob = Mockito.mock(ClusterJob.class);
        TransactionContext tx = Mockito.mock(TransactionContext.class);
        TransactionalList<Object> txJobsList =
                Mockito.mock(TransactionalList.class);
        TransactionalMap<Object, Object> txPayloadsMap =
                Mockito.mock(TransactionalMap.class);
        boolean mango = false;
        JobInfo apricot = Mockito.mock(JobInfo.class);
        String peach = "Foo";

        when(metricsHelper.getTimer(jobStore, "job", "put", "time"))
                .thenReturn(apple);
        when(apple.time()).thenReturn(timer);
        when(payload.getJobInfo()).thenReturn(grape).thenReturn(apricot);
        when(grape.getId()).thenReturn(jobId);
        when(objFactory.createClusterJob(jobId)).thenReturn(cluserJob);
        when(hz.newTransactionContext(txOptions)).thenReturn(tx);
        when(tx.getList(DsName.JOBS_LIST.toString())).thenReturn(txJobsList);
        when(tx.getMap(DsName.PAYLOADS_MAP.toString()))
                .thenReturn(txPayloadsMap);
        when(txPayloadsMap.containsKey(jobId)).thenReturn(mango);
        when(apricot.getLabel()).thenReturn(peach);

        boolean actual = jobStore.putJob(payload);

        assertTrue(actual);
        verify(tx).beginTransaction();
        verify(txJobsList).add(cluserJob);
        verify(txPayloadsMap).set(jobId, payload);
        verify(tx).commitTransaction();
        verify(tx, never()).rollbackTransaction();
        verify(timer).stop();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPutJobsDeleteOldJobAndCreateAddNewJob() throws Exception {
        List<Payload> payloads = new ArrayList<>();
        Payload payload = Mockito.mock(Payload.class);
        payloads.add(payload);
        long jobId = 1L;
        TransactionContext tx = Mockito.mock(TransactionContext.class);
        TransactionalList<Object> txJobsList =
                Mockito.mock(TransactionalList.class);
        TransactionalMap<Object, Object> txTakenJobsMap =
                Mockito.mock(TransactionalMap.class);
        TransactionalMap<Object, Object> txPayloadsMap =
                Mockito.mock(TransactionalMap.class);
        boolean kiwi = true;
        JobInfo mango = Mockito.mock(JobInfo.class);
        long newJobId = 1L;
        boolean banana = false; // txPayloadsMap doesn't contain newJobId
        ClusterJob cluserJob = Mockito.mock(ClusterJob.class);
        int peach = 5;
        FieldUtils.writeDeclaredField(jobStore, "jobTakeLimit", 4, true);

        when(hz.newTransactionContext(txOptions)).thenReturn(tx);
        when(tx.getList(DsName.JOBS_LIST.toString())).thenReturn(txJobsList);
        when(tx.getMap(DsName.TAKEN_JOBS_MAP.toString()))
                .thenReturn(txTakenJobsMap);
        when(tx.getMap(DsName.PAYLOADS_MAP.toString()))
                .thenReturn(txPayloadsMap);
        when(txTakenJobsMap.containsKey(jobId)).thenReturn(kiwi);
        when(payload.getJobInfo()).thenReturn(mango);
        when(mango.getId()).thenReturn(newJobId);
        when(txPayloadsMap.containsKey(newJobId)).thenReturn(banana);
        when(objFactory.createClusterJob(newJobId)).thenReturn(cluserJob);
        when(jobTakeThrottle.availablePermits()).thenReturn(peach);

        boolean actual = jobStore.putJobs(payloads, jobId);

        assertTrue(actual);
        verify(tx).beginTransaction();
        verify(txTakenJobsMap).delete(jobId);
        verify(txPayloadsMap).delete(jobId);
        verify(txJobsList).add(cluserJob);
        verify(txPayloadsMap).set(newJobId, payload);
        verify(tx).commitTransaction();
        verify(jobTakeThrottle, never()).release();
        verify(tx, never()).rollbackTransaction();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPutJobsDeleteReleaseJobTakeThrottle() throws Exception {
        List<Payload> payloads = new ArrayList<>();
        Payload payload = Mockito.mock(Payload.class);
        payloads.add(payload);
        long jobId = 1L;
        TransactionContext tx = Mockito.mock(TransactionContext.class);
        TransactionalList<Object> txJobsList =
                Mockito.mock(TransactionalList.class);
        TransactionalMap<Object, Object> txTakenJobsMap =
                Mockito.mock(TransactionalMap.class);
        TransactionalMap<Object, Object> txPayloadsMap =
                Mockito.mock(TransactionalMap.class);
        boolean kiwi = true;
        JobInfo mango = Mockito.mock(JobInfo.class);
        long newJobId = 1L;
        boolean banana = false; // txPayloadsMap doesn't contain newJobId
        ClusterJob cluserJob = Mockito.mock(ClusterJob.class);
        int peach = 2;
        FieldUtils.writeDeclaredField(jobStore, "jobTakeLimit", 4, true);

        when(hz.newTransactionContext(txOptions)).thenReturn(tx);
        when(tx.getList(DsName.JOBS_LIST.toString())).thenReturn(txJobsList);
        when(tx.getMap(DsName.TAKEN_JOBS_MAP.toString()))
                .thenReturn(txTakenJobsMap);
        when(tx.getMap(DsName.PAYLOADS_MAP.toString()))
                .thenReturn(txPayloadsMap);
        when(txTakenJobsMap.containsKey(jobId)).thenReturn(kiwi);
        when(payload.getJobInfo()).thenReturn(mango);
        when(mango.getId()).thenReturn(newJobId);
        when(txPayloadsMap.containsKey(newJobId)).thenReturn(banana);
        when(objFactory.createClusterJob(newJobId)).thenReturn(cluserJob);
        when(jobTakeThrottle.availablePermits()).thenReturn(peach);

        boolean actual = jobStore.putJobs(payloads, jobId);

        assertTrue(actual);
        verify(tx).beginTransaction();
        verify(txTakenJobsMap).delete(jobId);
        verify(txPayloadsMap).delete(jobId);
        verify(txJobsList).add(cluserJob);
        verify(txPayloadsMap).set(newJobId, payload);
        verify(tx).commitTransaction();
        verify(jobTakeThrottle).release();
        verify(tx, never()).rollbackTransaction();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPutJobsRemoveOldJobJobStateException() throws Exception {
        List<Payload> payloads = new ArrayList<>();
        long jobId = 1L;
        TransactionContext tx = Mockito.mock(TransactionContext.class);
        TransactionalList<Object> txJobsList =
                Mockito.mock(TransactionalList.class);
        TransactionalMap<Object, Object> txTakenJobsMap =
                Mockito.mock(TransactionalMap.class);
        TransactionalMap<Object, Object> txPayloadsMap =
                Mockito.mock(TransactionalMap.class);
        boolean kiwi = false; // doesn't contain
        Payload payload = Mockito.mock(Payload.class);
        long newJobId = 1L;
        ClusterJob cluserJob = Mockito.mock(ClusterJob.class);

        when(hz.newTransactionContext(txOptions)).thenReturn(tx);
        when(tx.getList(DsName.JOBS_LIST.toString())).thenReturn(txJobsList);
        when(tx.getMap(DsName.TAKEN_JOBS_MAP.toString()))
                .thenReturn(txTakenJobsMap);
        when(tx.getMap(DsName.PAYLOADS_MAP.toString()))
                .thenReturn(txPayloadsMap);
        when(txTakenJobsMap.containsKey(jobId)).thenReturn(kiwi);

        assertThrows(JobStateException.class,
                () -> jobStore.putJobs(payloads, jobId));

        verify(tx).beginTransaction();
        verify(txTakenJobsMap, never()).delete(jobId);
        verify(txPayloadsMap, never()).delete(jobId);
        verify(txJobsList, never()).add(cluserJob);
        verify(txPayloadsMap, never()).set(newJobId, payload);
        verify(tx, never()).commitTransaction();
        verify(jobTakeThrottle, never()).release();
        verify(tx).rollbackTransaction();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPutJobsDuplicateJob() throws Exception {
        List<Payload> payloads = new ArrayList<>();
        Payload payload = Mockito.mock(Payload.class);
        payloads.add(payload);

        long jobId = 1L;
        TransactionContext tx = Mockito.mock(TransactionContext.class);
        TransactionalList<Object> txJobsList =
                Mockito.mock(TransactionalList.class);
        TransactionalMap<Object, Object> txTakenJobsMap =
                Mockito.mock(TransactionalMap.class);
        TransactionalMap<Object, Object> txPayloadsMap =
                Mockito.mock(TransactionalMap.class);
        boolean kiwi = true;

        JobInfo mango = Mockito.mock(JobInfo.class);
        long newJobId = 1L;
        boolean banana = true;
        ClusterJob cluserJob = Mockito.mock(ClusterJob.class);

        when(hz.newTransactionContext(txOptions)).thenReturn(tx);
        when(tx.getList(DsName.JOBS_LIST.toString())).thenReturn(txJobsList);
        when(tx.getMap(DsName.TAKEN_JOBS_MAP.toString()))
                .thenReturn(txTakenJobsMap);
        when(tx.getMap(DsName.PAYLOADS_MAP.toString()))
                .thenReturn(txPayloadsMap);
        when(txTakenJobsMap.containsKey(jobId)).thenReturn(kiwi);
        when(payload.getJobInfo()).thenReturn(mango);
        when(mango.getId()).thenReturn(newJobId);
        when(txPayloadsMap.containsKey(newJobId)).thenReturn(banana);

        assertThrows(JobStateException.class,
                () -> jobStore.putJobs(payloads, jobId));

        verify(tx).beginTransaction();
        verify(txTakenJobsMap).delete(jobId);
        verify(txPayloadsMap).delete(jobId);
        verify(txJobsList, never()).add(cluserJob);
        verify(txPayloadsMap, never()).set(newJobId, payload);
        verify(tx, never()).commitTransaction();
        verify(jobTakeThrottle, never()).release();
        verify(tx).rollbackTransaction();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPutJobsException() throws Exception {
        List<Payload> payloads = new ArrayList<>();
        long jobId = 1L;
        TransactionContext tx = Mockito.mock(TransactionContext.class);
        TransactionalList<Object> txJobsList =
                Mockito.mock(TransactionalList.class);
        TransactionalMap<Object, Object> txTakenJobsMap =
                Mockito.mock(TransactionalMap.class);
        TransactionalMap<Object, Object> txPayloadsMap =
                Mockito.mock(TransactionalMap.class);
        Payload payload = Mockito.mock(Payload.class);
        long newJobId = 1L;
        ClusterJob cluserJob = Mockito.mock(ClusterJob.class);

        when(hz.newTransactionContext(txOptions)).thenReturn(tx);
        doThrow(IllegalStateException.class).when(tx).beginTransaction();

        assertThrows(TransactionException.class,
                () -> jobStore.putJobs(payloads, jobId));

        verify(tx).beginTransaction();
        verify(txTakenJobsMap, never()).delete(jobId);
        verify(txPayloadsMap, never()).delete(jobId);
        verify(txJobsList, never()).add(cluserJob);
        verify(txPayloadsMap, never()).set(newJobId, payload);
        verify(tx, never()).commitTransaction();
        verify(jobTakeThrottle, never()).release();
        verify(tx).rollbackTransaction();
    }

    @Test
    public void testTakeJobTimeout() throws Exception {
        Timer apple = Mockito.mock(Timer.class);
        Context timer = Mockito.mock(Context.class);
        boolean acquired = false;
        int jobTakeTimeout = 1;

        FieldUtils.writeDeclaredField(jobStore, "jobTakeTimeout",
                jobTakeTimeout, true);

        when(metricsHelper.getTimer(jobStore, "job", "take", "time"))
                .thenReturn(apple);
        when(apple.time()).thenReturn(timer);
        when(jobTakeThrottle.tryAcquire(jobTakeTimeout, TimeUnit.MILLISECONDS))
                .thenReturn(acquired);

        assertThrows(TimeoutException.class, () -> jobStore.takeJob());
    }

    @Test
    public void testTakeJobQueueEmpty() throws Exception {
        Timer apple = Mockito.mock(Timer.class);
        Context timer = Mockito.mock(Context.class);
        boolean acquired = true;
        int size = 0; // queue empty
        int jobTakeTimeout = 1;

        FieldUtils.writeDeclaredField(jobStore, "jobTakeTimeout",
                jobTakeTimeout, true);

        when(metricsHelper.getTimer(jobStore, "job", "take", "time"))
                .thenReturn(apple);
        when(apple.time()).thenReturn(timer);
        when(jobTakeThrottle.tryAcquire(jobTakeTimeout, TimeUnit.MILLISECONDS))
                .thenReturn(acquired);
        when(jobsList.size()).thenReturn(size);

        assertThrows(NoSuchElementException.class, () -> jobStore.takeJob());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTakeJob() throws Exception {
        Timer apple = Mockito.mock(Timer.class);
        Context timer = Mockito.mock(Context.class);
        boolean acquired = true;
        int size = 1;
        TransactionContext tx = Mockito.mock(TransactionContext.class);
        int windowSize = 10;
        int offset = 1;
        int index = 0;
        ClusterJob cJob = Mockito.mock(ClusterJob.class);
        TransactionalList<Object> txJobsList =
                Mockito.mock(TransactionalList.class);
        TransactionalMap<Object, Object> txTakenJobsMap =
                Mockito.mock(TransactionalMap.class);
        TransactionalMap<Object, Object> txPayloadsMap =
                Mockito.mock(TransactionalMap.class);
        long jobId = 1L;
        boolean lychee = true;
        Payload payload = Mockito.mock(Payload.class);
        long paynkpdg = 1L;
        Exception e = Mockito.mock(Exception.class);
        IllegalStateException e1 = Mockito.mock(IllegalStateException.class);
        String vosxcmie = "Foo";
        String message = "Bar";
        int jobTakeTimeout = 1;
        String memberId = "Baz";

        FieldUtils.writeDeclaredField(jobStore, "jobTakeTimeout",
                jobTakeTimeout, true);
        FieldUtils.writeDeclaredField(jobStore, "memberId", memberId, true);

        when(metricsHelper.getTimer(jobStore, "job", "take", "time"))
                .thenReturn(apple);
        when(apple.time()).thenReturn(timer);
        when(jobTakeThrottle.tryAcquire(jobTakeTimeout, TimeUnit.MILLISECONDS))
                .thenReturn(acquired);
        when(jobsList.size()).thenReturn(size);
        when(hz.newTransactionContext(txOptions)).thenReturn(tx);
        when(random.nextInt(windowSize)).thenReturn(offset);
        when(random.nextInt(size)).thenReturn(offset);
        when(jobsList.get(index)).thenReturn(cJob);
        when(tx.getList(DsName.JOBS_LIST.toString())).thenReturn(txJobsList);
        when(tx.getMap(DsName.TAKEN_JOBS_MAP.toString()))
                .thenReturn(txTakenJobsMap);
        when(tx.getMap(DsName.PAYLOADS_MAP.toString()))
                .thenReturn(txPayloadsMap);
        when(cJob.getJobId()).thenReturn(jobId).thenReturn(paynkpdg);
        when(txJobsList.remove(cJob)).thenReturn(lychee);
        when(txPayloadsMap.get(jobId)).thenReturn(payload);
        when(e1.getMessage()).thenReturn(vosxcmie);
        when(e.getMessage()).thenReturn(message);

        Payload actual = jobStore.takeJob();

        assertSame(payload, actual);
        verify(timer).stop();
        verify(jobTakeThrottle, never()).release();
        verify(tx).beginTransaction();
        verify(cJob).setTaken(true);
        verify(cJob).setMemberId(memberId);
        verify(txTakenJobsMap).set(jobId, cJob);
        verify(tx).commitTransaction();
        verify(tx, never()).rollbackTransaction();
        verify(timer, times(1)).stop();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTakeJobRemoveJobFailed() throws Exception {
        Timer apple = Mockito.mock(Timer.class);
        Context timer = Mockito.mock(Context.class);
        boolean acquired = true;

        TransactionContext tx = Mockito.mock(TransactionContext.class);
        int windowSize = 10;
        int offset = 1;
        // to cover getLifoIndex()
        int size = 20;
        int index = 18;

        ClusterJob cJob = Mockito.mock(ClusterJob.class);
        TransactionalList<Object> txJobsList =
                Mockito.mock(TransactionalList.class);
        TransactionalMap<Object, Object> txTakenJobsMap =
                Mockito.mock(TransactionalMap.class);
        TransactionalMap<Object, Object> txPayloadsMap =
                Mockito.mock(TransactionalMap.class);
        long jobId = 1L;
        boolean lychee = false; // job to remove not found
        Payload payload = Mockito.mock(Payload.class);
        long paynkpdg = 1L;
        Exception e = Mockito.mock(Exception.class);
        IllegalStateException e1 = Mockito.mock(IllegalStateException.class);
        String vosxcmie = "Foo";
        String message = "Bar";
        int jobTakeTimeout = 1;
        String memberId = "Baz";

        FieldUtils.writeDeclaredField(jobStore, "jobTakeTimeout",
                jobTakeTimeout, true);
        FieldUtils.writeDeclaredField(jobStore, "memberId", memberId, true);

        when(metricsHelper.getTimer(jobStore, "job", "take", "time"))
                .thenReturn(apple);
        when(apple.time()).thenReturn(timer);
        when(jobTakeThrottle.tryAcquire(jobTakeTimeout, TimeUnit.MILLISECONDS))
                .thenReturn(acquired);
        when(jobsList.size()).thenReturn(size);
        when(hz.newTransactionContext(txOptions)).thenReturn(tx);
        when(random.nextInt(windowSize)).thenReturn(offset);
        when(random.nextInt(size)).thenReturn(offset);
        when(jobsList.get(index)).thenReturn(cJob);
        when(tx.getList(DsName.JOBS_LIST.toString())).thenReturn(txJobsList);
        when(tx.getMap(DsName.TAKEN_JOBS_MAP.toString()))
                .thenReturn(txTakenJobsMap);
        when(tx.getMap(DsName.PAYLOADS_MAP.toString()))
                .thenReturn(txPayloadsMap);
        when(cJob.getJobId()).thenReturn(jobId).thenReturn(paynkpdg);
        when(txJobsList.remove(cJob)).thenReturn(lychee);
        when(txPayloadsMap.get(jobId)).thenReturn(payload);
        when(e1.getMessage()).thenReturn(vosxcmie);
        when(e.getMessage()).thenReturn(message);

        assertThrows(TransactionException.class, () -> jobStore.takeJob());

        verify(jobTakeThrottle, times(1)).release();
        verify(tx).beginTransaction();
        verify(cJob, never()).setTaken(true);
        verify(cJob, never()).setMemberId(memberId);
        verify(txTakenJobsMap, never()).set(jobId, cJob);
        verify(tx, never()).commitTransaction();
        verify(tx).rollbackTransaction();
        verify(timer, times(1)).stop();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTakeJobPayloadNotFound() throws Exception {
        Timer apple = Mockito.mock(Timer.class);
        Context timer = Mockito.mock(Context.class);
        boolean acquired = true;
        int size = 1;
        TransactionContext tx = Mockito.mock(TransactionContext.class);
        int windowSize = 10;
        int offset = 1;
        int index = 0;
        ClusterJob cJob = Mockito.mock(ClusterJob.class);
        TransactionalList<Object> txJobsList =
                Mockito.mock(TransactionalList.class);
        TransactionalMap<Object, Object> txTakenJobsMap =
                Mockito.mock(TransactionalMap.class);
        TransactionalMap<Object, Object> txPayloadsMap =
                Mockito.mock(TransactionalMap.class);
        long jobId = 1L;
        boolean lychee = true;
        Payload payload = null; // payload is null
        long paynkpdg = 1L;
        Exception e = Mockito.mock(Exception.class);
        IllegalStateException e1 = Mockito.mock(IllegalStateException.class);
        String vosxcmie = "Foo";
        String message = "Bar";
        int jobTakeTimeout = 1;
        String memberId = "Baz";

        FieldUtils.writeDeclaredField(jobStore, "jobTakeTimeout",
                jobTakeTimeout, true);
        FieldUtils.writeDeclaredField(jobStore, "memberId", memberId, true);

        when(metricsHelper.getTimer(jobStore, "job", "take", "time"))
                .thenReturn(apple);
        when(apple.time()).thenReturn(timer);
        when(jobTakeThrottle.tryAcquire(jobTakeTimeout, TimeUnit.MILLISECONDS))
                .thenReturn(acquired);
        when(jobsList.size()).thenReturn(size);
        when(hz.newTransactionContext(txOptions)).thenReturn(tx);
        when(random.nextInt(windowSize)).thenReturn(offset);
        when(random.nextInt(size)).thenReturn(offset);
        when(jobsList.get(index)).thenReturn(cJob);
        when(tx.getList(DsName.JOBS_LIST.toString())).thenReturn(txJobsList);
        when(tx.getMap(DsName.TAKEN_JOBS_MAP.toString()))
                .thenReturn(txTakenJobsMap);
        when(tx.getMap(DsName.PAYLOADS_MAP.toString()))
                .thenReturn(txPayloadsMap);
        when(cJob.getJobId()).thenReturn(jobId).thenReturn(paynkpdg);
        when(txJobsList.remove(cJob)).thenReturn(lychee);
        when(txPayloadsMap.get(jobId)).thenReturn(payload);
        when(e1.getMessage()).thenReturn(vosxcmie);
        when(e.getMessage()).thenReturn(message);
        // for test coverage
        doThrow(IllegalStateException.class).when(tx).rollbackTransaction();

        assertThrows(TransactionException.class, () -> jobStore.takeJob());

        verify(jobTakeThrottle, times(1)).release();
        verify(tx).beginTransaction();
        verify(cJob).setTaken(true);
        verify(cJob).setMemberId(memberId);
        verify(txTakenJobsMap).set(jobId, cJob);
        verify(tx, never()).commitTransaction();
        verify(tx).rollbackTransaction();
        verify(timer, times(1)).stop();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMarkFinished() throws Exception {
        long jobId = 1L;
        TransactionContext tx = Mockito.mock(TransactionContext.class);
        TransactionalMap<Object, Object> txTakenJobsMap =
                Mockito.mock(TransactionalMap.class);
        TransactionalMap<Object, Object> txPayloadsMap =
                Mockito.mock(TransactionalMap.class);
        ClusterJob orange = Mockito.mock(ClusterJob.class);

        int cherry = 1;
        FieldUtils.writeDeclaredField(jobStore, "jobTakeLimit", 2, true);

        when(hz.newTransactionContext(txOptions)).thenReturn(tx);
        when(tx.getMap(DsName.TAKEN_JOBS_MAP.toString()))
                .thenReturn(txTakenJobsMap);
        when(tx.getMap(DsName.PAYLOADS_MAP.toString()))
                .thenReturn(txPayloadsMap);
        when(txTakenJobsMap.remove(jobId)).thenReturn(orange);
        when(jobTakeThrottle.availablePermits()).thenReturn(cherry);

        boolean actual = jobStore.markFinished(jobId);

        assertTrue(actual);
        verify(tx).beginTransaction();
        verify(txPayloadsMap).delete(jobId);
        verify(tx).commitTransaction();
        verify(jobTakeThrottle).release();
        verify(tx, never()).rollbackTransaction();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMarkFinishedIgnoreDeleteException() throws Exception {
        long jobId = 1L;
        TransactionContext tx = Mockito.mock(TransactionContext.class);
        TransactionalMap<Object, Object> txTakenJobsMap =
                Mockito.mock(TransactionalMap.class);
        TransactionalMap<Object, Object> txPayloadsMap =
                Mockito.mock(TransactionalMap.class);
        ClusterJob orange = Mockito.mock(ClusterJob.class);

        int cherry = 1;
        FieldUtils.writeDeclaredField(jobStore, "jobTakeLimit", 2, true);

        when(hz.newTransactionContext(txOptions)).thenReturn(tx);
        when(tx.getMap(DsName.TAKEN_JOBS_MAP.toString()))
                .thenReturn(txTakenJobsMap);
        when(tx.getMap(DsName.PAYLOADS_MAP.toString()))
                .thenReturn(txPayloadsMap);
        when(txTakenJobsMap.remove(jobId)).thenReturn(orange);
        when(jobTakeThrottle.availablePermits()).thenReturn(cherry);
        // ignored exception
        doThrow(TransactionTimedOutException.class).when(txPayloadsMap)
                .delete(jobId);

        boolean actual = jobStore.markFinished(jobId);

        assertTrue(actual);
        verify(tx).beginTransaction();
        verify(tx).commitTransaction();
        verify(jobTakeThrottle).release();
        verify(tx, never()).rollbackTransaction();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMarkFinishedJobTakeThrottleNotReleased() throws Exception {
        long jobId = 1L;
        TransactionContext tx = Mockito.mock(TransactionContext.class);
        TransactionalMap<Object, Object> txTakenJobsMap =
                Mockito.mock(TransactionalMap.class);
        TransactionalMap<Object, Object> txPayloadsMap =
                Mockito.mock(TransactionalMap.class);
        ClusterJob orange = Mockito.mock(ClusterJob.class);

        int cherry = 2;
        FieldUtils.writeDeclaredField(jobStore, "jobTakeLimit", 2, true);

        when(hz.newTransactionContext(txOptions)).thenReturn(tx);
        when(tx.getMap(DsName.TAKEN_JOBS_MAP.toString()))
                .thenReturn(txTakenJobsMap);
        when(tx.getMap(DsName.PAYLOADS_MAP.toString()))
                .thenReturn(txPayloadsMap);
        when(txTakenJobsMap.remove(jobId)).thenReturn(orange);
        when(jobTakeThrottle.availablePermits()).thenReturn(cherry);

        boolean actual = jobStore.markFinished(jobId);

        assertTrue(actual);
        verify(tx).beginTransaction();
        verify(txPayloadsMap).delete(jobId);
        verify(tx).commitTransaction();
        verify(jobTakeThrottle, never()).release();
        verify(tx, never()).rollbackTransaction();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMarkFinishedJobNotFound() throws Exception {
        long jobId = 1L;
        TransactionContext tx = Mockito.mock(TransactionContext.class);
        TransactionalMap<Object, Object> txTakenJobsMap =
                Mockito.mock(TransactionalMap.class);
        TransactionalMap<Object, Object> txPayloadsMap =
                Mockito.mock(TransactionalMap.class);
        ClusterJob orange = null; // job not found
        int cherry = 1;

        when(hz.newTransactionContext(txOptions)).thenReturn(tx);
        when(tx.getMap(DsName.TAKEN_JOBS_MAP.toString()))
                .thenReturn(txTakenJobsMap);
        when(tx.getMap(DsName.PAYLOADS_MAP.toString()))
                .thenReturn(txPayloadsMap);
        when(txTakenJobsMap.remove(jobId)).thenReturn(orange);
        when(jobTakeThrottle.availablePermits()).thenReturn(cherry);

        assertThrows(JobStateException.class,
                () -> jobStore.markFinished(jobId));

        verify(tx).beginTransaction();
        verify(txPayloadsMap).delete(jobId);
        verify(tx, never()).commitTransaction();
        verify(jobTakeThrottle, never()).release();
        verify(tx).rollbackTransaction();
    }

    @Test
    public void testMarkFinishedException() throws Exception {
        long jobId = 1L;
        TransactionContext tx = Mockito.mock(TransactionContext.class);

        when(hz.newTransactionContext(txOptions)).thenReturn(tx);
        doThrow(IllegalStateException.class).when(tx).beginTransaction();

        assertThrows(TransactionException.class,
                () -> jobStore.markFinished(jobId));

        verify(tx).beginTransaction();
        verify(tx, never()).commitTransaction();
        verify(tx).rollbackTransaction();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testResetTakenJob() throws Exception {
        long jobId = 1L;
        TransactionContext tx = Mockito.mock(TransactionContext.class);
        TransactionalList<Object> txJobsList =
                Mockito.mock(TransactionalList.class);
        TransactionalMap<Object, Object> txTakenJobsMap =
                Mockito.mock(TransactionalMap.class);
        ClusterJob cJob = Mockito.mock(ClusterJob.class);

        int orange = 2;
        FieldUtils.writeDeclaredField(jobStore, "jobTakeLimit", 2, true);

        when(hz.newTransactionContext(txOptions)).thenReturn(tx);
        when(tx.getList(DsName.JOBS_LIST.toString())).thenReturn(txJobsList);
        when(tx.getMap(DsName.TAKEN_JOBS_MAP.toString()))
                .thenReturn(txTakenJobsMap);
        when(txTakenJobsMap.remove(jobId)).thenReturn(cJob);
        when(jobTakeThrottle.availablePermits()).thenReturn(orange);

        boolean actual = jobStore.resetTakenJob(jobId);

        assertTrue(actual);
        verify(tx).beginTransaction();
        verify(cJob).setTaken(false);
        verify(cJob).setMemberId(null);
        verify(txJobsList).add(cJob);
        verify(tx).commitTransaction();
        verify(jobTakeThrottle, never()).release();
        verify(tx, never()).rollbackTransaction();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testResetTakenJobReleaseJobTakeThrottle() throws Exception {
        long jobId = 1L;
        TransactionContext tx = Mockito.mock(TransactionContext.class);
        TransactionalList<Object> txJobsList =
                Mockito.mock(TransactionalList.class);
        TransactionalMap<Object, Object> txTakenJobsMap =
                Mockito.mock(TransactionalMap.class);
        ClusterJob cJob = Mockito.mock(ClusterJob.class);

        int orange = 1;
        FieldUtils.writeDeclaredField(jobStore, "jobTakeLimit", 2, true);

        when(hz.newTransactionContext(txOptions)).thenReturn(tx);
        when(tx.getList(DsName.JOBS_LIST.toString())).thenReturn(txJobsList);
        when(tx.getMap(DsName.TAKEN_JOBS_MAP.toString()))
                .thenReturn(txTakenJobsMap);
        when(txTakenJobsMap.remove(jobId)).thenReturn(cJob);
        when(jobTakeThrottle.availablePermits()).thenReturn(orange);

        boolean actual = jobStore.resetTakenJob(jobId);

        assertTrue(actual);
        verify(tx).beginTransaction();
        verify(cJob).setTaken(false);
        verify(cJob).setMemberId(null);
        verify(txJobsList).add(cJob);
        verify(tx).commitTransaction();
        verify(jobTakeThrottle).release();
        verify(tx, never()).rollbackTransaction();
    }

    @Test
    public void testResetTakenJobException() throws Exception {
        long jobId = 1L;
        TransactionContext tx = Mockito.mock(TransactionContext.class);

        when(hz.newTransactionContext(txOptions)).thenReturn(tx);
        doThrow(IllegalStateException.class).when(tx).beginTransaction();

        boolean actual = jobStore.resetTakenJob(jobId);

        assertFalse(actual);
        verify(tx).beginTransaction();
        verify(tx, never()).commitTransaction();
        verify(tx).rollbackTransaction();
    }

    @Test
    public void testIsDone() {
        when(jobsList.isEmpty()).thenReturn(true).thenReturn(true)
                .thenReturn(false);
        when(takenJobsMap.isEmpty()).thenReturn(true).thenReturn(false)
                .thenReturn(true);

        assertTrue(jobStore.isDone());
        assertFalse(jobStore.isDone());
        assertFalse(jobStore.isDone());
    }

    @Test
    public void testSetState() {
        State state = IJobStore.State.INITIALIZE;

        jobStore.setState(state);

        verify(keyStoreMap).put(DsName.DATA_GRID_STATE.toString(),
                state.toString());
    }

    @Test
    public void testGetJobIdSeq() {
        long apple = 1L;

        when(jobIdGenerator.newId()).thenReturn(apple);

        long actual = jobStore.getJobIdSeq();

        assertEquals(apple, actual);
    }

    @Test
    public void testResetCrashedJobs() throws Exception {
        boolean apple = true;
        int grape = 1;

        int startCrashCleanerMinThreshold = 2;
        FieldUtils.writeDeclaredField(jobStore, "startCrashCleanerMinThreshold",
                startCrashCleanerMinThreshold, true);

        when(crashCleaner.hasCrashedMembers()).thenReturn(apple);
        when(jobsList.size()).thenReturn(grape);

        jobStore.resetCrashedJobs();

        verify(crashCleaner).resetCrashedJobs();
    }

    @Test
    public void testResetCrashedJobsNoCrashedMembers() throws Exception {
        int grape = 2;
        int startCrashCleanerMinThreshold = 2;
        FieldUtils.writeDeclaredField(jobStore, "startCrashCleanerMinThreshold",
                startCrashCleanerMinThreshold, true);

        when(crashCleaner.hasCrashedMembers()).thenReturn(false)
                .thenReturn(true);
        when(jobsList.size()).thenReturn(grape);

        jobStore.resetCrashedJobs();
        jobStore.resetCrashedJobs(); // or list size not less than threshold

        verify(crashCleaner, never()).resetCrashedJobs();
    }
}
