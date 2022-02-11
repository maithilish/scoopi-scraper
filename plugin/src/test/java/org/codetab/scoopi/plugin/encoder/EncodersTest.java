package org.codetab.scoopi.plugin.encoder;

import static org.codetab.scoopi.util.Util.dashit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.model.Plugin;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class EncodersTest {
    @InjectMocks
    private Encoders encoders;

    @Mock
    private IPluginDef pluginDef;
    @Mock
    private EncoderFactory encoderFactory;
    @Mock
    private Errors errors;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateEncoders() throws Exception {
        List<Plugin> plugins = new ArrayList<>();
        Plugin plugin = Mockito.mock(Plugin.class);
        plugins.add(plugin);

        String stepsName = "Foo";
        String stepName = "Bar";
        String apple = "Baz";

        String appenderName = dashit(stepName, apple);

        Plugin encoderPlugin = Mockito.mock(Plugin.class);
        List<Plugin> encoderPluginList = new ArrayList<>();
        encoderPluginList.add(encoderPlugin);
        Optional<List<Plugin>> encoderPlugins = Optional.of(encoderPluginList);
        @SuppressWarnings("rawtypes")
        IEncoder encoder = Mockito.mock(IEncoder.class);

        when(plugin.getName()).thenReturn(apple);
        when(pluginDef.getPlugins(plugin)).thenReturn(encoderPlugins);
        when(encoderFactory.createEncoder(encoderPlugin)).thenReturn(encoder);

        encoders.createEncoders(plugins, stepsName, stepName);

        assertEquals(1, encoders.size());
        assertSame(encoder, encoders.get(appenderName).get(0));

        verify(errors, never()).inc();
    }

    @Test
    public void testCreateEncodersEncoderPluginsNotPresent() throws Exception {
        List<Plugin> plugins = new ArrayList<>();
        Plugin plugin = Mockito.mock(Plugin.class);
        plugins.add(plugin);

        String stepsName = "Foo";
        String stepName = "Bar";
        String apple = "Baz";

        Optional<List<Plugin>> encoderPlugins = Optional.empty(); // not present

        when(plugin.getName()).thenReturn(apple);
        when(pluginDef.getPlugins(plugin)).thenReturn(encoderPlugins);

        encoders.createEncoders(plugins, stepsName, stepName);

        assertEquals(0, encoders.size());

        verify(errors, never()).inc();
    }

    @Test
    public void testCreateEncodersEncoderPluginsIsNull() throws Exception {
        List<Plugin> plugins = new ArrayList<>();
        Plugin plugin = Mockito.mock(Plugin.class);
        plugins.add(plugin);

        String stepsName = "Foo";
        String stepName = "Bar";
        String apple = "Baz";

        Optional<List<Plugin>> encoderPlugins = null; // is null

        when(plugin.getName()).thenReturn(apple);
        when(pluginDef.getPlugins(plugin)).thenReturn(encoderPlugins);

        encoders.createEncoders(plugins, stepsName, stepName);

        assertEquals(0, encoders.size());

        verify(errors, never()).inc();
    }

    @Test
    public void testCreateEncodersGetEncoderPluginException() throws Exception {
        List<Plugin> plugins = new ArrayList<>();
        Plugin plugin = Mockito.mock(Plugin.class);
        plugins.add(plugin);

        String stepsName = "Foo";
        String stepName = "Bar";
        String apple = "Baz";

        Plugin encoderPlugin = Mockito.mock(Plugin.class);
        List<Plugin> encoderPluginList = new ArrayList<>();
        encoderPluginList.add(encoderPlugin);

        when(plugin.getName()).thenReturn(apple);
        when(pluginDef.getPlugins(plugin)).thenThrow(StepRunException.class);

        assertThrows(StepRunException.class,
                () -> encoders.createEncoders(plugins, stepsName, stepName));

        assertEquals(0, encoders.size());

        verify(errors, never()).inc();
    }

    @Test
    public void testCreateEncodersCreateException() throws Exception {
        List<Plugin> plugins = new ArrayList<>();
        Plugin plugin = Mockito.mock(Plugin.class);
        plugins.add(plugin);

        String stepsName = "Foo";
        String stepName = "Bar";
        String apple = "Baz";

        String appenderName = dashit(stepName, apple);

        Plugin encoderPlugin = Mockito.mock(Plugin.class);
        List<Plugin> encoderPluginList = new ArrayList<>();
        encoderPluginList.add(encoderPlugin);
        Optional<List<Plugin>> encoderPlugins = Optional.of(encoderPluginList);

        when(plugin.getName()).thenReturn(apple);
        when(pluginDef.getPlugins(plugin)).thenReturn(encoderPlugins);
        when(encoderFactory.createEncoder(encoderPlugin))
                .thenThrow(ClassNotFoundException.class);

        encoders.createEncoders(plugins, stepsName, stepName);

        assertEquals(1, encoders.size());
        assertSame(0, encoders.get(appenderName).size());

        verify(errors).inc();
    }
}
