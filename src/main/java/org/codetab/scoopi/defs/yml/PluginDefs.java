package org.codetab.scoopi.defs.yml;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.defs.IPluginDefs;
import org.codetab.scoopi.defs.yml.helper.PluginDefsHelper;
import org.codetab.scoopi.defs.yml.helper.StepDefsHelper;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.InvalidDefException;
import org.codetab.scoopi.model.Plugin;

import com.fasterxml.jackson.databind.JsonNode;

@Singleton
public class PluginDefs implements IPluginDefs {

    @Inject
    private PluginDefsHelper pluginDefsHelper;
    @Inject
    private StepDefsHelper stepDefsHelper;

    private JsonNode defs;

    /**
     * on first invoke sets defs and subsequent invocations do nothing.
     * @param defs
     */
    public void init(final JsonNode taskDefs) {
        if (this.defs == null) {
            this.defs = taskDefs;
        }
    }

    @Override
    public Optional<List<Plugin>> getPlugins(final String taskGroup,
            final String taskName, final String stepName)
            throws DefNotFoundException, InvalidDefException {
        Entry<String, JsonNode> steps =
                stepDefsHelper.getSteps(defs, taskGroup, taskName);
        return pluginDefsHelper.getPlugins(steps, taskGroup, taskName,
                stepName);
    }

    @Override
    public Optional<List<Plugin>> getPlugins(final Plugin plugin)
            throws InvalidDefException {
        return pluginDefsHelper.getPlugins(plugin);
    }

    @Override
    public String getPluginClass(final Plugin plugin)
            throws DefNotFoundException {
        return pluginDefsHelper.getPluginField(plugin, "class");
    }

    @Override
    public String getPluginName(final Plugin plugin)
            throws DefNotFoundException {
        return pluginDefsHelper.getPluginField(plugin, "name");
    }

    @Override
    public String getValue(final Plugin plugin, final String field)
            throws DefNotFoundException {
        return pluginDefsHelper.getPluginField(plugin, field);
    }
}
