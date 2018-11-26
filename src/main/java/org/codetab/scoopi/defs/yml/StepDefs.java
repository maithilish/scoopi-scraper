package org.codetab.scoopi.defs.yml;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.codetab.scoopi.util.Util.dashit;
import static org.codetab.scoopi.util.Util.spaceit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.StepInfo;

import com.fasterxml.jackson.databind.JsonNode;

class StepDefs {

    @Inject
    private Jacksons jacksons;
    @Inject
    private ObjectFactory objectFactory;

    public String getStepsName(final JsonNode steps) {
        if (steps.isMissingNode()) {
            throw new IllegalStateException("missing steps definition");
        }
        if (steps.isTextual()) {
            return steps.asText();
        } else {
            List<String> names = jacksons.getFieldNames(steps);
            if (names.size() == 1) {
                return names.get(0);
            } else {
                throw new IllegalStateException(
                        spaceit("expected single steps definition, found:",
                                String.valueOf(names.size())));
            }
        }
    }

    public boolean isStepDefined(final JsonNode steps, final String stepName) {
        return nonNull(steps.get(stepName));
    }

    public boolean isStepsDefined(final JsonNode task) {
        return nonNull(task.get("steps"));
    }

    public String getStepsName(final JsonNode defs, final String taskGroup,
            final String taskName) throws DefNotFoundException {
        String path = jacksons.path(taskGroup, taskName, "steps");
        JsonNode jSteps = defs.at(path);
        List<String> stepsNames = jacksons.getFieldNames(jSteps);
        int stepsCount = stepsNames.size();
        switch (stepsCount) {
        case 1:
            String stepsName = stepsNames.get(0);
            return stepsName;
        case 0:
            throw new DefNotFoundException(
                    spaceit("steps, [taskGroups]-[taskName] :", path));
        default:
            throw new IllegalStateException(spaceit(
                    "multiple steps for [taskGroups]-[taskName] :", path));
        }
    }

    public Map<String, String> getTaskStepsNameMap(
            final Map<String, JsonNode> tasksMap) throws DefNotFoundException {
        Map<String, String> map = new HashMap<>();

        for (String key : tasksMap.keySet()) {
            JsonNode jTask = tasksMap.get(key);
            JsonNode jSteps = jTask.path("steps");
            List<String> stepsNames = jacksons.getFieldNames(jSteps);
            int stepsCount = stepsNames.size();
            switch (stepsCount) {
            case 1:
                String stepsName = stepsNames.get(0);
                map.put(key, stepsName);
                break;
            case 0:
                throw new DefNotFoundException(
                        spaceit("steps, [taskGroups]-[taskName] :", key));
            default:
                throw new IllegalStateException(spaceit(
                        "multiple steps for [taskGroups]-[taskName] :", key));
            }
        }
        return map;
    }

    public Map<String, StepInfo> getStepNameStepInfoMap(final JsonNode defs,
            final String taskGroup, final String taskName,
            final String stepsName) throws DefNotFoundException {
        String path = jacksons.path(taskGroup, taskName, "steps", stepsName);
        JsonNode steps = defs.at(path);
        if (steps.isMissingNode()) {
            throw new DefNotFoundException(spaceit("steps at path:", path));
        }

        Map<String, StepInfo> map = new HashMap<>();

        Iterator<Entry<String, JsonNode>> entries = steps.fields();
        while (entries.hasNext()) {
            Entry<String, JsonNode> entry = entries.next();
            JsonNode step = entry.getValue();

            String stepName = entry.getKey();
            String previous = step.findValue("previous").asText();
            String next = step.findValue("next").asText();
            String className = step.findValue("class").asText();
            StepInfo stepInfo = objectFactory.createStepInfo(stepName, previous,
                    next, className);
            map.put(stepName, stepInfo);
        }
        return map;
    }

