package org.codetab.scoopi.store.cluster.hz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.CriticalException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.cluster.Address;
import com.hazelcast.cluster.Endpoint;
import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.impl.MemberImpl;
import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.LifecycleService;
import com.hazelcast.map.IMap;
import com.hazelcast.transaction.TransactionOptions;
import com.hazelcast.transaction.TransactionOptions.TransactionType;

public class ClusterTest {
    @InjectMocks
    private Cluster cluster;

    @Mock
    private MembershipListener membershipListener;
    @Mock
    private HazelcastConfig hazelcastConfig;
    @Mock
    private Factory factory;
    @Mock
    private HazelcastInstance hz;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testStartServer() throws Exception {
        String clusterMode = "server";
        String configFileName = "Bar";
        String configFile = "Bar";
        Properties properties = Mockito.mock(Properties.class);
        Config cfg = Mockito.mock(Config.class);
        ListenerConfig orange = Mockito.mock(ListenerConfig.class);
        String mango = "Qux";
        com.hazelcast.cluster.Cluster banana =
                Mockito.mock(com.hazelcast.cluster.Cluster.class);
        Set<Member> members = new HashSet<>();
        Member localMember =
                new MemberImpl(new Address("localhost", 20000), null, true);
        Member nonLocalMember =
                new MemberImpl(new Address("localhost", 20001), null, false);
        members.add(localMember);
        members.add(nonLocalMember);

        HazelcastInstance hz1 = Mockito.mock(HazelcastInstance.class);

        when(hazelcastConfig.getHazelcastSystemProperties())
                .thenReturn(properties);
        when(hazelcastConfig.getConfig(configFile)).thenReturn(cfg);
        when(factory.createListenerConfig(membershipListener))
                .thenReturn(orange);
        when(factory.createHazelcastInstance(cfg)).thenReturn(hz1);
        when(cfg.getClusterName()).thenReturn(mango);
        when(hz1.getCluster()).thenReturn(banana);
        when(banana.getMembers()).thenReturn(members);

        cluster.start(clusterMode, configFileName);

        assertEquals(hz1, cluster.getInstance());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<BiConsumer<? super Object, ? super Object>> argcA =
                ArgumentCaptor.forClass(BiConsumer.class);

        verify(cfg).addListenerConfig(orange);
        verify(properties).forEach(argcA.capture());
        verify(cfg).addListenerConfig(orange);
    }

    @Test
    public void testStartServerConfigFileIsNull() throws Exception {
        String clusterMode = "server";
        String configFileName = null;
        String configFile = "/hazelcast.xml";
        // also test properties.forEach() lambda
        Properties properties = new Properties();
        properties.setProperty("foo", "bar");
        Config cfg = Mockito.mock(Config.class);
        ListenerConfig orange = Mockito.mock(ListenerConfig.class);
        String mango = "Qux";
        com.hazelcast.cluster.Cluster banana =
                Mockito.mock(com.hazelcast.cluster.Cluster.class);
        Set<Member> members = new HashSet<>();
        Member localMember =
                new MemberImpl(new Address("localhost", 20000), null, true);
        members.add(localMember);

        HazelcastInstance hz1 = Mockito.mock(HazelcastInstance.class);

        when(hazelcastConfig.getHazelcastSystemProperties())
                .thenReturn(properties);
        when(hazelcastConfig.getConfig(configFile)).thenReturn(cfg);
        when(factory.createListenerConfig(membershipListener))
                .thenReturn(orange);
        when(factory.createHazelcastInstance(cfg)).thenReturn(hz1);
        when(cfg.getClusterName()).thenReturn(mango);
        when(hz1.getCluster()).thenReturn(banana);
        when(banana.getMembers()).thenReturn(members);

        cluster.start(clusterMode, configFileName);

        assertEquals(hz1, cluster.getInstance());

        verify(cfg).addListenerConfig(orange);
        verify(cfg).setProperty("foo", "bar");
        verify(cfg).addListenerConfig(orange);
    }

