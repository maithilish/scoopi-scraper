package org.codetab.scoopi.store.cluster.hz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.transaction.TransactionOptions;

public class FactoryTest {
    @InjectMocks
    private Factory factory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateHazelcastInstance() {
        String fileName = "/hazelcast.xml";
        Config cfg = new XmlConfigBuilder(
                FactoryTest.class.getResourceAsStream(fileName)).build();
        HazelcastInstance server = factory.createHazelcastInstance(cfg); // server

        assertEquals(cfg.getClusterName(), server.getConfig().getClusterName());

        fileName = "/hazelcast-client.xml";
        ClientConfig clientCfg = new XmlClientConfigBuilder(
                FactoryTest.class.getResourceAsStream(fileName)).build();

        HazelcastInstance client =
                factory.createHazelcastClientInstance(clientCfg);

        assertEquals(clientCfg.getClusterName(),
                client.getConfig().getClusterName());
    }

    @Test
    public void testCreateListenerConfig() {
        MembershipListener membershipListener =
                Mockito.mock(MembershipListener.class);

        ListenerConfig actual =
                factory.createListenerConfig(membershipListener);

        assertSame(membershipListener, actual.getImplementation());
    }

    @Test
    public void testCreateTxOptions() {
        TransactionOptions actual = factory.createTxOptions();

        assertNotNull(actual);
    }
}
