package org.codetab.scoopi.store.cluster.hz;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;

public class HazelcastConfigTest {
    @InjectMocks
    private HazelcastConfig hazelcastConfig;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @After
    public void teardown() throws Exception {
        System.clearProperty("hazelcast.foo");
    }

    @Test
    public void testGetConfig() {
        String fileName = "/hazelcast.xml";

        Config actual = hazelcastConfig.getConfig(fileName);

        assertEquals("scoopi", actual.getClusterName());
        assertEquals("GRACEFUL",
                actual.getProperty("hazelcast.shutdownhook.policy"));
    }

    @Test
    public void testGetClientConfig() {
        String fileName = "/hazelcast-client.xml";

        ClientConfig actual = hazelcastConfig.getClientConfig(fileName);

        assertEquals("scoopi", actual.getClusterName());
        assertEquals("GRACEFUL",
                actual.getProperty("hazelcast.shutdownhook.policy"));
    }

    @Test
    public void testGetHazelcastSystemProperties() {
        System.setProperty("hazelcast.foo", "bar");
        Properties properties = new Properties();
        properties.setProperty("hazelcast.foo", "bar");

        Properties actual = hazelcastConfig.getHazelcastSystemProperties();

        assertEquals(properties, actual);
    }
}
