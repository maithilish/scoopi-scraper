package org.codetab.scoopi.plugin.encoder;

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

public class EncoderFactoryTest {
    @InjectMocks
    private EncoderFactory encoderFactory;

    @Mock
    private DInjector di;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateEncoder() throws Exception {
        Plugin plugin = Mockito.mock(Plugin.class);
        String apple = "Foo";
        IEncoder<?> encoder = Mockito.mock(IEncoder.class);

        when(plugin.getClassName()).thenReturn(apple);
        when(di.instance(apple, IEncoder.class)).thenReturn(encoder);

        IEncoder<?> actual = encoderFactory.createEncoder(plugin);

        assertSame(encoder, actual);
        verify(encoder).setPlugin(plugin);
    }
}
