package org.codetab.scoopi.defs.yml;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map.Entry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RunWith(MockitoJUnitRunner.class)
public class NormalizersBackupTest {

    @InjectMocks
    private Normalizers normalizers;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private Jacksons jacksons;

    @Mock
    private StepDefs stepDefs;

    // @Before()
    // public void setUp() throws Exception {
    // MockitoAnnotations.openMocks(this);
    // }

    // @Test
    // public void testAddOrder() {
    // JsonNode item = Mockito.mock(JsonNode.class);
    // List<JsonNode> itemList = new ArrayList<>();
    // int i = 1;
    // List<JsonNode> itemsList = new ArrayList<>();
    // when(item.findValue("order")).thenReturn();
    // normalizers.addOrder(itemsList);
    // }
    //
    // @Test
    // public void testAddIndex() {
    // List<JsonNode> itemList = new ArrayList<>();
    // List<JsonNode> itemsList = new ArrayList<>();
    // normalizers.addIndex(itemsList);
    // }

    @SuppressWarnings({"unchecked"})
    @Test
    public void testAddFactsDim() throws Exception {
        JsonNode jFactsDim = Mockito.mock(JsonNode.class);
        JsonNode jFact = Mockito.mock(JsonNode.class);
        String factJson = "";
        ObjectNode jDataDef = Mockito.mock(ObjectNode.class);
        Entry<String, JsonNode> entry = Mockito.mock(Entry.class);
        when(entry.getValue()).thenReturn(jDataDef);
        when(jDataDef.findPath("facts")).thenReturn(jFact);
        when(jFact.isMissingNode()).thenReturn(true);
        when(jacksons.parseJson("[{item: {name: fact}}]")).thenReturn(factJson);
        when(mapper.readTree(factJson)).thenReturn(jFactsDim);

        normalizers.addFactsDim(entry);

        verify(jDataDef).replace("facts", jFactsDim);
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void testAddFactsDimExists() throws Exception {
        JsonNode jFact = Mockito.mock(JsonNode.class);
        ObjectNode jDataDef = Mockito.mock(ObjectNode.class);
        Entry<String, JsonNode> entry = Mockito.mock(Entry.class);
        when(entry.getValue()).thenReturn(jDataDef);
        when(jDataDef.findPath("facts")).thenReturn(jFact);
        when(jFact.isMissingNode()).thenReturn(false);

        normalizers.addFactsDim(entry);

        verify(jDataDef).findPath("facts");
        verifyNoMoreInteractions(jDataDef);
    }

    @Test
    public void testSetDefaultSteps() {
        ObjectNode task = Mockito.mock(ObjectNode.class);
        String defaultStepsName = "";
        normalizers.setDefaultSteps(task, defaultStepsName);

        verify(task).put("steps", defaultStepsName);
        verifyNoMoreInteractions(task);
    }

    @Test
    public void testExpandTaskSteps() {
        ObjectNode stepsNode = Mockito.mock(ObjectNode.class);
        ObjectNode taskDef = Mockito.mock(ObjectNode.class);
        JsonNode expandedSteps = Mockito.mock(JsonNode.class);
        String stepsName = "";
        when(mapper.createObjectNode()).thenReturn(stepsNode);
        normalizers.expandTaskSteps(taskDef, expandedSteps, stepsName);
        verify(stepsNode).set(stepsName, expandedSteps);
        verify(taskDef).set("steps", stepsNode);
        verifyNoMoreInteractions(stepsNode, taskDef);
    }

    @Test
    public void testReplaceSteps() {
        ObjectNode steps = Mockito.mock(ObjectNode.class);
        JsonNode replaceSteps = Mockito.mock(JsonNode.class);
        String stepsName = "";
        normalizers.replaceSteps(steps, replaceSteps, stepsName);
        verify(steps).set(stepsName, replaceSteps);
        verifyNoMoreInteractions(steps);
    }

    @Test
    public void testReplaceStep() {
        ObjectNode steps = Mockito.mock(ObjectNode.class);
        JsonNode replaceStep = Mockito.mock(JsonNode.class);
        String stepName = "";
        normalizers.replaceStep(steps, replaceStep, stepName);
        verify(steps).set(stepName, replaceStep);
        verifyNoMoreInteractions(steps);
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
        String stepName = "bazz";

        when(insertStep.findValue("previous").asText()).thenReturn(prev);
        when(insertStep.findValue("next").asText()).thenReturn(next);
        when(steps.findValue(prev)).thenReturn(prevStep);
        when(steps.findValue(next)).thenReturn(nextStep);
        when(nextStep.findValue("previous").asText()).thenReturn(nextStepPrev);

        normalizers.insertStep(steps, insertStep, stepName);

        verify(steps).findValue(next);
        verify(prevStep).put("next", stepName);
        verify(nextStep).put("previous", stepName);
        verify(steps).set(stepName, insertStep);
    }

    @Test
    public void testInsertStepNextStepPrevIsStart() {
        String next = "foo";
        String prev = "bar";
        String nextStepPrev = "start";
        ObjectNode prevStep = Mockito.mock(ObjectNode.class);
        ObjectNode nextStep =
                Mockito.mock(ObjectNode.class, RETURNS_DEEP_STUBS);
        ObjectNode steps = Mockito.mock(ObjectNode.class);
        JsonNode insertStep = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        String stepName = "bazz";
        when(insertStep.findValue("previous").asText()).thenReturn(prev);
        when(insertStep.findValue("next").asText()).thenReturn(next);
        when(steps.findValue(prev)).thenReturn(prevStep);
        when(steps.findValue(next)).thenReturn(nextStep);
        when(nextStep.findValue("previous").asText()).thenReturn(nextStepPrev);
        normalizers.insertStep(steps, insertStep, stepName);
        verify(prevStep).put("next", stepName);
        // verify(nextStep).put("previous", stepName);
        verify(steps).set(stepName, insertStep);
        // verifyNoMoreInteractions(prevStep, nextStep, steps);
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
        String stepName = "bazz";
        when(insertStep.findValue("previous").asText()).thenReturn(prev);
        when(insertStep.findValue("next").asText()).thenReturn(next);
        when(steps.findValue(prev)).thenReturn(prevStep);
        when(steps.findValue(next)).thenReturn(null);
        when(nextStep.findValue("previous").asText()).thenReturn(nextStepPrev);
        normalizers.insertStep(steps, insertStep, stepName);
        verify(prevStep).put("next", stepName);
        // verify(nextStep).put("previous", stepName);
        verify(steps).set(stepName, insertStep);
        // verifyNoMoreInteractions(prevStep, nextStep, steps);
    }
}
