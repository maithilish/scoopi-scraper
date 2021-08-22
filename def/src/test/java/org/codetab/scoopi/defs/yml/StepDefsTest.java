package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.assertj.core.util.Lists;
import org.assertj.core.util.Maps;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.StepInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;

public class StepDefsTest {

    @InjectMocks
    private StepDefs stepDefs;

    @Mock
    private Jacksons jacksons;

    @Mock
    private ObjectFactory objectFactory;

    @Before()
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetStepsName() {
        JsonNode steps = Mockito.mock(JsonNode.class);
        String expected = "foo";

        when(steps.isMissingNode()).thenReturn(false);
        when(steps.isTextual()).thenReturn(true);
        when(steps.asText()).thenReturn(expected);

        String actual = stepDefs.getStepsName(steps);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetStepsNameNotTextual() {
        String name = "foo";
        List<String> names = new ArrayList<>();
        names.add(name);

        JsonNode steps = Mockito.mock(JsonNode.class);

        when(steps.isMissingNode()).thenReturn(false);
        when(steps.isTextual()).thenReturn(false);
        when(jacksons.getFieldNames(steps)).thenReturn(names);

        String actual = stepDefs.getStepsName(steps);

        assertThat(actual).isEqualTo(name);
    }

    @Test
    public void testGetStepsNameMultiStepsName() {
        List<String> names = new ArrayList<>();
        names.add("foo");
        names.add("bar");

        JsonNode steps = Mockito.mock(JsonNode.class);

        when(steps.isMissingNode()).thenReturn(false);
        when(steps.isTextual()).thenReturn(false);
        when(jacksons.getFieldNames(steps)).thenReturn(names);

        assertThrows(IllegalStateException.class,
                () -> stepDefs.getStepsName(steps));
    }

    @Test
    public void testGetStepsNameMissingNode() {
        JsonNode steps = Mockito.mock(JsonNode.class);

        when(steps.isMissingNode()).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> stepDefs.getStepsName(steps));
    }

    @Test
    public void testIsStepDefined() {
        JsonNode steps = Mockito.mock(JsonNode.class);
        String stepName = "foo";

        when(steps.get(stepName)).thenReturn(steps).thenReturn(null);

        assertThat(stepDefs.isStepDefined(steps, stepName)).isTrue();
        assertThat(stepDefs.isStepDefined(steps, stepName)).isFalse();
    }

    @Test
    public void testIsStepsDefined() {
        JsonNode task = Mockito.mock(JsonNode.class);

        when(task.get("steps")).thenReturn(task).thenReturn(null);

        assertThat(stepDefs.isStepsDefined(task)).isTrue();
        assertThat(stepDefs.isStepsDefined(task)).isFalse();
    }

    @Test
    public void testGetStepsNameJsonNodeStringString() throws Exception {
        String path = "foo";
        JsonNode jSteps = Mockito.mock(JsonNode.class);
        JsonNode defs = Mockito.mock(JsonNode.class);
        String taskGroup = "bar";
        String taskName = "baz";

        String stepsName = "foo";
        List<String> stepsNames = new ArrayList<>();
        stepsNames.add(stepsName);

        when(jacksons.path(taskGroup, taskName, "steps")).thenReturn(path);
        when(defs.at(path)).thenReturn(jSteps);
        when(jacksons.getFieldNames(jSteps)).thenReturn(stepsNames);

        String actual = stepDefs.getStepsName(defs, taskGroup, taskName);

        assertThat(actual).isEqualTo(stepsName);
    }

    @Test
    public void testGetStepsNameJsonNodeStringStringMultiSteps()
            throws Exception {
        String path = "foo";
        JsonNode jSteps = Mockito.mock(JsonNode.class);
        JsonNode defs = Mockito.mock(JsonNode.class);
        String taskGroup = "bar";
        String taskName = "baz";

        List<String> stepsNames = new ArrayList<>(); // multi steps
        stepsNames.add("foo");
        stepsNames.add("bar");

        when(jacksons.path(taskGroup, taskName, "steps")).thenReturn(path);
        when(defs.at(path)).thenReturn(jSteps);
        when(jacksons.getFieldNames(jSteps)).thenReturn(stepsNames);

        assertThrows(IllegalStateException.class,
                () -> stepDefs.getStepsName(defs, taskGroup, taskName));
    }

