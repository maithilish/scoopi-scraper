package org.codetab.scoopi.defs.yml.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.assertj.core.util.Lists;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class NormalizerHelper {

    public JsonNode getTaskGroups(final JsonNode defs) {
        return defs.at("/taskGroups");
    }

    public Map<String, JsonNode> getTasks(final String group,
            final JsonNode taskGroups) {
        Map<String, JsonNode> taskMap = new HashMap<>();
        String path = String.join("/", "", group);
        JsonNode tasks = taskGroups.at(path);
        ArrayList<String> taskNames = Lists.newArrayList(tasks.fieldNames());
        for (String taskName : taskNames) {
            path = String.join("/", "", taskName);
            JsonNode task = tasks.at(path);
            taskMap.put(taskName, task);
        }
        return taskMap;
    }

    public Map<String, JsonNode> getTasks(final JsonNode taskGroups) {
        Map<String, JsonNode> taskMap = new HashMap<>();

        ArrayList<String> groups = Lists.newArrayList(taskGroups.fieldNames());
        for (String group : groups) {
            String path = String.join("/", "", group);
            JsonNode tasks = taskGroups.at(path);
            ArrayList<String> taskNames =
                    Lists.newArrayList(tasks.fieldNames());
            for (String taskName : taskNames) {
                path = String.join("/", "", taskName);
                JsonNode task = tasks.at(path);
                String key = String.join("/", group, taskName);
                taskMap.put(key, task);
            }
        }
        return taskMap;
    }

    public Map<String, JsonNode> getSteps(final JsonNode taskGroups) {
        Map<String, JsonNode> stepsMap = new HashMap<>();

        ArrayList<String> groups = Lists.newArrayList(taskGroups.fieldNames());
        for (String group : groups) {
            String path = String.join("/", "", group);
            JsonNode tasks = taskGroups.at(path);
            ArrayList<String> taskNames =
                    Lists.newArrayList(tasks.fieldNames());
            for (String taskName : taskNames) {
                path = String.join("/", "", taskName);
                JsonNode task = tasks.at(path);
                JsonNode steps = task.at("/steps");
                String key = String.join("/", group, taskName);
                stepsMap.put(key, steps);
            }
        }
        return stepsMap;
    }

    public void setSteps(final JsonNode task, final JsonNode steps) {
        ((ObjectNode) task).set("steps", steps);
    }

    public boolean isStepsDefined(final JsonNode task) {
        return task.get("steps") != null;
    }

    public void setDefaultSteps(final JsonNode task) {
        ((ObjectNode) task).put("steps", "default");
    }

    public String getOverriddenStepsName(final JsonNode steps) {
        ArrayList<Entry<String, JsonNode>> entries =
                Lists.newArrayList(steps.fields());
        if (entries.size() > 0) {
            return entries.get(0).getKey();
        } else {
            return "";
        }
    }

    public Map<String, JsonNode> getOverridenSteps(final String stepsName,
            final JsonNode steps) {
        Map<String, JsonNode> overridenSteps = new HashMap<>();
        String path = String.join("/", "", stepsName);
        ArrayList<Entry<String, JsonNode>> entries =
                Lists.newArrayList(steps.at(path).fields());
        for (Entry<String, JsonNode> entry : entries) {
            overridenSteps.put(entry.getKey(), entry.getValue());
        }
        return overridenSteps;
    }

    /**
     * Get steps defined at top level such as default and user defined steps
     * @param defs
     * @return
     */
    public JsonNode getStepsDef(final JsonNode defs) {
        return defs.at("/steps");
    }

    /**
     * Get map of steps defined at top level such as default and user defined
     * steps
     * @param defs
     * @return
     */
    public Map<String, JsonNode> getStepsMap(final JsonNode steps) {
        Map<String, JsonNode> stepMap = new HashMap<>();

        ArrayList<String> stepsNames = Lists.newArrayList(steps.fieldNames());
        for (String stepsName : stepsNames) {
            String path = String.join("/", "", stepsName);
            stepMap.put(stepsName, steps.at(path));
        }
        return stepMap;
    }
}
