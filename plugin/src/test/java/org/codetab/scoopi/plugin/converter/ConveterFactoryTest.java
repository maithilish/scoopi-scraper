package org.codetab.scoopi.plugin.converter;

import static org.junit.Assert.assertSame;
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

public class ConveterFactoryTest {
    @InjectMocks
    private ConveterFactory conveterFactory;

    @Mock
    private DInjector di;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateConverter() throws Exception {
        Plugin plugin = Mockito.mock(Plugin.class);
        String apple = "Foo";
        IConverter converter = Mockito.mock(IConverter.class);

        when(plugin.getClassName()).thenReturn(apple);
        when(di.instance(apple, IConverter.class)).thenReturn(converter);

        IConverter actual = conveterFactory.createConverter(plugin);

        assertSame(converter, actual);
        verify(converter).setPlugin(plugin);
    }
}
