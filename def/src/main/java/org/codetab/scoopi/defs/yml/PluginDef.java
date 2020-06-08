package org.codetab.scoopi.defs.yml;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.codetab.scoopi.util.Util.dashit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.InvalidDefException;
import org.codetab.scoopi.model.Plugin;

@Singleton
public class PluginDef implements IPluginDef {

    @Inject
    private PluginDefs pluginDefs;
    @Inject
    private PluginDefData data;

    @Override
    public Optional<List<Plugin>> getPlugins(final String taskGroup,
            final String taskName, final String stepName)
            throws DefNotFoundException, InvalidDefException {
        String key = dashit(taskGroup, taskName, stepName);
        List<Plugin> plugins = data.getPluginMap().get(key);
        if (nonNull(plugins)) {
            List<Plugin> list = new ArrayList<>();
            for (Plugin plugin : plugins) {
                list.add(pluginDefs.copy(plugin));
            }
            return Optional.ofNullable(Collections.unmodifiableList(list));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<List<Plugin>> getPlugins(final Plugin plugin)
            throws InvalidDefException {
        // not cached, helper returns new unmodifiable list
        return pluginDefs.getPlugins(plugin);
    }

    @Override
    public String getValue(final Plugin plugin, final String field)
            throws DefNotFoundException {
        return pluginDefs.getFieldValue(plugin, field);
    }

    @Override
    public String getValue(final Plugin plugin, final String field,
            final String defaultValue) {
        try {
            return pluginDefs.getFieldValue(plugin, field);
        } catch (DefNotFoundException e) {
            return defaultValue;
        }
    }

    @Override
    public Optional<List<String>> getArrayValues(final Plugin plugin,
            final String field) {
        List<String> list = pluginDefs.getArrayValues(plugin, field);
        if (isNull(list)) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(list);
        }
    }
}
