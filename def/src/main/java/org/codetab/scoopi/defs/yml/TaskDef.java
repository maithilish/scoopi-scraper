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
import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.StepInfo;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

@Singleton
public class TaskDef implements ITaskDef {

    @Inject
    private TaskDefs taskDefs;
    @Inject
    private StepDefs stepDefs;
    @Inject
    private Jacksons jacksons;

    private JsonNode defs;
    private Map<String, List<String>> taskNamesMap;
    private Map<String, String> stepsNameMap;
    // taskGroup-taskName, Map of stepName, stepInfo
    private Map<String, Map<String, StepInfo>> stepsMap;
    // taskGroup-taskName, Map of prevStepName, stepInfo
    private Map<String, Map<String, StepInfo>> nextStepsMap;

    @Override
    public void init(final Object taskDefNodes) throws DefNotFoundException {
        Validate.validState(taskDefNodes instanceof JsonNode,
                "taskDefNodes is not JsonNode");

        this.defs = (JsonNode) taskDefNodes;
        taskNamesMap = taskDefs.getTaskNamesMap(defs);
        Map<String, JsonNode> allTasks = taskDefs.getAllTasks(defs);
        stepsNameMap = stepDefs.getTaskStepsNameMap(allTasks);
        stepsMap = stepDefs.getStepsMap(defs, taskNamesMap);
        nextStepsMap = stepDefs.getNextStepsMap(defs, stepsMap);
    }

    @Override
    public List<String> getTaskNames(final String taskGroup) {
        return Collections.unmodifiableList(taskNamesMap.get(taskGroup));
    }

    @Override
    public Optional<String> getFirstTaskName(final String taskGroup) {
        List<String> taskNames = taskNamesMap.get(taskGroup);
        if (isNull(taskNames) || taskNames.size() == 0) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(taskNames.get(0));
        }
    }

    @Override
    public String getFieldValue(final String taskGroup, final String taskName,
            final String... fieldNames) throws DefNotFoundException {
        ArrayList<String> parts = Lists.newArrayList(taskGroup, taskName);
        Collections.addAll(parts, fieldNames);
        String[] pathParts = new String[parts.size()];
        parts.toArray(pathParts);

        return jacksons.getFieldValue(defs, pathParts);
    }

    @Override
    public String getLive(final String taskGroup) throws DefNotFoundException {
        return jacksons.getFieldValue(defs, taskGroup, "live");
    }

    @Override
    public String getStepsName(final String taskGroup, final String taskName)
            throws DefNotFoundException {
        String key = dashit(taskGroup, taskName);
        return stepsNameMap.get(key);
    }

    @Override
    public StepInfo getNextStep(final String taskGroup, final String taskName,
            final String stepName) throws DefNotFoundException {
        String key = dashit(taskGroup, taskName);
        return nextStepsMap.get(key).get(stepName);
    }

}
