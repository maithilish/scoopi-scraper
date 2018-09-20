package org.codetab.scoopi.defs.yml.helper;

import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.commons.lang3.Validate.validState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import javax.inject.Inject;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.InvalidDefException;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public class PluginDefsHelper {

    @Inject
    private YamlHelper yamlHelper;
    @Inject
    private ObjectFactory objectFactory;

    public String getPluginField(final Plugin plugin, final String field)
            throws DefNotFoundException {
        notNull(field, "field must not be null");
        validState(plugin.getDef() instanceof JsonNode, "def is not JsonNode");

        JsonNode def = (JsonNode) plugin.getDef();
        String path = String.join("/", "", field);
        JsonNode query = def.at(path);
        if (query.isMissingNode()) {
            throw new DefNotFoundException(
                    String.join(" ", "field:", field, "in", plugin.toString()));
        } else {
            return query.asText();
        }
    }

    public Optional<List<Plugin>> getPlugins(
            final Entry<String, JsonNode> steps, final String taskGroup,
            final String taskName, final String stepName)
            throws InvalidDefException {

        String stepsName = steps.getKey();
        JsonNode jSteps = steps.getValue();

        String path = String.join("/", "", stepName, "plugins");
        JsonNode jPlugins = jSteps.at(path);
        List<JsonNode> jPluginList = jPlugins.findValues("plugin");

        List<Plugin> pluginList = new ArrayList<>();
        for (JsonNode jPlugin : jPluginList) {
            String defJson;
            try {
                defJson = yamlHelper.toJson(jPlugin);
            } catch (JsonProcessingException e) {
                throw new InvalidDefException(
                        String.join(" ", "steps:", stepsName), e);
            }
            String pluginName = jPlugin.at("/name").asText();
            String className = jPlugin.at("/class").asText();
            Plugin plugin = objectFactory.createPlugin(pluginName, className,
                    taskGroup, taskName, stepName, defJson, jPlugin);
            pluginList.add(plugin);
        }
        if (pluginList.size() > 0) {
            return Optional.ofNullable(pluginList);
        } else {
            return Optional.empty();
        }
    }

    public Optional<List<Plugin>> getPlugins(final Plugin plugin)
            throws InvalidDefException {
        JsonNode pluginDef = (JsonNode) plugin.getDef();
        String path = String.join("/", "", "plugins");
        JsonNode jPlugins = pluginDef.at(path);
        List<JsonNode> jPluginList = jPlugins.findValues("plugin");

        List<Plugin> pluginList = new ArrayList<>();
        for (JsonNode jPlugin : jPluginList) {
            String defJson;
            try {
                defJson = yamlHelper.toJson(jPlugin);
            } catch (JsonProcessingException e) {
                throw new InvalidDefException(String.join(" ",
                        "child plugins of plugin:", plugin.toString()), e);
            }
            String pluginName = jPlugin.at("/name").asText();
            String className = jPlugin.at("/class").asText();
            Plugin childPlugin = objectFactory.createPlugin(pluginName,
                    className, plugin.getTaskGroup(), plugin.getTaskName(),
                    plugin.getStepName(), defJson, jPlugin);
            pluginList.add(childPlugin);
        }
        if (pluginList.size() > 0) {
            return Optional.ofNullable(pluginList);
        } else {
            return Optional.empty();
        }
    }
}
