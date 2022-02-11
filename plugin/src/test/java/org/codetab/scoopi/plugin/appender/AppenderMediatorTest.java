package org.codetab.scoopi.plugin.appender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Plugin;
import org.codetab.scoopi.model.PrintPayload;
import org.codetab.scoopi.plugin.appender.Appender.Marker;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class AppenderMediatorTest {
    @InjectMocks
    private AppenderMediator appenderMediator;

    @Mock
    private AppenderFactory appenderFactory;
    @Mock
    private AppenderPoolService appenderPoolService;
    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private Errors errors;

    private Map<String, Appender> appenders;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        appenders = (Map<String, Appender>) FieldUtils
                .readDeclaredField(appenderMediator, "appenders", true);
    }

    @Test
    public void testGetAppender() {
        String appenderName = "Foo";

        Appender appender = Mockito.mock(Appender.class);
        appenders.put(appenderName, appender);

        Appender actual = appenderMediator.getAppender(appenderName);

        assertThat(actual).isSameAs(appender);
    }

    @Test
    public void testCreateAppender() throws Exception {
        String appenderName = "Foo";
        Plugin plugin = Mockito.mock(Plugin.class);
        Appender appender = Mockito.mock(Appender.class);

        when(appenderFactory.createAppender(appenderName, plugin))
                .thenReturn(appender);

        Appender actual = appenderMediator.createAppender(appenderName, plugin);

        assertSame(appender, actual);
        verify(appenderPoolService).submit("appender", appender);
    }

    @Test
    public void testCreateAppenderNotNull() throws Exception {
        String appenderName = "Foo";
        Plugin plugin = Mockito.mock(Plugin.class);
        Appender appender = Mockito.mock(Appender.class);
        appenders.put(appenderName, appender); // appender exists

        when(appenderFactory.createAppender(appenderName, plugin))
                .thenReturn(appender);

        Appender actual = appenderMediator.createAppender(appenderName, plugin);

        assertSame(appender, actual);
        verify(appenderFactory, never()).createAppender(appenderName, plugin);
        verify(appenderPoolService, never()).submit("appender", appender);

        assertEquals(1, appenders.size());
        assertSame(appender, appenders.get(appenderName));
    }

    @Test
    public void testCloseAll() throws InterruptedException {
        String appenderName = "Foo";
        Appender appender = Mockito.mock(Appender.class);
        PrintPayload eosPayload = Mockito.mock(PrintPayload.class);
        appenders.put(appenderName, appender);

        when(objectFactory.createPrintPayload(null, Marker.END_OF_STREAM))
                .thenReturn(eosPayload);
        appenderMediator.closeAll();

        verify(appender).append(eosPayload);
        verify(errors, never()).inc();
    }

    @Test
    public void testCloseAllInterrupted() throws InterruptedException {
        String appenderName = "Foo";
        Appender appender = Mockito.mock(Appender.class);
        PrintPayload eosPayload = Mockito.mock(PrintPayload.class);
        appenders.put(appenderName, appender);

        when(objectFactory.createPrintPayload(null, Marker.END_OF_STREAM))
                .thenReturn(eosPayload);
        doThrow(InterruptedException.class).when(appender).append(eosPayload);

        appenderMediator.closeAll();

        verify(errors).inc();
    }

    @Test
    public void testWaitForFinish() {
        appenderMediator.waitForFinish();

        verify(appenderPoolService).waitForFinish();
    }
}
