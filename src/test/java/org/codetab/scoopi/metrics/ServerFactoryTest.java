package org.codetab.scoopi.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class ServerFactoryTest {

    @InjectMocks
    private ServerFactory serverFactory;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateServer() throws Exception {
        int port = 9010;
        Server server = serverFactory.createServer(port);

        assertThat(server).isNotNull();
        server.start();

        int actualPort =
                ((ServerConnector) server.getConnectors()[0]).getLocalPort();

        assertThat(actualPort).isEqualTo(port);
    }

    @Test
    public void testCreateWebAppContext() {
        WebAppContext actual = serverFactory.createWebAppContext();
        assertThat(actual).isNotNull();
    }

}
