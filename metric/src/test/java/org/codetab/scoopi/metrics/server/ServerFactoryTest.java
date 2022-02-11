package org.codetab.scoopi.metrics.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class ServerFactoryTest {
    @InjectMocks
    private ServerFactory serverFactory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateServer() throws Exception {
        int port = 9010;

        org.eclipse.jetty.util.log.StdErrLog logger =
                new org.eclipse.jetty.util.log.StdErrLog();
        logger.setLevel(org.eclipse.jetty.util.log.StdErrLog.LEVEL_OFF);
        org.eclipse.jetty.util.log.Log.setLog(logger);

        Server server = serverFactory.createServer(port);
        server.start();

        int actual =
                ((ServerConnector) server.getConnectors()[0]).getLocalPort();

        server.stop();

        assertEquals(port, actual);
    }

    @Test
    public void testCreateWebAppContext() {
        WebAppContext actual = serverFactory.createWebAppContext();

        assertNotNull(actual);
    }
}
