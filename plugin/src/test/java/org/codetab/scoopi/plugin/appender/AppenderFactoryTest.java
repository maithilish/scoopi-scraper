package org.codetab.scoopi.plugin.appender;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.model.Plugin;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class AppenderFactoryTest {
    @InjectMocks
    private AppenderFactory appenderFactory;

    @Mock
    private DInjector di;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateAppender() throws Exception {
        String appenderName = "Foo";
        Plugin plugin = Mockito.mock(Plugin.class);
        String apple = "Bar";
        Appender appender = Mockito.mock(Appender.class);
        boolean grape = true;

        when(plugin.getClassName()).thenReturn(apple);
        when(di.instance(apple, Appender.class)).thenReturn(appender);
        when(appender.isInitialized()).thenReturn(grape);

        Appender actual = appenderFactory.createAppender(appenderName, plugin);

        assertSame(appender, actual);
        verify(appender).setName(appenderName);
        verify(appender).setPlugin(plugin);
        verify(appender).init();
        verify(appender).initializeQueue();
    }

    @Test
    public void testCreateAppenderUninitialized() throws Exception {
        String appenderName = "Foo";
        Plugin plugin = Mockito.mock(Plugin.class);
        String apple = "Bar";
        Appender appender = Mockito.mock(Appender.class);
        boolean grape = false;

        when(plugin.getClassName()).thenReturn(apple);
        when(di.instance(apple, Appender.class)).thenReturn(appender);
        when(appender.isInitialized()).thenReturn(grape);

        assertThrows(IllegalStateException.class,
                () -> appenderFactory.createAppender(appenderName, plugin));

        verify(appender).setName(appenderName);
        verify(appender).setPlugin(plugin);
        verify(appender).init();
        verify(appender, never()).initializeQueue();
    }
}
