package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codetab.scoopi.util.Util.dashit;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class TaskDefsTest {

    @InjectMocks
    private TaskDefs taskDefs;

    @Mock
    private Jacksons jacksons;

    @Before()
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetTaskNamesMap() {
        String taskGroup = "foo";
        Iterator<String> taskGroups = Lists.newArrayList(taskGroup).iterator();
        JsonNode jTaskGroup = Mockito.mock(JsonNode.class);

        String key = "bar";
        Iterator<Entry<String, JsonNode>> it =
                Collections.singletonMap(key, (JsonNode) new ObjectNode(null))
                        .entrySet().iterator();

        JsonNode defs = Mockito.mock(JsonNode.class);

        when(defs.fieldNames()).thenReturn(taskGroups);
        when(defs.path(taskGroup)).thenReturn(jTaskGroup);
        when(jTaskGroup.fields()).thenReturn(it);

        Map<String, List<String>> actual = taskDefs.getTaskNamesMap(defs);

        assertThat(actual).hasSize(1);
        assertThat(actual.get(taskGroup)).hasSize(1);
        assertThat(actual.get(taskGroup).get(0)).isEqualTo(key);
    }

    @Test
    public void testGetTaskNamesMapNotObjectNode() {
        String taskGroup = "foo";
        Iterator<String> taskGroups = Lists.newArrayList(taskGroup).iterator();
        JsonNode jTaskGroup = Mockito.mock(JsonNode.class);

        String key = "bar";
        Iterator<Entry<String, JsonNode>> it =
                Collections.singletonMap(key, (JsonNode) new TextNode(null))
                        .entrySet().iterator();
        JsonNode defs = Mockito.mock(JsonNode.class);

        when(defs.fieldNames()).thenReturn(taskGroups);
        when(defs.path(taskGroup)).thenReturn(jTaskGroup);
        when(jTaskGroup.fields()).thenReturn(it);

        Map<String, List<String>> actual = taskDefs.getTaskNamesMap(defs);

        assertThat(actual).hasSize(1);
        assertThat(actual.get(taskGroup)).hasSize(0);
    }

    @Test
    public void testGetAllTasks() {
        JsonNode jTaskGroup = Mockito.mock(JsonNode.class);
        String taskGroup = "foo";
        Iterator<Entry<String, JsonNode>> taskGroups = Collections
                .singletonMap(taskGroup, jTaskGroup).entrySet().iterator();

        String taskName = "bar";
        JsonNode task = Mockito.mock(JsonNode.class);
        Iterator<Entry<String, JsonNode>> tasks =
                Collections.singletonMap(taskName, task).entrySet().iterator();

        JsonNode taskGroupsDef = Mockito.mock(JsonNode.class);
        String key = dashit(taskGroup, taskName);

        when(taskGroupsDef.fields()).thenReturn(taskGroups);
        when(jTaskGroup.fields()).thenReturn(tasks);
        when(task.isObject()).thenReturn(true);

        Map<String, JsonNode> actual = taskDefs.getAllTasks(taskGroupsDef);

        assertThat(actual).hasSize(1);
        assertThat(actual.get(key)).isSameAs(task);
    }

    @Test
    public void testGetAllTasksNotObject() {
        JsonNode jTaskGroup = Mockito.mock(JsonNode.class);
        String taskGroup = "foo";
        Iterator<Entry<String, JsonNode>> taskGroups = Collections
                .singletonMap(taskGroup, jTaskGroup).entrySet().iterator();

        String taskName = "bar";
        JsonNode task = Mockito.mock(JsonNode.class);
        Iterator<Entry<String, JsonNode>> tasks =
                Collections.singletonMap(taskName, task).entrySet().iterator();

        JsonNode taskGroupsDef = Mockito.mock(JsonNode.class);

        when(taskGroupsDef.fields()).thenReturn(taskGroups);
        when(jTaskGroup.fields()).thenReturn(tasks);
        when(task.isObject()).thenReturn(false);

        Map<String, JsonNode> actual = taskDefs.getAllTasks(taskGroupsDef);

        assertThat(actual).hasSize(0);
    }

    @Test
    public void testGetTasks() {
        String path = "foo";
        JsonNode jTasks = Mockito.mock(JsonNode.class);

        String taskName = "bar";
        JsonNode jTask = Mockito.mock(JsonNode.class);
        Iterator<Entry<String, JsonNode>> tasks =
                Collections.singletonMap(taskName, jTask).entrySet().iterator();

        JsonNode taskGroupsDef = Mockito.mock(JsonNode.class);
        String group = "bar";
        when(jacksons.path(group)).thenReturn(path);
        when(taskGroupsDef.at(path)).thenReturn(jTasks);
        when(jTasks.fields()).thenReturn(tasks);

        Map<String, JsonNode> actual = taskDefs.getTasks(taskGroupsDef, group);

        assertThat(actual).hasSize(1);
        assertThat(actual.get(taskName)).isSameAs(jTask);
    }
}
