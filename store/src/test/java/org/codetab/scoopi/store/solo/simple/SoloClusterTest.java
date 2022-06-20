package org.codetab.scoopi.store.solo.simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Optional;

import org.codetab.scoopi.config.Configs;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class SoloClusterTest {
    @InjectMocks
    private SoloCluster soloCluster;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetMemberId() {
        String apple = "solo";

        String actual = soloCluster.getMemberId();

        assertEquals(apple, actual);
    }

    @Test
    public void testGetShortId() {
        String memberId = "Foo";
        String apple = "solo";

        String actual = soloCluster.getShortId(memberId);

        assertEquals(apple, actual);
    }

    @Test
    public void testGetMetricsHolder() {

        Map<String, byte[]> actual = soloCluster.getMetricsHolder();

        assertEquals(0, actual.size());
        assertNotSame(actual, soloCluster.getMetricsHolder());
    }

    @Test
    public void testStart() {
        String clusterMode = "Foo";
        String configFileName = "Bar";
        soloCluster.start(clusterMode, configFileName);
    }

    @Test
    public void testShutdown() {
        soloCluster.shutdown();
    }

    @Test
    public void testGetInstance() {
        Optional<Object> apple = Optional.empty();

        Object actual = soloCluster.getInstance();

        assertEquals(apple, actual);
    }

    @Test
    public void testGetLeader() {
        String apple = "solo";

        String actual = soloCluster.getLeader();

        assertEquals(apple, actual);
    }

    @Test
    public void testGetTxOptions() {
        Configs configs = Mockito.mock(Configs.class);
        Optional<Object> apple = Optional.empty();

        Object actual = soloCluster.getTxOptions(configs);

        assertEquals(apple, actual);
    }

    @Test
    public void testGetSize() {
        int apple = 1;

        int actual = soloCluster.getSize();

        assertEquals(apple, actual);
    }

    @Test
    public void testIsNodeRunning() {

        boolean actual = soloCluster.isNodeRunning();

        assertTrue(actual);
    }
}
