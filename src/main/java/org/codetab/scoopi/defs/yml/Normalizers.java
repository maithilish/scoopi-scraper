package org.codetab.scoopi.defs.yml;

import static java.util.Objects.isNull;

import java.io.IOException;
import java.util.List;
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

    public void addOrder(List<JsonNode> itemsList) {
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

    public void addIndex(List<JsonNode> itemsList) {
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

    public void addFact(Entry<String, JsonNode> entry) throws IOException {
        JsonNode jDataDef = entry.getValue();
        JsonNode jFact = jDataDef.findPath("fact");
        if (jFact.isMissingNode()) {
            String factJson = jacksons.parseJson("[{item: {name: fact}}]");
            JsonNode jFactDim = mapper.readTree(factJson);
            ((ObjectNode) jDataDef).replace("fact", jFactDim);
        }
    }

    public void setDefaultSteps(final JsonNode task,
            final String defaultStepsName) {
        ((ObjectNode) task).put("steps", defaultStepsName);
    }

    public void expandSteps(JsonNode taskDef, JsonNode expandedSteps,
            final String stepsName) {
        ObjectNode stepsNode = mapper.createObjectNode();
        stepsNode.set(stepsName, expandedSteps);
        ((ObjectNode) taskDef).set("steps", stepsNode);
    }

    public void replaceSteps(JsonNode steps, JsonNode replaceSteps,
            String stepsName) {
        ((ObjectNode) steps).set(stepsName, replaceSteps);
    }

    public void replaceStep(JsonNode steps, JsonNode replaceStep,
            String stepName) {
        ((ObjectNode) steps).set(stepName, replaceStep);
    }

    public void insertStep(JsonNode steps, JsonNode insertStep,
            String stepName) {
        String prev = insertStep.findValue("previous").asText();
        String next = insertStep.findValue("next").asText();

        JsonNode prevStep = steps.findValue(prev);
        JsonNode nextStep = steps.findValue(next);

        ((ObjectNode) prevStep).put("next", stepName);
        ((ObjectNode) nextStep).put("previous", stepName);

        ((ObjectNode) steps).set(stepName, insertStep);
    }

}
