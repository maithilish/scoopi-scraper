package org.codetab.scoopi.plugin.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.Plugin;

public class ConverterMap extends HashMap<String, List<IConverter>> {

    private static final long serialVersionUID = 1L;

    @Inject
    private IPluginDef pluginDef;
    @Inject
    private ConveterFactory conveterFactory;

    public void init(final List<Plugin> plugins)
            throws ClassNotFoundException, DefNotFoundException {
        for (Plugin plugin : plugins) {
            String itemName = pluginDef.getValue(plugin, "item");
            IConverter converter = conveterFactory.createConverter(plugin);
            if (containsKey(itemName)) {
                get(itemName).add(converter);
            } else {
                List<IConverter> list = new ArrayList<>();
                list.add(converter);
                put(itemName, list);
            }
        }
    }
}