    @Test
    public void testGetStepsNameJsonNodeStringStringNoSteps() throws Exception {
        String path = "foo";
        JsonNode jSteps = Mockito.mock(JsonNode.class);
        JsonNode defs = Mockito.mock(JsonNode.class);
        String taskGroup = "bar";
        String taskName = "baz";

        List<String> stepsNames = new ArrayList<>(); // empty - no steps

        when(jacksons.path(taskGroup, taskName, "steps")).thenReturn(path);
        when(defs.at(path)).thenReturn(jSteps);
        when(jacksons.getFieldNames(jSteps)).thenReturn(stepsNames);

        assertThrows(DefNotFoundException.class,
                () -> stepDefs.getStepsName(defs, taskGroup, taskName));
    }

    @Test
    public void testGetTaskStepsNameMap() throws Exception {

        JsonNode jSteps = Mockito.mock(JsonNode.class);
        JsonNode jTask = Mockito.mock(JsonNode.class);

        String key = "foo";
        Map<String, JsonNode> tasksMap = new HashMap<>();
        tasksMap.put(key, jTask);

        String stepsName = "bar";
        List<String> stepsNames = new ArrayList<>();
        stepsNames.add(stepsName);

        when(jTask.path("steps")).thenReturn(jSteps);
        when(jacksons.getFieldNames(jSteps)).thenReturn(stepsNames);

        Map<String, String> actual = stepDefs.getTaskStepsNameMap(tasksMap);

        assertThat(actual).hasSize(1);
        assertThat(actual.get(key)).isEqualTo(stepsName);
    }

    @Test
    public void testGetTaskStepsNameMapNoStepsDefined() throws Exception {

        JsonNode jSteps = Mockito.mock(JsonNode.class);
        JsonNode jTask = Mockito.mock(JsonNode.class);

        String key = "foo";
        Map<String, JsonNode> tasksMap = new HashMap<>();
        tasksMap.put(key, jTask);

        List<String> stepsNames = new ArrayList<>(); // empty, no steps

        when(jTask.path("steps")).thenReturn(jSteps);
        when(jacksons.getFieldNames(jSteps)).thenReturn(stepsNames);

        assertThrows(DefNotFoundException.class,
                () -> stepDefs.getTaskStepsNameMap(tasksMap));
    }

    @Test
    public void testGetTaskStepsNameMapMultiStepsDefined() throws Exception {

        JsonNode jSteps = Mockito.mock(JsonNode.class);
        JsonNode jTask = Mockito.mock(JsonNode.class);

        String key = "foo";
        Map<String, JsonNode> tasksMap = new HashMap<>();
        tasksMap.put(key, jTask);

        List<String> stepsNames = new ArrayList<>(); // multi steps
        stepsNames.add("foo");
        stepsNames.add("bar");

        when(jTask.path("steps")).thenReturn(jSteps);
        when(jacksons.getFieldNames(jSteps)).thenReturn(stepsNames);

        assertThrows(IllegalStateException.class,
                () -> stepDefs.getTaskStepsNameMap(tasksMap));
    }

    @Test
    public void testGetStepNameStepInfoMap() throws Exception {
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String previous = "foo";
        String className = "baz";
        JsonNode steps = Mockito.mock(JsonNode.class);
        String next = "qux";
        String path = "quux";
        JsonNode defs = Mockito.mock(JsonNode.class);
        String taskGroup = "corge";
        String taskName = "grault";
        String stepsName = "garply";

        String stepName = "bar";
        JsonNode step = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);

        Iterator<Entry<String, JsonNode>> it =
                Maps.newHashMap(stepName, step).entrySet().iterator();

        when(jacksons.path(taskGroup, taskName, "steps", stepsName))
                .thenReturn(path);
        when(defs.at(path)).thenReturn(steps);
        when(steps.isMissingNode()).thenReturn(false);

        when(steps.fields()).thenReturn(it);

        when(step.findValue("previous").asText()).thenReturn(previous);
        when(step.findValue("next").asText()).thenReturn(next);
        when(step.findValue("class").asText()).thenReturn(className);
        when(objectFactory.createStepInfo(stepName, previous, next, className))
                .thenReturn(stepInfo);

        Map<String, StepInfo> actual = stepDefs.getStepNameStepInfoMap(defs,
                taskGroup, taskName, stepsName);

