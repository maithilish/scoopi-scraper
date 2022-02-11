package org.codetab.scoopi.plugin.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.model.Plugin;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ConverterMapTest {
    @InjectMocks
    private ConverterMap converterMap;

    @Mock
    private IPluginDef pluginDef;
    @Mock
    private ConveterFactory conveterFactory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInitConverterKeyExists() throws Exception {
        List<Plugin> plugins = new ArrayList<>();
        Plugin plugin = Mockito.mock(Plugin.class);
        plugins.add(plugin);

        String itemName = "Foo";
        IConverter converter = Mockito.mock(IConverter.class);

        converterMap.put(itemName, new ArrayList<>()); // key exists

        when(pluginDef.getValue(plugin, "item")).thenReturn(itemName);
        when(conveterFactory.createConverter(plugin)).thenReturn(converter);
        converterMap.init(plugins);

        assertEquals(1, converterMap.size());
        assertEquals(1, converterMap.get(itemName).size());
        assertSame(converter, converterMap.get(itemName).get(0));
    }

    @Test
    public void testInitNoKey() throws Exception {
        List<Plugin> plugins = new ArrayList<>();
        Plugin plugin = Mockito.mock(Plugin.class);
        plugins.add(plugin);

        String itemName = "Foo";
        IConverter converter = Mockito.mock(IConverter.class);

        when(pluginDef.getValue(plugin, "item")).thenReturn(itemName);
        when(conveterFactory.createConverter(plugin)).thenReturn(converter);
        converterMap.init(plugins);

        assertEquals(1, converterMap.size());
        assertEquals(1, converterMap.get(itemName).size());
        assertSame(converter, converterMap.get(itemName).get(0));
    }
}
