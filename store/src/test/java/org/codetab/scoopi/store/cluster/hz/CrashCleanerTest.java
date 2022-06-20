package org.codetab.scoopi.store.cluster.hz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.model.ClusterJob;
import org.codetab.scoopi.store.ICluster;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.hazelcast.collection.IList;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;
import com.hazelcast.transaction.TransactionalList;
import com.hazelcast.transaction.TransactionalMap;

public class CrashCleanerTest {
    @InjectMocks
    private CrashCleaner crashCleaner;

    @Mock
    private Configs configs;
    @Mock
    private ICluster cluster;
    @Mock
    private CrashedMembers crashedMembers;
    @Mock
    private TransactionOptions txOptions;
    @Mock
    private HazelcastInstance hz;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInit() throws Exception {
        HazelcastInstance hz1 = Mockito.mock(HazelcastInstance.class);
        TransactionOptions txOptions1 = Mockito.mock(TransactionOptions.class);
        @SuppressWarnings("unchecked")
        IMap<Object, Object> takenJobsMap = Mockito.mock(IMap.class);

        when(cluster.getInstance()).thenReturn(hz1);
        when(cluster.getTxOptions(configs)).thenReturn(txOptions1);
        when(hz1.getMap(DsName.TAKEN_JOBS_MAP.toString()))
                .thenReturn(takenJobsMap);
        crashCleaner.init();

        assertSame(hz1, FieldUtils.readDeclaredField(crashCleaner, "hz", true));
        assertSame(txOptions1,
                FieldUtils.readDeclaredField(crashCleaner, "txOptions", true));
        assertSame(takenJobsMap, FieldUtils.readDeclaredField(crashCleaner,
                "takenJobsMap", true));
        @SuppressWarnings("rawtypes")
        Set clearedMembers = (Set) FieldUtils.readDeclaredField(crashCleaner,
                "clearedMembers", true);
        assertEquals(0, clearedMembers.size());
    }

    @Test
    public void testResetCrashedJobsEmpty() {
        when(crashedMembers.isEmpty()).thenReturn(true);

        boolean actual = crashCleaner.resetCrashedJobs();

        assertFalse(actual);
    }

    @Test
    public void testResetCrashedNotLeader() {
        String leader = "Foo";
        String grape = "Bar";

        when(crashedMembers.isEmpty()).thenReturn(false);
        when(cluster.getLeader()).thenReturn(leader);
        when(cluster.getMemberId()).thenReturn(grape);

        boolean actual = crashCleaner.resetCrashedJobs();

        assertFalse(actual);
    }

    @Test
    public void testResetCrashedAllCrashCleared() throws Exception {
        String leader = "Foo";
        String grape = "Foo";

        @SuppressWarnings("unchecked")
        Set<String> clearedMembers = Mockito.mock(Set.class);
        FieldUtils.writeDeclaredField(crashCleaner, "clearedMembers",
                clearedMembers, true);
        Set<String> notClearedMembers = new HashSet<>(); // all are cleared

        when(crashedMembers.isEmpty()).thenReturn(false);
        when(cluster.getLeader()).thenReturn(leader);
        when(cluster.getMemberId()).thenReturn(grape);
        when(crashedMembers.difference(clearedMembers))
                .thenReturn(notClearedMembers);

        boolean actual = crashCleaner.resetCrashedJobs();

        assertFalse(actual);
    }