    public Map<String, Map<String, StepInfo>> getStepsMap(final JsonNode defs,
            final Map<String, List<String>> taskNamesMap)
            throws DefNotFoundException {

        Map<String, Map<String, StepInfo>> map = new HashMap<>();

        for (String taskGroup : taskNamesMap.keySet()) {
            for (String taskName : taskNamesMap.get(taskGroup)) {
                String stepsName = getStepsName(defs, taskGroup, taskName);
                Map<String, StepInfo> stepMap = getStepNameStepInfoMap(defs,
                        taskGroup, taskName, stepsName);
                String key = dashit(taskGroup, taskName);
                map.put(key, stepMap);
            }
        }
        return map;
    }

    public Map<String, JsonNode> getStepsNodeMap(
            final Map<String, JsonNode> tasksMap) throws DefNotFoundException {
        Map<String, JsonNode> map = new HashMap<>();

        for (String key : tasksMap.keySet()) {
            JsonNode jTask = tasksMap.get(key);
            JsonNode jSteps = jTask.path("steps");
            if (jSteps.isMissingNode()) {
                throw new DefNotFoundException(
                        spaceit("steps for taskGroup-task :", key));
            } else {
                map.put(key, jSteps);
            }
        }
        return map;
    }

    public Map<String, JsonNode> getStepNodeMap(
            final Map<String, JsonNode> tasksMap) throws DefNotFoundException {
        Map<String, JsonNode> map = new HashMap<>();

        Map<String, String> stepsNameMap = getTaskStepsNameMap(tasksMap);

        for (String key : tasksMap.keySet()) {
            JsonNode jTask = tasksMap.get(key);
            String stepsName = stepsNameMap.get(key);
            JsonNode jSteps = jTask.at("/steps/" + stepsName);
            if (jSteps.isMissingNode()) {
                throw new DefNotFoundException(
                        spaceit("steps for taskGroup-task :", key));
            } else {
                Iterator<Entry<String, JsonNode>> it = jSteps.fields();
                while (it.hasNext()) {
                    Entry<String, JsonNode> entry = it.next();
                    String stepName = entry.getKey();
                    String extKey = key + "-" + stepName;
                    map.put(extKey, entry.getValue());
                }
            }
        }
        return map;
    }

    public Map<String, JsonNode> getStepNodes(final JsonNode steps,
            final String stepsName) {
        Map<String, JsonNode> map = new HashMap<>();

        String path = jacksons.path(stepsName);
        Iterator<Entry<String, JsonNode>> entries = steps.at(path).fields();
        while (entries.hasNext()) {
            Entry<String, JsonNode> entry = entries.next();
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public Map<String, Map<String, StepInfo>> getNextStepsMap(
            final JsonNode defs,
            final Map<String, Map<String, StepInfo>> stepsMap) {
        Map<String, Map<String, StepInfo>> nextStepsMap = new HashMap<>();
        for (String key : stepsMap.keySet()) {
            Map<String, StepInfo> nextStepsView = new HashMap<>();
            for (StepInfo stepInfo : stepsMap.get(key).values()) {
                nextStepsView.put(stepInfo.getPriviousStepName(), stepInfo);
            }
            nextStepsMap.put(key, nextStepsView);

        }
        return nextStepsMap;
    }

    /**
     * Get map of steps defined at top level such as default and user defined
     * steps
     * @param topStepsdef
     * @return
     */
    public Map<String, JsonNode> getTopStepsMap(final JsonNode topStepsDef) {
        Map<String, JsonNode> map = new HashMap<>();

        Iterator<Entry<String, JsonNode>> entries = topStepsDef.fields();
        while (entries.hasNext()) {
            Entry<String, JsonNode> entry = entries.next();
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public JsonNode getTopSteps(final Map<String, JsonNode> topStepsMap,
            final String stepsName) {

        if (isNull(topStepsMap.get(stepsName))) {
            String message =
                    spaceit("steps not defined for steps name:", stepsName);
            throw new NoSuchElementException(message);
        } else {
            return topStepsMap.get(stepsName);
        }
    }

    public boolean isNestedSteps(final JsonNode steps) {
        boolean nested = true;
        Iterator<Entry<String, JsonNode>> entries = steps.fields();
        while (entries.hasNext()) {
            Entry<String, JsonNode> entry = entries.next();
            JsonNode node = entry.getValue();
            if (!node.path("class").isMissingNode()) {
                nested = false;
            }
        }
        return nested;
    }

}