        assertThat(actual).hasSize(1);
        assertThat(actual.get(stepName)).isSameAs(stepInfo);
    }

    @Test
    public void testGetStepNameStepInfoMapIsMissingNode() throws Exception {
        String path = "quux";
        JsonNode defs = Mockito.mock(JsonNode.class);
        String taskGroup = "corge";
        String taskName = "grault";
        String stepsName = "garply";

        when(jacksons.path(taskGroup, taskName, "steps", stepsName))
                .thenReturn(path);
        when(defs.at(path)).thenReturn(MissingNode.getInstance());

        assertThrows(DefNotFoundException.class, () -> stepDefs
                .getStepNameStepInfoMap(defs, taskGroup, taskName, stepsName));
    }

    @Test
    public void testGetStepsMap() throws Exception {

        // for in class getStepName() call
        String taskName = "bar";
        String taskGroup = "foo";
        String path = "baz";
        JsonNode jSteps = Mockito.mock(JsonNode.class);
        JsonNode defs = Mockito.mock(JsonNode.class);

        String stepsName = "foo";
        List<String> stepsNames = new ArrayList<>();
        stepsNames.add(stepsName);

        when(jacksons.path(taskGroup, taskName, "steps")).thenReturn(path);
        when(jacksons.getFieldNames(jSteps)).thenReturn(stepsNames);

        // for in class getStepNameStepInfoMap() call
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String previous = "foo";
        String className = "baz";
        JsonNode steps = Mockito.mock(JsonNode.class);
        String next = "qux";

        String stepName = "bar";
        JsonNode step = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        Iterator<Entry<String, JsonNode>> it =
                Maps.newHashMap(stepName, step).entrySet().iterator();

        String key = String.join("-", taskGroup, taskName);

        when(defs.at(path)).thenReturn(jSteps).thenReturn(steps);
        when(jacksons.path(taskGroup, taskName, "steps", stepsName))
                .thenReturn(path);
        when(steps.isMissingNode()).thenReturn(false);

        when(steps.fields()).thenReturn(it);

        when(step.findValue("previous").asText()).thenReturn(previous);
        when(step.findValue("next").asText()).thenReturn(next);
        when(step.findValue("class").asText()).thenReturn(className);
        when(objectFactory.createStepInfo(stepName, previous, next, className))
                .thenReturn(stepInfo);

        // for test getStepsMap() call
        List<String> taskNames = Lists.newArrayList(taskName);
        Map<String, List<String>> taskNamesMap = new HashMap<>();
        taskNamesMap.put(taskGroup, taskNames);

        Map<String, Map<String, StepInfo>> actual =
                stepDefs.getStepsMap(defs, taskNamesMap);

        assertThat(actual).hasSize(1);
        assertThat(actual.get(key)).hasSize(1);
        assertThat(actual.get(key).get(stepName)).isSameAs(stepInfo);
    }

    @Test
    public void testGetStepsNodeMap() throws Exception {
        JsonNode jSteps = Mockito.mock(JsonNode.class);
        JsonNode jTask = Mockito.mock(JsonNode.class);

        String key = "foo";
        Map<String, JsonNode> tasksMap = new HashMap<>();
        tasksMap.put(key, jTask);

        when(jTask.path("steps")).thenReturn(jSteps);
        when(jSteps.isMissingNode()).thenReturn(false);

        Map<String, JsonNode> actual = stepDefs.getStepsNodeMap(tasksMap);

        assertThat(actual).hasSize(1);
        assertThat(actual.get(key)).isSameAs(jSteps);
    }

    @Test
    public void testGetStepsNodeMapIsMissing() throws Exception {
        JsonNode jSteps = Mockito.mock(JsonNode.class);
        JsonNode jTask = Mockito.mock(JsonNode.class);

        String key = "foo";
        Map<String, JsonNode> tasksMap = new HashMap<>();
        tasksMap.put(key, jTask);

        when(jTask.path("steps")).thenReturn(jSteps);
        when(jSteps.isMissingNode()).thenReturn(true);

        assertThrows(DefNotFoundException.class,
                () -> stepDefs.getStepsNodeMap(tasksMap));
    }

    @Test
    public void testGetStepNodeMap() throws Exception {

        JsonNode jStep = Mockito.mock(JsonNode.class);

        String stepsName = "foo";
        String stepName = "bar";
        Iterator<Entry<String, JsonNode>> it =
                Maps.newHashMap(stepName, jStep).entrySet().iterator();

        JsonNode jSteps = Mockito.mock(JsonNode.class);

        String key = "baz";
        JsonNode jTask = Mockito.mock(JsonNode.class);
        Map<String, JsonNode> tasksMap = new HashMap<>();
        tasksMap.put(key, jTask);

        Map<String, String> stepsNameMap = new HashMap<>();
        stepsNameMap.put(key, stepsName);

        String extKey = key + "-" + stepName;

        when(jTask.at("/steps/" + stepsName)).thenReturn(jSteps);
        when(jSteps.isMissingNode()).thenReturn(false);
        when(jSteps.fields()).thenReturn(it);

        Map<String, JsonNode> actual =
                stepDefs.getStepNodeMap(tasksMap, stepsNameMap);

        assertThat(actual).hasSize(1);
        assertThat(actual.get(extKey)).isSameAs(jStep);
    }

    @Test
    public void testGetStepNodeMapIsMissing() throws Exception {
        String stepsName = "foo";
        JsonNode jSteps = Mockito.mock(JsonNode.class);

        String key = "baz";
        JsonNode jTask = Mockito.mock(JsonNode.class);
        Map<String, JsonNode> tasksMap = new HashMap<>();
        tasksMap.put(key, jTask);

        Map<String, String> stepsNameMap = new HashMap<>();
        stepsNameMap.put(key, stepsName);

        when(jTask.at("/steps/" + stepsName)).thenReturn(jSteps);
        when(jSteps.isMissingNode()).thenReturn(true);

        assertThrows(DefNotFoundException.class,
                () -> stepDefs.getStepNodeMap(tasksMap, stepsNameMap));
    }

    @Test
    public void testGetStepNodes() {
        String path = "foo";
        JsonNode steps = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        String stepsName = "bar";

        String stepName = "baz";
        JsonNode stepNode = Mockito.mock(JsonNode.class);
        Iterator<Entry<String, JsonNode>> entries =
                Maps.newHashMap(stepName, stepNode).entrySet().iterator();

        when(jacksons.path(stepsName)).thenReturn(path);
        when(steps.at(path).fields()).thenReturn(entries);

        Map<String, JsonNode> actual = stepDefs.getStepNodes(steps, stepsName);

        assertThat(actual).hasSize(1);

        assertThat(actual.get(stepName)).isSameAs(stepNode);
    }

    @Test
    public void testGetNextStepsMap() {
        String key = "foo";
        String stepName = "bar";
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        Map<String, StepInfo> stepInfoMap = new HashMap<>();
        stepInfoMap.put(stepName, stepInfo);

        Map<String, Map<String, StepInfo>> stepsMap = new HashMap<>();
        stepsMap.put(key, stepInfoMap);
        String priviousStepName = "baz";

        when(stepInfo.getPriviousStepName()).thenReturn(priviousStepName);

        Map<String, Map<String, StepInfo>> actual =
                stepDefs.getNextStepsMap(stepsMap);

        assertThat(actual).hasSize(1);
        assertThat(actual.get(key)).hasSize(1);
        assertThat(actual.get(key).get(priviousStepName)).isSameAs(stepInfo);
    }

    @Test
    public void testGetTopStepsMap() {
        String key = "baz";
        JsonNode stepNode = Mockito.mock(JsonNode.class);
        Iterator<Entry<String, JsonNode>> entries =
                Maps.newHashMap(key, stepNode).entrySet().iterator();

        JsonNode topStepsDef = Mockito.mock(JsonNode.class);

        when(topStepsDef.fields()).thenReturn(entries);

        Map<String, JsonNode> actual = stepDefs.getTopStepsMap(topStepsDef);

        assertThat(actual).hasSize(1);
        assertThat(actual.get(key)).isSameAs(stepNode);
    }

    @Test
    public void testGetTopSteps() {
        String stepsName = "foo";
        JsonNode stepsNode = Mockito.mock(JsonNode.class);
        Map<String, JsonNode> topStepsMap = new HashMap<>();
        topStepsMap.put(stepsName, stepsNode);

        JsonNode actual = stepDefs.getTopSteps(topStepsMap, stepsName);
        assertThat(actual).isSameAs(stepsNode);
    }

    @Test
    public void testGetTopStepsIsNull() {
        String stepsName = "foo";
        Map<String, JsonNode> topStepsMap = new HashMap<>();
        topStepsMap.put(stepsName, null);

        assertThrows(NoSuchElementException.class,
                () -> stepDefs.getTopSteps(topStepsMap, stepsName));
    }

    @Test
    public void testIsNestedSteps() {
        String key = "baz";
        JsonNode node = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        Iterator<Entry<String, JsonNode>> entries =
                Maps.newHashMap(key, node).entrySet().iterator();

        JsonNode steps = Mockito.mock(JsonNode.class);

        when(steps.fields()).thenReturn(entries);
        when(node.path("class").isMissingNode()).thenReturn(true);

        assertThat(stepDefs.isNestedSteps(steps)).isTrue();
    }

    @Test
    public void testIsNestedStepsIsMissingNode() {
        String key = "baz";
        JsonNode node = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        Iterator<Entry<String, JsonNode>> entries =
                Maps.newHashMap(key, node).entrySet().iterator();

        JsonNode steps = Mockito.mock(JsonNode.class);

        when(steps.fields()).thenReturn(entries);
        when(node.path("class").isMissingNode()).thenReturn(false);

        assertThat(stepDefs.isNestedSteps(steps)).isFalse();
    }
}