    @Test
    public void testResetCrashedNoTakenJobByCrashedNode() throws Exception {
        String leader = "Foo";
        String grape = "Foo";
        String crashedMemberId = "Qux";

        @SuppressWarnings("unchecked")
        Set<String> clearedMembers = Mockito.mock(Set.class);
        FieldUtils.writeDeclaredField(crashCleaner, "clearedMembers",
                clearedMembers, true);

        Set<String> notClearedMembers = new HashSet<>();
        notClearedMembers.add(crashedMemberId);

        @SuppressWarnings("unchecked")
        IMap<Object, Object> takenJobsMap = Mockito.mock(IMap.class);
        FieldUtils.writeDeclaredField(crashCleaner, "takenJobsMap",
                takenJobsMap, true);

        List<Object> takenClusterJobs = new ArrayList<>();
        ClusterJob takenJob = Mockito.mock(ClusterJob.class);
        takenClusterJobs.add(takenJob);

        when(crashedMembers.isEmpty()).thenReturn(false);
        when(cluster.getLeader()).thenReturn(leader);
        when(cluster.getMemberId()).thenReturn(grape);
        when(crashedMembers.difference(clearedMembers))
                .thenReturn(notClearedMembers);
        when(takenJobsMap.values()).thenReturn(takenClusterJobs);
        when(takenJob.getMemberId()).thenReturn(crashedMemberId);
        when(takenJob.isTaken()).thenReturn(false); // not taken

        boolean actual = crashCleaner.resetCrashedJobs();

        assertFalse(actual);
    }

    @Test
    public void testResetCrashedJobTakenByAnotherMember() throws Exception {
        String leader = "Foo";
        String grape = "Foo";
        String crashedMemberId = "Qux";

        @SuppressWarnings("unchecked")
        Set<String> clearedMembers = Mockito.mock(Set.class);
        FieldUtils.writeDeclaredField(crashCleaner, "clearedMembers",
                clearedMembers, true);

        Set<String> notClearedMembers = new HashSet<>();
        notClearedMembers.add(crashedMemberId);

        @SuppressWarnings("unchecked")
        IMap<Object, Object> takenJobsMap = Mockito.mock(IMap.class);
        FieldUtils.writeDeclaredField(crashCleaner, "takenJobsMap",
                takenJobsMap, true);

        List<Object> takenClusterJobs = new ArrayList<>();
        ClusterJob takenJob = Mockito.mock(ClusterJob.class);
        takenClusterJobs.add(takenJob);

        when(crashedMembers.isEmpty()).thenReturn(false);
        when(cluster.getLeader()).thenReturn(leader);
        when(cluster.getMemberId()).thenReturn(grape);
        when(crashedMembers.difference(clearedMembers))
                .thenReturn(notClearedMembers);
        when(takenJobsMap.values()).thenReturn(takenClusterJobs);
        // taken but by another member
        when(takenJob.isTaken()).thenReturn(true);
        when(takenJob.getMemberId()).thenReturn("someOtherMember");

        boolean actual = crashCleaner.resetCrashedJobs();

        assertFalse(actual);
    }

    @Test
    public void testResetCrashed() throws Exception {
        String leader = "Foo";
        String grape = "Foo";
        String crashedMemberId = "Qux";

        @SuppressWarnings("unchecked")
        Set<String> clearedMembers = Mockito.mock(Set.class);
        FieldUtils.writeDeclaredField(crashCleaner, "clearedMembers",
                clearedMembers, true);

        Set<String> notClearedMembers = new HashSet<>();
        notClearedMembers.add(crashedMemberId);

        @SuppressWarnings("unchecked")
        IMap<Object, Object> takenJobsMap = Mockito.mock(IMap.class);
        FieldUtils.writeDeclaredField(crashCleaner, "takenJobsMap",
                takenJobsMap, true);

        List<Object> takenClusterJobs = new ArrayList<>();
        ClusterJob takenJob = Mockito.mock(ClusterJob.class);
        takenClusterJobs.add(takenJob);

        TransactionContext tx = Mockito.mock(TransactionContext.class);
        @SuppressWarnings("unchecked")
        TransactionalList<Object> txJobsList =
                Mockito.mock(TransactionalList.class);
        @SuppressWarnings("unchecked")
        TransactionalMap<Object, Object> txTakenJobsMap =
                Mockito.mock(TransactionalMap.class);
        Long jobId = Long.valueOf(1L);
        ClusterJob cJob = Mockito.mock(ClusterJob.class);

        when(crashedMembers.isEmpty()).thenReturn(false);
        when(cluster.getLeader()).thenReturn(leader);
        when(cluster.getMemberId()).thenReturn(grape);
        when(crashedMembers.difference(clearedMembers))
                .thenReturn(notClearedMembers);
        when(takenJobsMap.values()).thenReturn(takenClusterJobs);
        when(takenJob.getMemberId()).thenReturn(crashedMemberId);
        when(takenJob.getJobId()).thenReturn(jobId);
        when(takenJob.isTaken()).thenReturn(true);

        when(hz.newTransactionContext(txOptions)).thenReturn(tx);
        when(tx.getList(DsName.JOBS_LIST.toString())).thenReturn(txJobsList);
        when(tx.getMap(DsName.TAKEN_JOBS_MAP.toString()))
                .thenReturn(txTakenJobsMap);
        when(txTakenJobsMap.remove(jobId)).thenReturn(cJob);

        boolean actual = crashCleaner.resetCrashedJobs();

        assertTrue(actual);

        verify(tx).beginTransaction();
        verify(cJob).setTaken(false);
        verify(cJob).setMemberId(null);
        verify(txJobsList).add(cJob);
        verify(tx).commitTransaction();
        verify(tx, never()).rollbackTransaction();
    }

