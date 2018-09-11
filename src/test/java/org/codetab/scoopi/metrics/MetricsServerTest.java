package org.codetab.scoopi.metrics;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.FileNotFoundException;
import java.net.URL;

import org.codetab.scoopi.di.BasicFactory;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.helper.IOHelper;
import org.codetab.scoopi.system.ConfigService;
import org.codetab.scoopi.system.ErrorLogger;
import org.eclipse.jetty.server.Server;
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
    private BasicFactory factory;
    @Mock
    private ConfigService configService;
    @Mock
    private IOHelper ioHelper;
    @Mock
    private ErrorLogger errorLogger;

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

        URL webAppBase = new URL("file://scoopi/web");
        String webAppDescriptor = webAppBase.toString() + "WEB-INF/web.xml";

        given(configService.getConfig("scoopi.metrics.server.port"))
                .willReturn(port);
        given(configService.getConfig("scoopi.metrics.server.enable"))
                .willReturn("true");
        given(factory.getServer(Integer.parseInt(port))).willReturn(server);
        given(factory.getWebAppContext()).willReturn(webAppContext);
        given(ioHelper.getURL("/webapp")).willReturn(webAppBase);

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

        URL webAppBase = new URL("file://scoopi/web");
        String webAppDescriptor = webAppBase.toString() + "WEB-INF/web.xml";

        given(configService.getConfig("scoopi.metrics.server.port"))
                .willThrow(ConfigNotFoundException.class);
        given(configService.getConfig("scoopi.metrics.server.enable"))
                .willReturn("true");
        given(factory.getServer(Integer.parseInt(port))).willReturn(server);
        given(factory.getWebAppContext()).willReturn(webAppContext);
        given(ioHelper.getURL("/webapp")).willReturn(webAppBase);

        metricsServer.start();

        verify(webAppContext).setContextPath("/");
        verify(webAppContext).setResourceBase(webAppBase.toString());
        verify(webAppContext).setDescriptor(webAppDescriptor);
        verify(server).setHandler(webAppContext);
        verify(server).start();
    }

    @Test
    public void testStartServerNotEnabled() throws Exception {
        given(configService.getConfig("scoopi.metrics.server.enable"))
                .willReturn("false");

        metricsServer.start();

        verify(configService).getConfig("scoopi.metrics.server.enable");
        verifyNoMoreInteractions(configService);
        verifyZeroInteractions(ioHelper, factory);
    }

    @Test
    public void testStartServerNoEnableConfig() throws Exception {
        given(configService.getConfig("scoopi.metrics.server.enable"))
                .willThrow(ConfigNotFoundException.class);

        metricsServer.start();

        verify(configService).getConfig("scoopi.metrics.server.enable");
        verifyNoMoreInteractions(configService);
        verifyZeroInteractions(ioHelper, factory);
    }

    @Test
    public void testStartShouldThrowException() throws Exception {
        String port = "10000";
        given(configService.getConfig("scoopi.metrics.server.port"))
                .willReturn(port);
        given(configService.getConfig("scoopi.metrics.server.enable"))
                .willReturn("true");
        given(ioHelper.getURL("/webapp"))
                .willThrow(FileNotFoundException.class);

        testRule.expect(Exception.class);
        metricsServer.start();
    }

    @Test
    public void testStop() throws Exception {
        String port = "10000";
        Server server = Mockito.mock(Server.class);
        WebAppContext webAppContext = Mockito.mock(WebAppContext.class);

        URL webAppBase = new URL("file://scoopi/web");

        given(configService.getConfig("scoopi.metrics.server.port"))
                .willReturn(port);
        given(configService.getConfig("scoopi.metrics.server.enable"))
                .willReturn("true");
        given(factory.getServer(Integer.parseInt(port))).willReturn(server);
        given(factory.getWebAppContext()).willReturn(webAppContext);
        given(ioHelper.getURL("/webapp")).willReturn(webAppBase);

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