    @Test
    public void testStartClient() throws Exception {
        String clusterMode = "client";
        String configFileName = "Bar";
        String configFile = "Bar";
        Properties properties = Mockito.mock(Properties.class);
        ClientConfig clientCfg = Mockito.mock(ClientConfig.class);
        ListenerConfig orange = Mockito.mock(ListenerConfig.class);
        String mango = "Qux";
        com.hazelcast.cluster.Cluster banana =
                Mockito.mock(com.hazelcast.cluster.Cluster.class);
        Set<Member> members = new HashSet<>();
        Member nonLocalMember =
                new MemberImpl(new Address("localhost", 20000), null, false);
        members.add(nonLocalMember);

        HazelcastInstance hz1 = Mockito.mock(HazelcastInstance.class);

        when(hazelcastConfig.getClientConfig(configFile)).thenReturn(clientCfg);
        when(factory.createHazelcastClientInstance(clientCfg)).thenReturn(hz1);
        when(clientCfg.getClusterName()).thenReturn(mango);
        when(hazelcastConfig.getHazelcastSystemProperties())
                .thenReturn(properties);
        when(factory.createListenerConfig(membershipListener))
                .thenReturn(orange);
        when(hz1.getCluster()).thenReturn(banana);
        when(banana.getMembers()).thenReturn(members);

        cluster.start(clusterMode, configFileName);

        assertEquals(hz1, cluster.getInstance());

        verify(clientCfg).addListenerConfig(orange);
    }

    @Test
    public void testStartClientConfigFileIsNull() throws Exception {
        String clusterMode = "client";
        String configFileName = null;
        String configFile = "/hazelcast-client.xml";
        Properties properties = Mockito.mock(Properties.class);
        ClientConfig clientCfg = Mockito.mock(ClientConfig.class);
        ListenerConfig orange = Mockito.mock(ListenerConfig.class);
        String mango = "Qux";
        com.hazelcast.cluster.Cluster banana =
                Mockito.mock(com.hazelcast.cluster.Cluster.class);
        Set<Member> members = new HashSet<>();
        Member localMember =
                new MemberImpl(new Address("localhost", 20000), null, true);
        members.add(localMember);

        HazelcastInstance hz1 = Mockito.mock(HazelcastInstance.class);

        when(hazelcastConfig.getClientConfig(configFile)).thenReturn(clientCfg);
        when(factory.createHazelcastClientInstance(clientCfg)).thenReturn(hz1);
        when(clientCfg.getClusterName()).thenReturn(mango);
        when(hazelcastConfig.getHazelcastSystemProperties())
                .thenReturn(properties);
        when(factory.createListenerConfig(membershipListener))
                .thenReturn(orange);
        when(hz1.getCluster()).thenReturn(banana);
        when(banana.getMembers()).thenReturn(members);

        cluster.start(clusterMode, configFileName);

        assertEquals(hz1, cluster.getInstance());

        verify(clientCfg).addListenerConfig(orange);
    }

    @Test
    public void testStartException() throws Exception {
        String configFileName = "Bar";
        String configFile = "Bar";
        Properties properties = Mockito.mock(Properties.class);

        when(hazelcastConfig.getHazelcastSystemProperties())
                .thenReturn(properties);
        when(hazelcastConfig.getConfig(configFile))
                .thenThrow(IllegalArgumentException.class);
        when(hazelcastConfig.getClientConfig(configFile))
                .thenThrow(IllegalArgumentException.class);

        assertThrows(CriticalException.class,
                () -> cluster.start("server", configFileName));
        assertThrows(CriticalException.class,
                () -> cluster.start("client", configFileName));

    }

    @Test
    public void testShutdown() throws IllegalAccessException {
        cluster.shutdown();

        verify(hz).shutdown();
    }

    @Test
    public void testGetInstance() throws IllegalAccessException {
        Object actual = cluster.getInstance();
        assertEquals(hz, actual);
    }

    @Test
    public void testGetMemberId() throws IllegalAccessException {
        Endpoint apple = Mockito.mock(Endpoint.class);
        UUID grape = Mockito.mock(UUID.class);
        String orange = "Foo";

        when(hz.getLocalEndpoint()).thenReturn(apple);
        when(apple.getUuid()).thenReturn(grape);
        when(grape.toString()).thenReturn(orange);

        String actual = cluster.getMemberId();

        assertEquals(orange, actual);
    }

