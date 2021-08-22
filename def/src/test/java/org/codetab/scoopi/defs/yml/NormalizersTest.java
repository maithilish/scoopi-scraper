package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class NormalizersTest {

    @InjectMocks
    private Normalizers normalizers;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private Jacksons jacksons;

    @Mock
    private StepDefs stepDefs;

    @Before()
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAddOrder() {
        ObjectNode item = Mockito.mock(ObjectNode.class);
        List<JsonNode> itemList = new ArrayList<>();
        itemList.add(item);

        JsonNode items = Mockito.mock(JsonNode.class);
        List<JsonNode> itemsList = new ArrayList<>();
        itemsList.add(items);

        when(item.findValue("order")).thenReturn(null);
        when(items.findValues("item")).thenReturn(itemList);

        normalizers.addOrder(itemsList);

        verify(item).put("order", 1);
    }

    @Test
    public void testAddOrderAlreadySet() {
        ObjectNode item = Mockito.mock(ObjectNode.class);
        List<JsonNode> itemList = new ArrayList<>();
        itemList.add(item);

        JsonNode items = Mockito.mock(JsonNode.class);
        List<JsonNode> itemsList = new ArrayList<>();
        itemsList.add(items);

        when(item.findValue("order")).thenReturn(new TextNode("foo"));
        when(items.findValues("item")).thenReturn(itemList);

        normalizers.addOrder(itemsList);

        verify(item, times(0)).put("order", 1);
    }

    @Test
    public void testAddIndex() {
        ObjectNode item = Mockito.mock(ObjectNode.class);
        List<JsonNode> itemList = new ArrayList<>();
        itemList.add(item);

        JsonNode items = Mockito.mock(JsonNode.class);
        List<JsonNode> itemsList = new ArrayList<>();
        itemsList.add(items);

        when(item.findValue("index")).thenReturn(null)
                .thenReturn(new TextNode("foo")).thenReturn(null)
                .thenReturn(new TextNode("foo"));
        when(item.findValue("indexRange")).thenReturn(null)
                .thenReturn(new TextNode("foo")).thenReturn(new TextNode("foo"))
                .thenReturn(null);
        when(items.findValues("item")).thenReturn(itemList);

        normalizers.addIndex(itemsList);
        verify(item, times(1)).put("index", 1);

        normalizers.addIndex(itemsList);
        verify(item, times(1)).put("index", 1);

        normalizers.addIndex(itemsList);
        verify(item, times(1)).put("index", 1);

        normalizers.addIndex(itemsList);
        verify(item, times(1)).put("index", 1);
    }

    @Test
    public void testAddFactsDim() throws Exception {
        JsonNode jFactsDim = Mockito.mock(JsonNode.class);
        JsonNode jFact = Mockito.mock(JsonNode.class);
        String factJson = "foo";
        ObjectNode jDataDef = Mockito.mock(ObjectNode.class);
        @SuppressWarnings("unchecked")
        Entry<String, JsonNode> entry = Mockito.mock(Entry.class);

        when(entry.getValue()).thenReturn(jDataDef);
        when(jDataDef.findPath("facts")).thenReturn(jFact);
        when(jFact.isMissingNode()).thenReturn(true);
        when(jacksons.parseJson("[{item: {name: fact}}]")).thenReturn(factJson);
        when(mapper.readTree(factJson)).thenReturn(jFactsDim);

        normalizers.addFactsDim(entry);

        verify(jDataDef).replace("facts", jFactsDim);
    }

    @Test
    public void testAddFactsDimAlreadyExists() throws Exception {
        JsonNode jFactsDim = Mockito.mock(JsonNode.class);
        JsonNode jFact = Mockito.mock(JsonNode.class);
        ObjectNode jDataDef = Mockito.mock(ObjectNode.class);
        @SuppressWarnings("unchecked")
        Entry<String, JsonNode> entry = Mockito.mock(Entry.class);

        when(entry.getValue()).thenReturn(jDataDef);
        when(jDataDef.findPath("facts")).thenReturn(jFact);
        when(jFact.isMissingNode()).thenReturn(false); // exists

        normalizers.addFactsDim(entry);

        verify(jDataDef, times(0)).replace("facts", jFactsDim);
    }

    @Test
    public void testSetDefaultSteps() {
        ObjectNode task = Mockito.mock(ObjectNode.class);
        String defaultStepsName = "foo";

        normalizers.setDefaultSteps(task, defaultStepsName);

        verify(task).put("steps", defaultStepsName);
    }

    @Test
    public void testExpandTaskSteps() {
        ObjectNode stepsNode = Mockito.mock(ObjectNode.class);
        ObjectNode taskDef = Mockito.mock(ObjectNode.class);
        JsonNode expandedSteps = Mockito.mock(JsonNode.class);
        String stepsName = "foo";

        when(mapper.createObjectNode()).thenReturn(stepsNode);

        normalizers.expandTaskSteps(taskDef, expandedSteps, stepsName);

        verify(stepsNode).set(stepsName, expandedSteps);
        verify(taskDef).set("steps", stepsNode);
    }

    @Test
    public void testReplaceSteps() {
        ObjectNode steps = Mockito.mock(ObjectNode.class);
        JsonNode replaceSteps = Mockito.mock(JsonNode.class);
        String stepsName = "foo";

        normalizers.replaceSteps(steps, replaceSteps, stepsName);

        verify(steps).set(stepsName, replaceSteps);
    }

    @Test
    public void testReplaceStep() {
        ObjectNode steps = Mockito.mock(ObjectNode.class);
        JsonNode replaceStep = Mockito.mock(JsonNode.class);
        String stepName = "foo";

        normalizers.replaceStep(steps, replaceStep, stepName);

        verify(steps).set(stepName, replaceStep);
    }

    @Test
    public void testInsertStep() {
        String next = "foo";
        String prev = "bar";
        String nextStepPrev = "baz";
        ObjectNode prevStep = Mockito.mock(ObjectNode.class);
        ObjectNode nextStep =
                Mockito.mock(ObjectNode.class, RETURNS_DEEP_STUBS);
        ObjectNode steps = Mockito.mock(ObjectNode.class);
        JsonNode insertStep = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        String stepName = "qux";

        when(insertStep.findValue("previous").asText()).thenReturn(prev);
        when(insertStep.findValue("next").asText()).thenReturn(next);
        when(steps.findValue(prev)).thenReturn(prevStep);
        when(steps.findValue(next)).thenReturn(nextStep);
        when(nextStep.findValue("previous").asText()).thenReturn(nextStepPrev);

        normalizers.insertStep(steps, insertStep, stepName);

        verify(prevStep).put("next", stepName);
        verify(nextStep).put("previous", stepName);
        verify(steps).set(stepName, insertStep);
    }

    @Test
    public void testInsertStepPreviousStepIsStart() {
        String next = "foo";
        String prev = "bar";
        String nextStepPrev = "start";
        ObjectNode prevStep = Mockito.mock(ObjectNode.class);
        ObjectNode nextStep =
                Mockito.mock(ObjectNode.class, RETURNS_DEEP_STUBS);
        ObjectNode steps = Mockito.mock(ObjectNode.class);
        JsonNode insertStep = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        String stepName = "qux";

        when(insertStep.findValue("previous").asText()).thenReturn(prev);
        when(insertStep.findValue("next").asText()).thenReturn(next);
        when(steps.findValue(prev)).thenReturn(prevStep);
        when(steps.findValue(next)).thenReturn(nextStep);
        when(nextStep.findValue("previous").asText()).thenReturn(nextStepPrev);

        normalizers.insertStep(steps, insertStep, stepName);

        verify(prevStep).put("next", stepName);
        verify(nextStep, times(0)).put("previous", stepName);
        verify(steps).set(stepName, insertStep);
    }

    @Test
    public void testInsertStepNextStepNull() {
        String next = "foo";
        String prev = "bar";
        String nextStepPrev = "baz";
        ObjectNode prevStep = Mockito.mock(ObjectNode.class);
        ObjectNode nextStep =
                Mockito.mock(ObjectNode.class, RETURNS_DEEP_STUBS);
        ObjectNode steps = Mockito.mock(ObjectNode.class);
        JsonNode insertStep = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        String stepName = "qux";

        when(insertStep.findValue("previous").asText()).thenReturn(prev);
        when(insertStep.findValue("next").asText()).thenReturn(next);
        when(steps.findValue(prev)).thenReturn(prevStep);
        when(steps.findValue(next)).thenReturn(null);
        when(nextStep.findValue("previous").asText()).thenReturn(nextStepPrev);

        normalizers.insertStep(steps, insertStep, stepName);

        verify(prevStep).put("next", stepName);
        verify(nextStep, times(0)).put("previous", stepName);
        verify(steps).set(stepName, insertStep);
    }

    // TODO - cleanup and verify
    @Test
    public void testExpandSteps() {

        JsonNode extSteps = Mockito.mock(JsonNode.class);
        JsonNode oStep = Mockito.mock(JsonNode.class);
        JsonNode oStepsDeepCopy = Mockito.mock(JsonNode.class);
        ObjectNode stepsNode = Mockito.mock(ObjectNode.class);
        JsonNode overridenSteps = Mockito.mock(JsonNode.class);

        String oStepName = "foo";
        JsonNode childSteps = Mockito.mock(JsonNode.class);
        String childStepsName = "bar";

        Map<String, JsonNode> mapA = new HashMap<>();
        mapA.put(childStepsName, overridenSteps);
        Iterator<Entry<String, JsonNode>> entries = mapA.entrySet().iterator();

        Map<String, JsonNode> mapB = new HashMap<>();
        mapB.put(oStepName, oStep);
        Iterator<Entry<String, JsonNode>> it = mapB.entrySet().iterator();

        Map<String, JsonNode> stepsMap = new HashMap<>();
        stepsMap.put(childStepsName, childSteps);

        JsonNode steps = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        JsonNode stepsDeepCopy = Mockito.mock(JsonNode.class);

        when(stepDefs.isNestedSteps(steps)).thenReturn(true);
        when(steps.deepCopy()).thenReturn(stepsDeepCopy)
                .thenReturn(stepsDeepCopy);
        when(steps.fields()).thenReturn(entries);

        when(normalizers.expandSteps(stepsMap, childSteps))
                .thenReturn(extSteps);

        when(extSteps.deepCopy()).thenReturn(stepsNode);
        when(overridenSteps.fields()).thenReturn(it);

        when(stepDefs.isStepDefined(stepsNode, oStepName)).thenReturn(true);
        when(oStep.deepCopy()).thenReturn(oStepsDeepCopy);

        JsonNode actual = normalizers.expandSteps(stepsMap, steps);

        assertThat(actual).isSameAs(stepsNode);
    }

    @Test
    public void testExpandStepsStepsNotDefined() {

        JsonNode extSteps = Mockito.mock(JsonNode.class);
        JsonNode oStep = Mockito.mock(JsonNode.class);
        JsonNode oStepsDeepCopy =
                Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        ObjectNode stepsNode =
                Mockito.mock(ObjectNode.class, RETURNS_DEEP_STUBS);
        JsonNode overridenSteps = Mockito.mock(JsonNode.class);

        String oStepName = "foo";
        JsonNode childSteps = Mockito.mock(JsonNode.class);
        String childStepsName = "bar";

        Map<String, JsonNode> mapA = new HashMap<>();
        mapA.put(childStepsName, overridenSteps);
        Iterator<Entry<String, JsonNode>> entries = mapA.entrySet().iterator();

        Map<String, JsonNode> mapB = new HashMap<>();
        mapB.put(oStepName, oStep);
        Iterator<Entry<String, JsonNode>> it = mapB.entrySet().iterator();

        Map<String, JsonNode> stepsMap = new HashMap<>();
        stepsMap.put(childStepsName, childSteps);

        JsonNode steps = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        JsonNode stepsDeepCopy = Mockito.mock(JsonNode.class);

        when(stepDefs.isNestedSteps(steps)).thenReturn(true);
        when(steps.deepCopy()).thenReturn(stepsDeepCopy)
                .thenReturn(stepsDeepCopy);
        when(steps.fields()).thenReturn(entries);

        when(normalizers.expandSteps(stepsMap, childSteps))
                .thenReturn(extSteps);

        when(extSteps.deepCopy()).thenReturn(stepsNode);
        when(overridenSteps.fields()).thenReturn(it);

        when(stepDefs.isStepDefined(stepsNode, oStepName)).thenReturn(false);
        when(oStep.deepCopy()).thenReturn(oStepsDeepCopy);

        // from insertStep method
        String next = "tfoo";
        String prev = "tbar";
        String nextStepPrev = "tbaz";
        ObjectNode prevStep =
                Mockito.mock(ObjectNode.class, RETURNS_DEEP_STUBS);
        ObjectNode nextStep =
                Mockito.mock(ObjectNode.class, RETURNS_DEEP_STUBS);
        JsonNode insertStep = oStepsDeepCopy;

        when(insertStep.findValue("previous").asText()).thenReturn(prev);
        when(insertStep.findValue("next").asText()).thenReturn(next);
        when(stepsNode.findValue(prev)).thenReturn(prevStep);
        when(stepsNode.findValue(next)).thenReturn(nextStep);
        when(nextStep.findValue("previous").asText()).thenReturn(nextStepPrev);

        JsonNode actual = normalizers.expandSteps(stepsMap, steps);

        assertThat(actual).isSameAs(stepsNode);

        // verify(prevStep).put("next", "foo");
        // verify(nextStep).put("previous", "previous");
        // verify(stepsNode).set(stepName, insertStep);
    }

    @Test
    public void testExpandStepsStepsNotNested() {

        Map<String, JsonNode> stepsMap = new HashMap<>();

        JsonNode steps = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        JsonNode stepsDeepCopy = Mockito.mock(JsonNode.class);

        when(stepDefs.isNestedSteps(steps)).thenReturn(false);

        when(steps.deepCopy()).thenReturn(stepsDeepCopy);

        JsonNode actual = normalizers.expandSteps(stepsMap, steps);

        assertThat(actual).isSameAs(stepsDeepCopy);
    }
}
