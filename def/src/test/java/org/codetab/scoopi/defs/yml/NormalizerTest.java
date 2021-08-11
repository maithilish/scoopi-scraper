package org.codetab.scoopi.defs.yml;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class NormalizerTest {

    @InjectMocks
    private Normalizer normalizer;

    @Mock
    private Normalizers normalizers;
    @Mock
    private TaskDefs taskDefs;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private StepDefs stepDefs;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAddItemIndex() throws IOException {
        JsonNode defs = Mockito.mock(JsonNode.class);

        JsonNode dataDefs = Mockito.mock(JsonNode.class);
        List<JsonNode> items = new ArrayList<>();
        items.add(new TextNode("items"));
        List<JsonNode> dims = new ArrayList<>();
        dims.add(new TextNode("dims"));
        List<JsonNode> fact = new ArrayList<>();
        fact.add(new TextNode("fact"));

        when(defs.at("/dataDefs")).thenReturn(dataDefs);
        when(dataDefs.findValues("items")).thenReturn(items);
        when(dataDefs.findValues("dims")).thenReturn(dims);
        when(dataDefs.findValues("fact")).thenReturn(fact);

        normalizer.addItemIndex(defs);

        verify(normalizers).addIndex(items);
        verify(normalizers).addIndex(dims);
        verify(normalizers).addIndex(fact);
    }

    @Test
    public void testAddItemOrder() throws IOException {
        JsonNode defs = Mockito.mock(JsonNode.class);

        JsonNode dataDefs = Mockito.mock(JsonNode.class);
        List<JsonNode> items = new ArrayList<>();
        items.add(new TextNode("items"));
        List<JsonNode> dims = new ArrayList<>();
        dims.add(new TextNode("dims"));
        List<JsonNode> fact = new ArrayList<>();
        fact.add(new TextNode("fact"));

        when(defs.at("/dataDefs")).thenReturn(dataDefs);
        when(dataDefs.findValues("items")).thenReturn(items);
        when(dataDefs.findValues("dims")).thenReturn(dims);
        when(dataDefs.findValues("fact")).thenReturn(fact);

        normalizer.addItemOrder(defs);

        verify(normalizers).addOrder(items);
        verify(normalizers).addOrder(dims);
        verify(normalizers).addOrder(fact);
    }

    @Test
    public void testAddFactsDim() throws IOException {
        JsonNode defs = Mockito.mock(JsonNode.class);
        JsonNode dataDefs = Mockito.mock(JsonNode.class);

        Map<String, JsonNode> map = new HashMap<>();
        map.put("foo", new TextNode("foo"));
        Entry<String, JsonNode> entry = map.entrySet().iterator().next();
        Iterator<Entry<String, JsonNode>> entries = map.entrySet().iterator();

        when(defs.at("/dataDefs")).thenReturn(dataDefs);
        when(dataDefs.fields()).thenReturn(entries);

        normalizer.addFactsDim(defs);

        verify(normalizers).addFactsDim(entry);
    }

    @Test
    public void testSetDefaultSteps() {
        JsonNode defs = Mockito.mock(JsonNode.class);
        String defaultStepsName = "foo";

        JsonNode taskGroupsDef = Mockito.mock(JsonNode.class);
        JsonNode task = Mockito.mock(JsonNode.class);
        Map<String, JsonNode> taskMap = new HashMap<>();
        taskMap.put("ttask", task);

        when(defs.at("/taskGroups")).thenReturn(taskGroupsDef);
        when(taskDefs.getAllTasks(taskGroupsDef)).thenReturn(taskMap);
        when(stepDefs.isStepsDefined(task)).thenReturn(false);
        when(task.isObject()).thenReturn(true);

        normalizer.setDefaultSteps(defs, defaultStepsName);

        verify(normalizers).setDefaultSteps(task, defaultStepsName);
    }

    @Test
    public void testSetDefaultStepsNotDefined() {
        JsonNode defs = Mockito.mock(JsonNode.class);
        String defaultStepsName = "foo";

        JsonNode taskGroupsDef = Mockito.mock(JsonNode.class);
        JsonNode task = Mockito.mock(JsonNode.class);
        Map<String, JsonNode> taskMap = new HashMap<>();
        taskMap.put("ttask", task);

        when(defs.at("/taskGroups")).thenReturn(taskGroupsDef);
        when(taskDefs.getAllTasks(taskGroupsDef)).thenReturn(taskMap);
        when(stepDefs.isStepsDefined(task)).thenReturn(false).thenReturn(true);
        when(task.isObject()).thenReturn(false).thenReturn(false);

        normalizer.setDefaultSteps(defs, defaultStepsName);

        normalizer.setDefaultSteps(defs, defaultStepsName);

        verifyNoInteractions(normalizers);
    }

    @Test
    public void testExpandTaskStepsIsMissingNode() throws DefNotFoundException {
        JsonNode defs = Mockito.mock(JsonNode.class);

        JsonNode topStepsDef = Mockito.mock(JsonNode.class);
        Map<String, JsonNode> topStepsMap = new HashMap<>();
        JsonNode taskGroupsDef = Mockito.mock(JsonNode.class);

        String key = "tkey";
        JsonNode jTask = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        JsonNode jSteps = Mockito.mock(JsonNode.class);
        Map<String, JsonNode> tasksMap = new HashMap<>();
        tasksMap.put(key, jTask);
        Map<String, JsonNode> stepsMap = new HashMap<>();
        stepsMap.put(key, jSteps);

        when(defs.at("/steps")).thenReturn(topStepsDef);
        when(stepDefs.getTopStepsMap(topStepsDef)).thenReturn(topStepsMap);
        when(defs.at("/taskGroups")).thenReturn(taskGroupsDef);
        when(taskDefs.getAllTasks(taskGroupsDef)).thenReturn(tasksMap);
        when(stepDefs.getStepsNodeMap(tasksMap)).thenReturn(stepsMap);

        when(jSteps.isMissingNode()).thenReturn(true);

        assertThrows(NoSuchElementException.class,
                () -> normalizer.expandTaskSteps(defs));
    }

    @Test
    public void testExpandTaskStepsIsTextual() throws DefNotFoundException {
        JsonNode defs = Mockito.mock(JsonNode.class);

        JsonNode topStepsDef = Mockito.mock(JsonNode.class);
        Map<String, JsonNode> topStepsMap = new HashMap<>();
        JsonNode taskGroupsDef = Mockito.mock(JsonNode.class);

        String key = "tkey";
        JsonNode jTask = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        JsonNode jSteps = Mockito.mock(JsonNode.class);
        Map<String, JsonNode> tasksMap = new HashMap<>();
        tasksMap.put(key, jTask);
        Map<String, JsonNode> stepsMap = new HashMap<>();
        stepsMap.put(key, jSteps);

        String stepsName = "seeder";
        JsonNode expandedStepsCopy = Mockito.mock(JsonNode.class);

        when(defs.at("/steps")).thenReturn(topStepsDef);
        when(stepDefs.getTopStepsMap(topStepsDef)).thenReturn(topStepsMap);
        when(defs.at("/taskGroups")).thenReturn(taskGroupsDef);
        when(taskDefs.getAllTasks(taskGroupsDef)).thenReturn(tasksMap);
        when(stepDefs.getStepsNodeMap(tasksMap)).thenReturn(stepsMap);

        when(jSteps.isMissingNode()).thenReturn(false);
        when(jSteps.isTextual()).thenReturn(true);
        when(jSteps.isObject()).thenReturn(false);

        when(jTask.path("steps").asText()).thenReturn(stepsName);
        when(stepDefs.getTopSteps(topStepsMap, stepsName).deepCopy())
                .thenReturn(expandedStepsCopy);

        normalizer.expandTaskSteps(defs);
        verify(normalizers).expandTaskSteps(jTask, expandedStepsCopy,
                stepsName);
        verifyNoMoreInteractions(normalizers);
    }

    @Test
    public void testExpandTaskStepsIsObjectStepDefined()
            throws DefNotFoundException {
        JsonNode defs = Mockito.mock(JsonNode.class);

        JsonNode topStepsDef = Mockito.mock(JsonNode.class);
        Map<String, JsonNode> topStepsMap = new HashMap<>();
        JsonNode taskGroupsDef = Mockito.mock(JsonNode.class);

        String key = "tkey";
        JsonNode jTask = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        JsonNode jSteps = Mockito.mock(JsonNode.class);
        Map<String, JsonNode> tasksMap = new HashMap<>();
        tasksMap.put(key, jTask);
        Map<String, JsonNode> stepsMap = new HashMap<>();
        stepsMap.put(key, jSteps);

        String stepsName = "jsoup";
        String stepName = "parser";
        JsonNode expandedStepsCopy = Mockito.mock(JsonNode.class);
        JsonNode overriddenStep = Mockito.mock(JsonNode.class);
        JsonNode overriddenStepCopy = Mockito.mock(JsonNode.class);
        Map<String, JsonNode> overriddenSteps = new HashMap<>();
        overriddenSteps.put(stepName, overriddenStep);

        when(defs.at("/steps")).thenReturn(topStepsDef);
        when(stepDefs.getTopStepsMap(topStepsDef)).thenReturn(topStepsMap);
        when(defs.at("/taskGroups")).thenReturn(taskGroupsDef);
        when(taskDefs.getAllTasks(taskGroupsDef)).thenReturn(tasksMap);
        when(stepDefs.getStepsNodeMap(tasksMap)).thenReturn(stepsMap);

        when(jSteps.isMissingNode()).thenReturn(false);
        when(jSteps.isTextual()).thenReturn(false);
        when(jSteps.isObject()).thenReturn(true);

        when(stepDefs.getStepsName(jSteps)).thenReturn(stepsName);
        when(stepDefs.getTopSteps(topStepsMap, stepsName).deepCopy())
                .thenReturn(expandedStepsCopy);

        when(stepDefs.getStepNodes(jSteps, stepsName))
                .thenReturn(overriddenSteps);
        when(overriddenStep.deepCopy()).thenReturn(overriddenStepCopy);

        when(stepDefs.isStepDefined(expandedStepsCopy, stepName))
                .thenReturn(true);

        // step defined
        normalizer.expandTaskSteps(defs);

        verify(normalizers).replaceStep(expandedStepsCopy, overriddenStepCopy,
                stepName);
        verify(normalizers).replaceSteps(jSteps, expandedStepsCopy, stepsName);
        verifyNoMoreInteractions(normalizers);
    }

    @Test
    public void testExpandTaskStepsIsObjectStepNotDefined()
            throws DefNotFoundException {
        JsonNode defs = Mockito.mock(JsonNode.class);

        JsonNode topStepsDef = Mockito.mock(JsonNode.class);
        Map<String, JsonNode> topStepsMap = new HashMap<>();
        JsonNode taskGroupsDef = Mockito.mock(JsonNode.class);

        String key = "tkey";
        JsonNode jTask = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        JsonNode jSteps = Mockito.mock(JsonNode.class);
        Map<String, JsonNode> tasksMap = new HashMap<>();
        tasksMap.put(key, jTask);
        Map<String, JsonNode> stepsMap = new HashMap<>();
        stepsMap.put(key, jSteps);

        String stepsName = "jsoup";
        String stepName = "parser";
        JsonNode expandedStepsCopy = Mockito.mock(JsonNode.class);
        JsonNode overriddenStep = Mockito.mock(JsonNode.class);
        JsonNode overriddenStepCopy = Mockito.mock(JsonNode.class);
        Map<String, JsonNode> overriddenSteps = new HashMap<>();
        overriddenSteps.put(stepName, overriddenStep);

        when(defs.at("/steps")).thenReturn(topStepsDef);
        when(stepDefs.getTopStepsMap(topStepsDef)).thenReturn(topStepsMap);
        when(defs.at("/taskGroups")).thenReturn(taskGroupsDef);
        when(taskDefs.getAllTasks(taskGroupsDef)).thenReturn(tasksMap);
        when(stepDefs.getStepsNodeMap(tasksMap)).thenReturn(stepsMap);

        when(jSteps.isMissingNode()).thenReturn(false);
        when(jSteps.isTextual()).thenReturn(false);
        when(jSteps.isObject()).thenReturn(true);

        when(stepDefs.getStepsName(jSteps)).thenReturn(stepsName);
        when(stepDefs.getTopSteps(topStepsMap, stepsName).deepCopy())
                .thenReturn(expandedStepsCopy);

        when(stepDefs.getStepNodes(jSteps, stepsName))
                .thenReturn(overriddenSteps);
        when(overriddenStep.deepCopy()).thenReturn(overriddenStepCopy);

        when(stepDefs.isStepDefined(expandedStepsCopy, stepName))
                .thenReturn(false);

        // step not defined
        normalizer.expandTaskSteps(defs);

        verify(normalizers).insertStep(expandedStepsCopy, overriddenStepCopy,
                stepName);
        verify(normalizers).replaceSteps(jSteps, expandedStepsCopy, stepsName);
        verifyNoMoreInteractions(normalizers);
    }

    @Test
    public void testExpandSteps() throws DefNotFoundException {
        JsonNode defs = Mockito.mock(JsonNode.class);

        String stepsName = "parser";
        ObjectNode topStepsDef = Mockito.mock(ObjectNode.class);
        JsonNode steps = Mockito.mock(JsonNode.class);
        Map<String, JsonNode> topStepsMap = new HashMap<>();
        topStepsMap.put(stepsName, steps);
        JsonNode extSteps = Mockito.mock(JsonNode.class);

        when(defs.at("/steps")).thenReturn(topStepsDef);
        when(stepDefs.getTopStepsMap(topStepsDef)).thenReturn(topStepsMap);
        when(normalizers.expandSteps(topStepsMap, steps)).thenReturn(extSteps);

        normalizer.expandSteps(defs);

        verify(topStepsDef).set(stepsName, extSteps);
    }

}
