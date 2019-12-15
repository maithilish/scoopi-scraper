package org.codetab.scoopi.metrics;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.FileNotFoundException;
import java.net.URL;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class MetricsServerTest {

    @Mock
    private ServerFactory factory;
    @Mock
    private Configs configs;
    @Mock
    private MetricsHelper metricsHelper;

    @InjectMocks
    private MetricsServer metricsServer;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testStart() throws Exception {
        String port = "10000";
        Server server = Mockito.mock(Server.class);
        WebAppContext webAppContext = Mockito.mock(WebAppContext.class);
        ServerConnector[] connector =
                new ServerConnector[] {Mockito.mock(ServerConnector.class)};

        URL webAppBase = new URL("file://scoopi/web");
        String webAppDescriptor =
                String.join("/", webAppBase.toString(), "WEB-INF/web.xml");

        given(configs.getConfig("scoopi.metrics.server.port")).willReturn(port);
        given(configs.getConfig("scoopi.metrics.server.enable"))
                .willReturn("true");
        given(factory.createServer(Integer.parseInt(port))).willReturn(server);
        given(factory.createWebAppContext()).willReturn(webAppContext);
        given(metricsHelper.getURL("/webapp")).willReturn(webAppBase);
        given(server.getConnectors()).willReturn(connector);
        given(connector[0].getLocalPort()).willReturn(Integer.parseInt(port));

        metricsServer.start();

        verify(webAppContext).setContextPath("/");
        verify(webAppContext).setResourceBase(webAppBase.toString());
        verify(webAppContext).setDescriptor(webAppDescriptor);
        verify(server).setHandler(webAppContext);
        verify(server).start();
    }

    @Test
    public void testStartNoPortConfig() throws Exception {
        String port = "9010";
        Server server = Mockito.mock(Server.class);
        WebAppContext webAppContext = Mockito.mock(WebAppContext.class);
        ServerConnector[] connector =
                new ServerConnector[] {Mockito.mock(ServerConnector.class)};

        URL webAppBase = new URL("file://scoopi/web");
        String webAppDescriptor =
                String.join("/", webAppBase.toString(), "WEB-INF/web.xml");

        given(configs.getConfig("scoopi.metrics.server.port"))
                .willThrow(ConfigNotFoundException.class);
        given(configs.getConfig("scoopi.metrics.server.enable"))
                .willReturn("true");
        given(factory.createServer(Integer.parseInt(port))).willReturn(server);
        given(factory.createWebAppContext()).willReturn(webAppContext);
        given(metricsHelper.getURL("/webapp")).willReturn(webAppBase);
        given(server.getConnectors()).willReturn(connector);
        given(connector[0].getLocalPort()).willReturn(Integer.parseInt(port));

        metricsServer.start();

        verify(webAppContext).setContextPath("/");
        verify(webAppContext).setResourceBase(webAppBase.toString());
        verify(webAppContext).setDescriptor(webAppDescriptor);
        verify(server).setHandler(webAppContext);
        verify(server).start();
    }

    @Test
    public void testStartServerNotEnabled() throws Exception {
        given(configs.getConfig("scoopi.metrics.server.enable"))
                .willReturn("false");

        metricsServer.start();

        verify(configs).getConfig("scoopi.metrics.server.enable");
        verifyNoMoreInteractions(configs);
        verifyNoInteractions(metricsHelper, factory);
    }

    @Test
    public void testStartServerNoEnableConfig() throws Exception {
        given(configs.getConfig("scoopi.metrics.server.enable"))
                .willThrow(ConfigNotFoundException.class);

        metricsServer.start();

        verify(configs).getConfig("scoopi.metrics.server.enable");
        verifyNoMoreInteractions(configs);
        verifyNoInteractions(metricsHelper, factory);
    }

    @Test
    public void testStartShouldThrowException() throws Exception {
        String port = "10000";
        given(configs.getConfig("scoopi.metrics.server.port")).willReturn(port);
        given(configs.getConfig("scoopi.metrics.server.enable"))
                .willReturn("true");
        given(metricsHelper.getURL("/webapp"))
                .willThrow(FileNotFoundException.class);

        testRule.expect(Exception.class);
        metricsServer.start();
    }

    @Test
    public void testStop() throws Exception {
        String port = "10000";
        Server server = Mockito.mock(Server.class);
        WebAppContext webAppContext = Mockito.mock(WebAppContext.class);
        ServerConnector[] connector =
                new ServerConnector[] {Mockito.mock(ServerConnector.class)};

        URL webAppBase = new URL("file://scoopi/web");

        given(configs.getConfig("scoopi.metrics.server.port")).willReturn(port);
        given(configs.getConfig("scoopi.metrics.server.enable"))
                .willReturn("true");
        given(factory.createServer(Integer.parseInt(port))).willReturn(server);
        given(factory.createWebAppContext()).willReturn(webAppContext);
        given(metricsHelper.getURL("/webapp")).willReturn(webAppBase);
        given(server.getConnectors()).willReturn(connector);
        given(connector[0].getLocalPort()).willReturn(Integer.parseInt(port));

        metricsServer.start();

        metricsServer.stop();
        verify(server).stop();

        doThrow(Exception.class).when(server).stop();
        metricsServer.stop();
    }

    @Test
    public void testStopServerNotSet() throws Exception {
        metricsServer.stop();
    }
}
