package org.codetab.scoopi.defs.yml.helper;

import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.commons.lang3.Validate.validState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
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

    public String getPluginField(final Plugin plugin, final String field) {
        notNull(field, "field must not be null");
        validState(plugin.getDef() instanceof JsonNode, "def is not JsonNode");

        JsonNode def = (JsonNode) plugin.getDef();
        String path = String.join("/", "", field);
        JsonNode query = def.at(path);
        if (query.isMissingNode()) {
            throw new NoSuchElementException(
                    String.join(" ", "field", field, "in", plugin.toString()));
        } else {
            return query.asText();
        }
    }

    public Optional<List<Plugin>> getPlugins(
            final Entry<String, JsonNode> steps, final String taskGroup,
            final String taskName, final String stepName)
            throws DefNotFoundException, InvalidDefException {

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
            Plugin plugin = objectFactory.createPlugin(taskGroup, taskName,
                    stepName, defJson, jPlugin);
            pluginList.add(plugin);
        }
        if (pluginList.size() > 0) {
            return Optional.ofNullable(pluginList);
        } else {
            return Optional.empty();
        }
    }
}
