package org.codetab.scoopi.defs.yml;

import static org.codetab.scoopi.util.Util.dashit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

class TaskDefs {

    @Inject
    private Jacksons jacksons;

    public Map<String, List<String>> getTaskNamesMap(final JsonNode defs) {
        Map<String, List<String>> taskNamesMap = new HashMap<>();
        Iterator<String> taskGroups = defs.fieldNames();
        while (taskGroups.hasNext()) {
            String taskGroup = taskGroups.next();
            JsonNode jTaskGroup = defs.path(taskGroup);
            ArrayList<String> taskNames =
                    Lists.newArrayList(jTaskGroup.fieldNames());
            taskNamesMap.put(taskGroup, taskNames);
        }
        return taskNamesMap;
    }

    public Map<String, JsonNode> getAllTasks(final JsonNode taskGroupsDef) {
        Map<String, JsonNode> map = new HashMap<>();

        Iterator<Entry<String, JsonNode>> taskGroups = taskGroupsDef.fields();
        while (taskGroups.hasNext()) {
            Entry<String, JsonNode> taskGroupEntry = taskGroups.next();
            String taskGroup = taskGroupEntry.getKey();
            JsonNode jTaskGroup = taskGroupEntry.getValue();
            Iterator<Entry<String, JsonNode>> tasks = jTaskGroup.fields();
            while (tasks.hasNext()) {
                Entry<String, JsonNode> taskEntry = tasks.next();
                String taskName = taskEntry.getKey();
                JsonNode task = taskEntry.getValue();
                String key = dashit(taskGroup, taskName);
                map.put(key, task);
            }
        }
        return map;
    }

    public Map<String, JsonNode> getTasks(final JsonNode taskGroupsDef,
            final String group) {
        Map<String, JsonNode> taskMap = new HashMap<>();

        String path = jacksons.path(group);
        JsonNode jTasks = taskGroupsDef.at(path);
        Iterator<Entry<String, JsonNode>> entries = jTasks.fields();
        while (entries.hasNext()) {
            Entry<String, JsonNode> entry = entries.next();
            String taskName = entry.getKey();
            JsonNode jTask = entry.getValue();
            taskMap.put(taskName, jTask);
        }
        return taskMap;
    }

}
