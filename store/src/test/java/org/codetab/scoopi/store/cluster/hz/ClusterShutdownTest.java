package org.codetab.scoopi.store.cluster.hz;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.scoopi.store.ICluster;
import org.codetab.scoopi.store.IJobStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.map.IMap;

public class ClusterShutdownTest {
    @InjectMocks
    private ClusterShutdown clusterShutdown;

    @Mock
    private ICluster cluster;
    @Mock
    private IJobStore jobStore;
    @Mock
    private HazelcastInstance hz;
    @Mock
    private com.hazelcast.cluster.Cluster clst;
    @Mock
    private AtomicBoolean cancelled;

    @Mock
    private IMap<Object, Object> doneMap;
    @Mock
    private IMap<Object, Object> terminateMap;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInit() {
        HazelcastInstance hz1 = Mockito.mock(HazelcastInstance.class);
        com.hazelcast.cluster.Cluster clst1 =
                Mockito.mock(com.hazelcast.cluster.Cluster.class);
        String memberId = "Foo";

        when(cluster.getInstance()).thenReturn(hz1);
        when(hz1.getCluster()).thenReturn(clst1);
        when(cluster.getMemberId()).thenReturn(memberId);
        when(hz1.getMap(DsName.MEMBER_DONE_MAP.toString())).thenReturn(doneMap);
        when(hz1.getMap(DsName.MEMBER_TERMINATE_MAP.toString()))
                .thenReturn(terminateMap);
        clusterShutdown.init();
    }

    @Test
    public void testSetDone() throws Exception {
        boolean apple = true;

        String memberId = "foo";
        FieldUtils.writeDeclaredField(clusterShutdown, "memberId", memberId,
                true);
        when(cluster.isNodeRunning()).thenReturn(apple);

        clusterShutdown.setDone();

        verify(doneMap).put(memberId, true);
    }

    @Test
    public void testSetDoneNodeNotRunning() throws Exception {
        boolean apple = false;

        String memberId = "foo";
        FieldUtils.writeDeclaredField(clusterShutdown, "memberId", memberId,
                true);
        when(cluster.isNodeRunning()).thenReturn(apple);

        clusterShutdown.setDone();

        verify(doneMap, never()).put(memberId, true);
    }

    @Test
    public void testSetTerminate() throws Exception {
        boolean apple = true;

        String memberId = "foo";
        FieldUtils.writeDeclaredField(clusterShutdown, "memberId", memberId,
                true);
        when(cluster.isNodeRunning()).thenReturn(apple);

        clusterShutdown.setTerminate();

        verify(terminateMap).put(memberId, true);
    }

    @Test
    public void testSetTerminateNodeNotRunning() throws Exception {
        boolean apple = false; // not running

        String memberId = "foo";
        FieldUtils.writeDeclaredField(clusterShutdown, "memberId", memberId,
                true);
        when(cluster.isNodeRunning()).thenReturn(apple);

        clusterShutdown.setTerminate();

        verify(terminateMap, never()).put(memberId, true);
    }

    @Test
    public void testSetTerminateException() throws Exception {
        String memberId = "foo";
        FieldUtils.writeDeclaredField(clusterShutdown, "memberId", memberId,
                true);
        HazelcastInstanceNotActiveException e =
                Mockito.mock(HazelcastInstanceNotActiveException.class);
        when(cluster.isNodeRunning()).thenReturn(true);
        doThrow(e).when(terminateMap).put(memberId, true);

        clusterShutdown.setTerminate();

        verify(e).getLocalizedMessage();
    }

    @Test
    public void testTerminate() {
        clusterShutdown.terminate();

        verify(hz).shutdown();
    }

    @Test
    public void testTerminateException() {
        doThrow(HazelcastInstanceNotActiveException.class).when(hz).shutdown();

        clusterShutdown.terminate();
    }

    @Test
    public void testCancel() {
        clusterShutdown.cancel();

        verify(cancelled).set(true);
    }

    @Test
    public void testAllNodesDone() {
        Set<Member> apple = new HashSet<>();

        when(clst.getMembers()).thenReturn(apple);

        boolean actual = clusterShutdown.allNodesDone();

        assertTrue(actual);
    }

    @Test
    public void testAllNodesNotDone() {
        Set<Member> apple = new HashSet<>();
        Member member = Mockito.mock(Member.class);
        apple.add(member);
        UUID cherry = new UUID(123, 789);

        when(clst.getMembers()).thenReturn(apple);
        when(member.getUuid()).thenReturn(cherry);

        boolean actual = clusterShutdown.allNodesDone();

        assertFalse(actual);
    }

    @Test
    public void testAllNodesDoneException() {
        when(clst.getMembers()).thenThrow(NullPointerException.class);

        boolean actual = clusterShutdown.allNodesDone();

        assertFalse(actual);
    }

    @Test
    public void testJobStoreDone() {
        boolean apple = true;

        when(jobStore.isDone()).thenReturn(apple);

        boolean actual = clusterShutdown.jobStoreDone();

        assertTrue(actual);
    }

    @Test
    public void testIsCancelled() {
        when(cancelled.get()).thenReturn(true);
        assertTrue(clusterShutdown.isCancelled());
    }
}
