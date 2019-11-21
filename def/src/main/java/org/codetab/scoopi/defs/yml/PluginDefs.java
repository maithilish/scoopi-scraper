package org.codetab.scoopi.defs.yml;

import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.commons.lang3.Validate.validState;
import static org.codetab.scoopi.util.Util.spaceit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.InvalidDefException;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

class PluginDefs {

    @Inject
    private Jacksons jacksons;
    @Inject
    private Yamls yamls;
    @Inject
    private ObjectFactory objectFactory;

    public Map<String, Optional<List<Plugin>>> getPluginMap(
            final Map<String, JsonNode> stepsMap) throws InvalidDefException {

        Map<String, Optional<List<Plugin>>> map = new HashMap<>();

        for (String key : stepsMap.keySet()) {
            List<JsonNode> jPlugins = stepsMap.get(key).findValues("plugin");
            List<Plugin> pluginList = new ArrayList<>();
            for (JsonNode jPlugin : jPlugins) {
                String[] parts = key.split("-");
                String taskGroup = parts[0];
                String taskName = parts[1];
                String stepName = parts[2];
                String pluginName = jPlugin.at("/name").asText();
                String className = jPlugin.at("/class").asText();

                String defJson;
                try {
                    defJson = yamls.toJson(jPlugin);
                } catch (JsonProcessingException e) {
                    throw new InvalidDefException(key, e);
                }

                Plugin plugin = objectFactory.createPlugin(pluginName,
                        className, taskGroup, taskName, stepName, defJson,
                        jPlugin);
                pluginList.add(plugin);
            }
            if (pluginList.size() > 0) {
                map.put(key, Optional.ofNullable(pluginList));
            } else {
                map.put(key, Optional.empty());
            }
        }
        return map;
    }

    public Optional<List<Plugin>> getPlugins(final Plugin plugin)
            throws InvalidDefException {
        JsonNode pluginDef = (JsonNode) plugin.getDef();
        List<JsonNode> jPlugins = pluginDef.findValues("plugin");

        Optional<List<Plugin>> plugins = Optional.empty();
        List<Plugin> list = new ArrayList<>();
        for (JsonNode jPlugin : jPlugins) {
            String defJson;
            try {
                defJson = yamls.toJson(jPlugin);
            } catch (JsonProcessingException e) {
                throw new InvalidDefException(
                        spaceit("child plugins of plugin:", plugin.toString()),
                        e);
            }
            String pluginName = jPlugin.at("/name").asText();
            String className = jPlugin.at("/class").asText();
            Plugin childPlugin = objectFactory.createPlugin(pluginName,
                    className, plugin.getTaskGroup(), plugin.getTaskName(),
                    plugin.getStepName(), defJson, jPlugin);
            list.add(childPlugin);
        }
        if (list.size() > 0) {
            plugins = Optional.ofNullable(Collections.unmodifiableList(list));
        }
        return plugins;
    }

    public String getFieldValue(final Plugin plugin, final String field)
            throws DefNotFoundException {
        notNull(field, "field must not be null");
        validState(plugin.getDef() instanceof JsonNode, "def is not JsonNode");

        JsonNode def = (JsonNode) plugin.getDef();
        String path = jacksons.path(field);
        JsonNode jField = def.at(path);
        if (jField.isMissingNode()) {
            throw new DefNotFoundException(
                    spaceit("field:", field, "in", plugin.toString()));
        } else {
            return jField.asText();
        }
    }

    public List<String> getArrayValues(final Plugin plugin,
            final String field) {
        notNull(field, "field must not be null");
        validState(plugin.getDef() instanceof JsonNode, "def is not JsonNode");

        JsonNode def = (JsonNode) plugin.getDef();
        return jacksons.getArrayAsStrings(def, field);
    }

    public Plugin copy(final Plugin plugin) {
        JsonNode jPlugin = (JsonNode) plugin.getDef();
        return objectFactory.createPlugin(plugin.getName(),
                plugin.getClassName(), plugin.getTaskGroup(),
                plugin.getTaskName(), plugin.getStepName(), plugin.getDefJson(),
                jPlugin.deepCopy());
    }

}
