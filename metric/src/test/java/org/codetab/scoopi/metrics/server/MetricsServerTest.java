package org.codetab.scoopi.metrics.server;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class MetricsServerTest {
    @InjectMocks
    private MetricsServer metricsServer;

    @Mock
    private Configs configs;
    @Mock
    private MetricsHelper metricsHelper;
    @Mock
    private ServerFactory factory;
    @Mock
    private Server server;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testStart() throws Exception {
        String apple = "9012";
        int port = 9012;
        URL kiwi = Mockito.mock(URL.class);
        String webappBase = "Bar";
        String descriptorPath = String.join("/", webappBase, "WEB-INF/web.xml");
        WebAppContext webappContext = Mockito.mock(WebAppContext.class);
        Server localServer = Mockito.mock(Server.class);
        ServerConnector connector = Mockito.mock(ServerConnector.class);
        Connector[] banana = {connector};

        Map<String, byte[]> metricsJsonData = new HashMap<>();
        metricsServer.setMetricsJsonData(metricsJsonData);

        when(configs.getConfig("scoopi.metrics.server.port")).thenReturn(apple);
        when(metricsHelper.getURL("/webapp")).thenReturn(kiwi);
        when(kiwi.toString()).thenReturn(webappBase);
        when(factory.createWebAppContext()).thenReturn(webappContext);
        when(factory.createServer(port)).thenReturn(localServer);
        when(localServer.getConnectors()).thenReturn(banana);
        when(connector.getLocalPort()).thenReturn(port);

        metricsServer.start();

        verify(webappContext).setContextPath("/");
        verify(webappContext).setResourceBase(webappBase);
        verify(webappContext).setDescriptor(descriptorPath);
        verify(webappContext).setAttribute("scoopi.metricsJsonData",
                metricsJsonData);
        verify(localServer).setHandler(webappContext);
        verify(localServer).start();
    }

    @Test
    public void testStartPortNotConfigured() throws Exception {
        int port = 9010;
        URL kiwi = Mockito.mock(URL.class);
        String webappBase = "Bar";
        String descriptorPath = String.join("/", webappBase, "WEB-INF/web.xml");
        WebAppContext webappContext = Mockito.mock(WebAppContext.class);
        Server localServer = Mockito.mock(Server.class);

        ServerConnector connector = Mockito.mock(ServerConnector.class);
        Connector[] banana = {connector};

        Map<String, byte[]> metricsJsonData = new HashMap<>();
        metricsServer.setMetricsJsonData(metricsJsonData);

        when(configs.getConfig("scoopi.metrics.server.port"))
                .thenThrow(ConfigNotFoundException.class);
        when(metricsHelper.getURL("/webapp")).thenReturn(kiwi);
        when(kiwi.toString()).thenReturn(webappBase);
        when(factory.createWebAppContext()).thenReturn(webappContext);
        when(factory.createServer(port)).thenReturn(localServer);
        when(localServer.getConnectors()).thenReturn(banana);
        when(connector.getLocalPort()).thenReturn(port);

        metricsServer.start();

        verify(webappContext).setContextPath("/");
        verify(webappContext).setResourceBase(webappBase);
        verify(webappContext).setDescriptor(descriptorPath);
        verify(webappContext).setAttribute("scoopi.metricsJsonData",
                metricsJsonData);
        verify(localServer).setHandler(webappContext);
        verify(localServer).start();
    }

    @Test
    public void testStartThrowsException() throws Exception {
        int port = 9010;
        URL kiwi = Mockito.mock(URL.class);
        String webappBase = "Bar";
        WebAppContext webappContext = Mockito.mock(WebAppContext.class);
        Server localServer = Mockito.mock(Server.class);

        when(configs.getConfig("scoopi.metrics.server.port"))
                .thenThrow(ConfigNotFoundException.class);
        when(metricsHelper.getURL("/webapp")).thenReturn(kiwi);
        when(kiwi.toString()).thenReturn(webappBase);
        when(factory.createWebAppContext()).thenReturn(webappContext);
        when(factory.createServer(port)).thenReturn(localServer);

        when(localServer.getConnectors()).thenReturn(null); // throws exception

        assertThrows(Exception.class, () -> metricsServer.start());
    }

    @Test
    public void testStop() throws Exception {
        metricsServer.stop();

        verify(server).stop();
    }

    @Test
    public void testStopServerIsNull() throws Exception {
        FieldUtils.writeDeclaredField(metricsServer, "server", null, true);

        metricsServer.stop();

        verify(server, never()).stop();
    }

    @Test
    public void testStopException() throws Exception {
        doThrow(Exception.class).when(server).stop();

        metricsServer.stop(); // logs exception nothing is thrown
    }

    @Test
    public void testSetMetricsJsonData() throws IllegalAccessException {
        Map<String, byte[]> metricsJsonData = new HashMap<>();
        metricsServer.setMetricsJsonData(metricsJsonData);

        Object actual = FieldUtils.readDeclaredField(metricsServer,
                "metricsJsonData", true);

        assertSame(metricsJsonData, actual);
    }
}
