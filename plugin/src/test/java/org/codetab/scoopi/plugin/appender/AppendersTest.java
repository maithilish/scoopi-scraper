package org.codetab.scoopi.plugin.appender;

import static org.codetab.scoopi.util.Util.dashit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.model.Plugin;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class AppendersTest {
    @InjectMocks
    private Appenders appenders;

    @Mock
    private AppenderMediator appenderMediator;
    @Mock
    private Errors errors;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateAppendersAppenderExists() throws ClassCastException,
            ClassNotFoundException, DefNotFoundException {
        List<Plugin> plugins = new ArrayList<>();
        Plugin plugin = Mockito.mock(Plugin.class);
        plugins.add(plugin);

        String stepsName = "Foo";
        String stepName = "Bar";
        String apple = "Baz";
        String appenderName = dashit(stepName, apple);
        Appender appender = Mockito.mock(Appender.class);

        when(plugin.getName()).thenReturn(apple);
        when(appenderMediator.getAppender(appenderName)).thenReturn(appender);
        // when(appenderMediator.createAppender(appenderName,
        // plugin)).thenReturn(appender);
        appenders.createAppenders(plugins, stepsName, stepName);

        verify(errors, never()).inc();

        assertSame(appender, appenders.get(appenderName));
    }

    @Test
    public void testCreateAppendersNewAppender() throws ClassCastException,
            ClassNotFoundException, DefNotFoundException {
        List<Plugin> plugins = new ArrayList<>();
        Plugin plugin = Mockito.mock(Plugin.class);
        plugins.add(plugin);

        String stepsName = "Foo";
        String stepName = "Bar";
        String apple = "Baz";
        String appenderName = dashit(stepName, apple);
        Appender appender = Mockito.mock(Appender.class);

        when(plugin.getName()).thenReturn(apple);
        when(appenderMediator.getAppender(appenderName)).thenReturn(null);
        when(appenderMediator.createAppender(appenderName, plugin))
                .thenReturn(appender);
        appenders.createAppenders(plugins, stepsName, stepName);

        verify(errors, never()).inc();

        assertSame(appender, appenders.get(appenderName));
    }

    @Test
    public void testCreateAppendersException() throws ClassCastException,
            ClassNotFoundException, DefNotFoundException {
        List<Plugin> plugins = new ArrayList<>();
        Plugin plugin = Mockito.mock(Plugin.class);
        plugins.add(plugin);

        String stepsName = "Foo";
        String stepName = "Bar";
        String apple = "Baz";
        String appenderName = dashit(stepName, apple);

        when(plugin.getName()).thenReturn(apple);
        when(appenderMediator.getAppender(appenderName)).thenReturn(null);
        when(appenderMediator.createAppender(appenderName, plugin))
                .thenThrow(ClassCastException.class);
        appenders.createAppenders(plugins, stepsName, stepName);

        verify(errors).inc();

        assertEquals(0, appenders.size());
    }
}
