package org.codetab.scoopi.defs.yml;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.defs.IPluginDefBuilder;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.InvalidDefException;
import org.codetab.scoopi.model.Plugin;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Build PluginDefData from JsonNode. PluginDefData is serialized and stored in
 * IStore by DefBootstrap.
 * @author m
 *
 */
public class PluginDefBuilder implements IPluginDefBuilder {

    @Inject
    private PluginDefs pluginDefs;
    @Inject
    private TaskDefs taskDefs;
    @Inject
    private StepDefs stepDefs;
    @Inject
    private PluginDefData pluginDefData;

    @Override
    public byte[] serialize(final PluginDefData data) {
        return SerializationUtils.serialize(data);
    }

    @Override
    public PluginDefData deserialize(final byte[] data) {
        return SerializationUtils.deserialize(data);
    }

    @Override
    public PluginDefData buildData(final Object defs)
            throws DefNotFoundException, InvalidDefException {
        Validate.validState(defs instanceof JsonNode,
                "taskDefsNode is not JsonNode");
        JsonNode node = (JsonNode) defs;

        Map<String, JsonNode> allTasks = taskDefs.getAllTasks(node);
        Map<String, JsonNode> stepsMap = stepDefs.getStepNodeMap(allTasks);
        Map<String, List<Plugin>> pluginMap = pluginDefs.getPluginMap(stepsMap);
        pluginDefData.setPluginMap(pluginMap);

        return pluginDefData;
    }
}