    @Test
    public void testGetShortId() {
        String memberId = "Foo-Bar-Quz";
        String orange = "Quz";

        String actual = cluster.getShortId(memberId);

        assertEquals(orange, actual);
    }

    @Test
    public void testGetMetricsHolder() {
        @SuppressWarnings("unchecked")
        IMap<Object, Object> apple = Mockito.mock(IMap.class);

        when(hz.getMap("metrics")).thenReturn(apple);

        Map<String, byte[]> actual = cluster.getMetricsHolder();

        assertSame(apple, actual);
    }

    @Test
    public void testGetLeader() throws Exception {
        com.hazelcast.cluster.Cluster apple =
                Mockito.mock(com.hazelcast.cluster.Cluster.class);
        Set<Member> grape = new HashSet<>();
        Member member = Mockito.mock(Member.class);
        grape.add(member);

        UUID cherry = new UUID(123, 789);

        when(hz.getCluster()).thenReturn(apple);
        when(apple.getMembers()).thenReturn(grape);
        when(member.getUuid()).thenReturn(cherry);

        String actual = cluster.getLeader();

        assertEquals(cherry.toString(), actual);
    }

    @Test
    public void testGetLeaderException() throws Exception {
        com.hazelcast.cluster.Cluster apple =
                Mockito.mock(com.hazelcast.cluster.Cluster.class);
        Set<Member> grape = new HashSet<>();

        when(hz.getCluster()).thenReturn(apple);
        when(apple.getMembers()).thenReturn(grape);

        assertThrows(IllegalStateException.class, () -> cluster.getLeader());
    }

    @Test
    public void testGetTxOptions() {
        Configs configs = Mockito.mock(Configs.class);
        int txTimeout = 1;
        String grape = "Minutes";
        TimeUnit timeUnit = TimeUnit.valueOf(grape.toUpperCase());
        String kiwi = "two_phase";
        TransactionType txType = TransactionType.valueOf(kiwi.toUpperCase());
        TransactionOptions banana = Mockito.mock(TransactionOptions.class);
        TransactionOptions cherry = Mockito.mock(TransactionOptions.class);
        TransactionOptions apricot = Mockito.mock(TransactionOptions.class);

        when(configs.getInt("scoopi.cluster.tx.timeout", "10"))
                .thenReturn(txTimeout);
        when(configs.getConfig("scoopi.cluster.tx.timeoutUnit", "SECONDS"))
                .thenReturn(grape);
        when(configs.getConfig("scoopi.cluster.tx.type", "TWO_PHASE"))
                .thenReturn(kiwi);
        when(factory.createTxOptions()).thenReturn(banana);
        when(banana.setTransactionType(txType)).thenReturn(cherry);
        when(cherry.setTimeout(txTimeout, timeUnit)).thenReturn(apricot);

        Object actual = cluster.getTxOptions(configs);

        assertSame(apricot, actual);
    }

    @Test
    public void testGetSize() {
        com.hazelcast.cluster.Cluster apple =
                Mockito.mock(com.hazelcast.cluster.Cluster.class);
        Set<Member> grape = new HashSet<>();
        grape.add(Mockito.mock(Member.class));
        grape.add(Mockito.mock(Member.class));
        grape.add(Mockito.mock(Member.class));
        grape.add(Mockito.mock(Member.class));
        int orange = 4;

        when(hz.getCluster()).thenReturn(apple);
        when(apple.getMembers()).thenReturn(grape);

        int actual = cluster.getSize();

        assertEquals(orange, actual);
    }

    @Test
    public void testIsNodeRunning() {
        LifecycleService apple = Mockito.mock(LifecycleService.class);

        when(hz.getLifecycleService()).thenReturn(apple);
        when(apple.isRunning()).thenReturn(true).thenReturn(false);

        assertTrue(cluster.isNodeRunning());
        assertFalse(cluster.isNodeRunning());
    }
}
