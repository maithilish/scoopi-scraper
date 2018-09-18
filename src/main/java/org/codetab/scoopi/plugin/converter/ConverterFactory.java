package org.codetab.scoopi.plugin.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IPluginDefs;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.Plugin;

public class ConverterFactory {

    @Inject
    private IPluginDefs pluginDefs;
    @Inject
    private DInjector di;

    public Map<AxisName, List<IConverter>> getConverters(
            final List<Plugin> plugins) throws ClassNotFoundException {

        Map<AxisName, List<IConverter>> converters = new HashMap<>();
        for (AxisName axisName : AxisName.values()) {
            List<IConverter> list = new ArrayList<>();
            converters.put(axisName, list);
        }

        for (Plugin plugin : plugins) {
            AxisName axis = AxisName
                    .valueOf(pluginDefs.getValue(plugin, "axis").toUpperCase());
            String clzName = pluginDefs.getPluginClass(plugin);

            IConverter converter = createConverter(clzName);
            converter.setPlugin(plugin);

            converters.get(axis).add(converter);
        }
        return converters;
    }

    private IConverter createConverter(final String clzName)
            throws ClassNotFoundException {
        Class<?> clz = Class.forName(clzName);
        Object obj = di.instance(clz);
        if (obj instanceof IConverter) {
            return (IConverter) obj;
        } else {
            throw new ClassCastException(String.join(" ", "plugin class:",
                    clzName, "is not IConverter type"));
        }
    }

}