    @Test
    public void testResetCrashedException() throws Exception {
        String leader = "Foo";
        String grape = "Foo";
        String crashedMemberId = "Qux";

        @SuppressWarnings("unchecked")
        Set<String> clearedMembers = Mockito.mock(Set.class);
        FieldUtils.writeDeclaredField(crashCleaner, "clearedMembers",
                clearedMembers, true);

        Set<String> notClearedMembers = new HashSet<>();
        notClearedMembers.add(crashedMemberId);

        @SuppressWarnings("unchecked")
        IMap<Object, Object> takenJobsMap = Mockito.mock(IMap.class);
        FieldUtils.writeDeclaredField(crashCleaner, "takenJobsMap",
                takenJobsMap, true);

        List<Object> takenClusterJobs = new ArrayList<>();
        ClusterJob takenJob = Mockito.mock(ClusterJob.class);
        takenClusterJobs.add(takenJob);

        TransactionContext tx = Mockito.mock(TransactionContext.class);
        @SuppressWarnings("unchecked")
        TransactionalList<Object> txJobsList =
                Mockito.mock(TransactionalList.class);

        Long jobId = Long.valueOf(1L);
        ClusterJob cJob = Mockito.mock(ClusterJob.class);

        when(crashedMembers.isEmpty()).thenReturn(false);
        when(cluster.getLeader()).thenReturn(leader);
        when(cluster.getMemberId()).thenReturn(grape);
        when(crashedMembers.difference(clearedMembers))
                .thenReturn(notClearedMembers);
        when(takenJobsMap.values()).thenReturn(takenClusterJobs);
        when(takenJob.getMemberId()).thenReturn(crashedMemberId);
        when(takenJob.getJobId()).thenReturn(jobId);
        when(takenJob.isTaken()).thenReturn(true);

        when(hz.newTransactionContext(txOptions)).thenReturn(tx);
        doThrow(RuntimeException.class).when(tx).beginTransaction();

        boolean actual = crashCleaner.resetCrashedJobs();

        assertFalse(actual);

        verify(tx).beginTransaction();
        verify(cJob, never()).setTaken(false);
        verify(cJob, never()).setMemberId(null);
        verify(txJobsList, never()).add(cJob);
        verify(tx, never()).commitTransaction();
        verify(tx).rollbackTransaction();
    }

    @Test
    public void testClearDanglingJobs() {
        @SuppressWarnings("unchecked")
        IList<Object> jobsMap = Mockito.mock(IList.class);

        when(hz.getList(DsName.JOBS_LIST.toString())).thenReturn(jobsMap);
        crashCleaner.clearDanglingJobs();

        verify(jobsMap).clear();
    }

    @Test
    public void testHasCrashedMembers() {
        when(crashedMembers.isEmpty()).thenReturn(false).thenReturn(true);

        assertTrue(crashCleaner.hasCrashedMembers());
        assertFalse(crashCleaner.hasCrashedMembers());
    }
}
