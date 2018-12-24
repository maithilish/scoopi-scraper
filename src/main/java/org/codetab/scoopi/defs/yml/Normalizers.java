package org.codetab.scoopi.defs.yml;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class Normalizers {

    @Inject
    private ObjectMapper mapper;
    @Inject
    private Jacksons jacksons;
    @Inject
    private StepDefs stepDefs;

    public void addOrder(final List<JsonNode> itemsList) {
        for (JsonNode items : itemsList) {
            List<JsonNode> itemList = items.findValues("item");
            for (int i = 0; i < itemList.size(); i++) {
                JsonNode item = itemList.get(i);
                if (isNull(item.findValue("order"))) {
                    ((ObjectNode) item).put("order", i + 1);
                }
            }
        }
    }

    public void addIndex(final List<JsonNode> itemsList) {
        for (JsonNode items : itemsList) {
            List<JsonNode> itemList = items.findValues("item");
            for (JsonNode item : itemList) {
                if (isNull(item.findValue("index"))
                        && isNull(item.findValue("indexRange"))) {
                    ((ObjectNode) item).put("index", 1);
                }
            }
        }
    }

    public void addFactsDim(final Entry<String, JsonNode> entry)
            throws IOException {
        JsonNode jDataDef = entry.getValue();
        JsonNode jFact = jDataDef.findPath("facts");
        if (jFact.isMissingNode()) {
            String factJson = jacksons.parseJson("[{item: {name: fact}}]");
            JsonNode jFactsDim = mapper.readTree(factJson);
            ((ObjectNode) jDataDef).replace("facts", jFactsDim);
        }
    }

    public void setDefaultSteps(final JsonNode task,
            final String defaultStepsName) {
        ((ObjectNode) task).put("steps", defaultStepsName);
    }

    public void expandTaskSteps(final JsonNode taskDef,
            final JsonNode expandedSteps, final String stepsName) {
        ObjectNode stepsNode = mapper.createObjectNode();
        stepsNode.set(stepsName, expandedSteps);
        ((ObjectNode) taskDef).set("steps", stepsNode);
    }

    public void replaceSteps(final JsonNode steps, final JsonNode replaceSteps,
            final String stepsName) {
        ((ObjectNode) steps).set(stepsName, replaceSteps);
    }

    public void replaceStep(final JsonNode steps, final JsonNode replaceStep,
            final String stepName) {
        ((ObjectNode) steps).set(stepName, replaceStep);
    }

    public void insertStep(final JsonNode steps, final JsonNode insertStep,
            final String stepName) {
        String prev = insertStep.findValue("previous").asText();
        String next = insertStep.findValue("next").asText();

        JsonNode prevStep = steps.findValue(prev);
        ((ObjectNode) prevStep).put("next", stepName);

        JsonNode nextStep = steps.findValue(next);
        if (nonNull(nextStep)) {
            String nextStepPrev = nextStep.findValue("previous").asText();
            if (!nextStepPrev.equalsIgnoreCase("start")) {
                ((ObjectNode) nextStep).put("previous", stepName);
            }
        }

        ((ObjectNode) steps).set(stepName, insertStep);
    }

    public JsonNode expandSteps(final Map<String, JsonNode> stepsMap,
            final JsonNode steps) {
        if (stepDefs.isNestedSteps(steps)) {
            Iterator<Entry<String, JsonNode>> entries = steps.fields();
            Entry<String, JsonNode> entry = entries.next();
            String childStepsName = entry.getKey();
            JsonNode childSteps = stepsMap.get(childStepsName);
            JsonNode overridenSteps = entry.getValue();

            // ObjectNode stepsNode = mapper.createObjectNode();

            JsonNode extSteps = expandSteps(stepsMap, childSteps);

            JsonNode stepsNode = extSteps.deepCopy();
            // Iterator<Entry<String, JsonNode>> it = extSteps.fields();
            // while (it.hasNext()) {
            // Entry<String, JsonNode> e = it.next();
            // stepsNode.set(e.getKey(), e.getValue());
            // }

            Iterator<Entry<String, JsonNode>> it = overridenSteps.fields();
            while (it.hasNext()) {
                Entry<String, JsonNode> e = it.next();
                String oStepName = e.getKey();
                JsonNode oStep = e.getValue();
                if (stepDefs.isStepDefined(stepsNode, oStepName)) {
                    replaceStep(stepsNode, oStep.deepCopy(), oStepName);
                } else {
                    insertStep(stepsNode, oStep.deepCopy(), oStepName);
                }
            }
            return stepsNode;
        } else {
            // ObjectNode stepsNode = mapper.createObjectNode();
            // Iterator<Entry<String, JsonNode>> it = steps.fields();
            // while (it.hasNext()) {
            // Entry<String, JsonNode> e = it.next();
            // stepsNode.set(e.getKey(), e.getValue());
            // }
            // return stepsNode;
            return steps.deepCopy();
        }
    }

}
