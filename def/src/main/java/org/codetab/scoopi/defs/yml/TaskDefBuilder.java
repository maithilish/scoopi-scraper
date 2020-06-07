package org.codetab.scoopi.defs.yml;

import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.defs.ITaskDefBuilder;
import org.codetab.scoopi.exception.DefNotFoundException;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Build TaskDefData from JsonNode. TaskDefData is serialized and stored in
 * IStore by DefBootstrap.
 * @author m
 *
 */
public class TaskDefBuilder implements ITaskDefBuilder {

    @Inject
    private TaskDefs taskDefs;
    @Inject
    private StepDefs stepDefs;
    @Inject
    private TaskDefData taskDefData;

    @Override
    public byte[] serialize(final TaskDefData data) {
        return SerializationUtils.serialize(data);
    }

    @Override
    public TaskDefData deserialize(final byte[] data) {
        return SerializationUtils.deserialize(data);
    }

    @Override
    public TaskDefData buildData(final Object defs)
            throws DefNotFoundException {
        Validate.validState(defs instanceof JsonNode,
                "taskDefsNode is not JsonNode");
        JsonNode node = (JsonNode) defs;

        taskDefData.setDefsJson(node.toString()); // json of JsonNode
        taskDefData.setTaskNamesMap(taskDefs.getTaskNamesMap(node));
        Map<String, JsonNode> allTasks = taskDefs.getAllTasks(node);
        taskDefData.setStepsNameMap(stepDefs.getTaskStepsNameMap(allTasks));
        taskDefData.setStepsMap(
                stepDefs.getStepsMap(node, taskDefData.getTaskNamesMap()));
        taskDefData.setNextStepsMap(
                stepDefs.getNextStepsMap(node, taskDefData.getStepsMap()));
        return taskDefData;
    }
}
