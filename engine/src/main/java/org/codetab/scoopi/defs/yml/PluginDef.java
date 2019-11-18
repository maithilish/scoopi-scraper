package org.codetab.scoopi.defs.yml;

import static java.util.Objects.isNull;
import static org.codetab.scoopi.util.Util.dashit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.InvalidDefException;
import org.codetab.scoopi.model.Plugin;

import com.fasterxml.jackson.databind.JsonNode;

@Singleton
public class PluginDef implements IPluginDef {

    @Inject
    private PluginDefs pluginDefs;
    @Inject
    private TaskDefs taskDefs;
    @Inject
    private StepDefs stepDefs;

    private JsonNode defs;
    private Map<String, Optional<List<Plugin>>> pluginMap;
    private Map<String, JsonNode> stepsMap;

    @Override
    public void init(final Object taskDefNodes)
            throws DefNotFoundException, InvalidDefException {
        Validate.validState(taskDefNodes instanceof JsonNode,
                "taskDefNodes is not JsonNode");

        this.defs = (JsonNode) taskDefNodes;
        Map<String, JsonNode> allTasks = taskDefs.getAllTasks(defs);
        stepsMap = stepDefs.getStepNodeMap(allTasks);
        pluginMap = pluginDefs.getPluginMap(stepsMap);

    }

    @Override
    public Optional<List<Plugin>> getPlugins(final String taskGroup,
            final String taskName, final String stepName)
            throws DefNotFoundException, InvalidDefException {
        String key = dashit(taskGroup, taskName, stepName);
        Optional<List<Plugin>> plugins = pluginMap.get(key);
        List<Plugin> copy = null;
        if (plugins.isPresent()) {
            List<Plugin> list = new ArrayList<>();
            for (Plugin plugin : plugins.get()) {
                list.add(pluginDefs.copy(plugin));
            }
            copy = Collections.unmodifiableList(list);
        }
        return Optional.ofNullable(copy);
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
