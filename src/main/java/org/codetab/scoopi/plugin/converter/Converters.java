package org.codetab.scoopi.plugin.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IPluginDefs;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.Plugin;

public class Converters extends HashMap<AxisName, List<IConverter>> {

    private static final long serialVersionUID = 1L;

    @Inject
    private IPluginDefs pluginDefs;
    @Inject
    private ConveterFactory conveterFactory;

    public void createConverters(final List<Plugin> plugins)
            throws ClassNotFoundException, DefNotFoundException {

        for (AxisName axisName : AxisName.values()) {
            List<IConverter> list = new ArrayList<>();
            put(axisName, list);
        }

        for (Plugin plugin : plugins) {
            AxisName axis = AxisName
                    .valueOf(pluginDefs.getValue(plugin, "axis").toUpperCase());
            IConverter converter = conveterFactory.createConverter(plugin);
            get(axis).add(converter);
        }
    }
}
