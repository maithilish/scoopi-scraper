package org.codetab.scoopi.defs.yml;

import static java.util.Objects.isNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import org.assertj.core.util.Lists;
import org.codetab.scoopi.defs.yml.helper.NormalizerHelper;
import org.codetab.scoopi.exception.CriticalException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Methods to normalise user defs and to create effective defs.
 *
 * @author maithilish
 *
 */
public class DefsNormalizer {

    @Inject
    private ObjectMapper mapper;
    @Inject
    private NormalizerHelper nzHelper;

    /**
     * If index or indexRange fields are not defined, then add index field set
     * to zero.
     * @param nodes
     * @throws IOException
     */
    public void addItemIndex(final JsonNode nodes) throws IOException {
        JsonNode dataDefs = nodes.at("/dataDefs");
        List<JsonNode> items = dataDefs.findValues("item");
        for (JsonNode item : items) {
            if (isNull(item.findValue("index"))
                    && isNull(item.findValue("indexRange"))) {
                ((ObjectNode) item).put("index", 0);
            }
        }
    }

    /**
     * If order field is not defined, then add order
     * @param nodes
     * @throws IOException
     */
    public void addItemOrder(final JsonNode nodes) throws IOException {
        JsonNode dataDefs = nodes.at("/dataDefs");
        List<JsonNode> itemsList = dataDefs.findValues("items");
        for (JsonNode items : itemsList) {
            List<JsonNode> itemList = items.findValues("item");
            for (int i = 0; i < itemList.size(); i++) {
                JsonNode item = itemList.get(i);
                if (isNull(item.findValue("order"))) {
                    ((ObjectNode) item).put("order", i);
                }
            }
        }
    }

    /**
     * Add default items field to fact axis. Any existing items field is
     * replaced with default items
     * @param nodes
     * @throws IOException
     */
    public void addFactItem(final JsonNode nodes) throws IOException {
        JsonNode dataDefs = nodes.at("/dataDefs");
        ArrayList<String> names = Lists.newArrayList(dataDefs.fieldNames());
        for (String name : names) {
            String path = String.join("/", "", name, "axis", "fact");
            JsonNode fact = dataDefs.at(path);
            String itemYml = "[{\"item\": {\"name\": \"fact\"}}]";
            JsonNode item = mapper.readTree(itemYml);
            ((ObjectNode) fact).replace("items", item);
        }
    }

    /**
     * Add missing query (noQuery) to axis.
     * @param nodes
     * @throws IOException
     */
    public void addNoQuery(final JsonNode nodes) throws IOException {
        JsonNode dataDefs = nodes.at("/dataDefs");
        ArrayList<String> names = Lists.newArrayList(dataDefs.fieldNames());
        for (String name : names) {
            String path = String.join("/", "", name, "axis");
            JsonNode axes = dataDefs.at(path);
            for (String axisName : Lists.newArrayList(axes.fieldNames())) {
                path = String.join("/", "", axisName);
                JsonNode axis = axes.at(path);
                JsonNode query = axis.at("/query");
                if (query.isMissingNode()) {
                    String queryYml =
                            "{ \"region\": \"undefined\", \"field\": \"undefined\" }";
                    query = mapper.readTree(queryYml);
                    ((ObjectNode) axis).replace("query", query);
                }
            }
        }
    }

    /**
     * If steps is not defined for the task, then add field - steps: default
     * @param defs
     */
    public void setDefaultSteps(final JsonNode defs,
            final String defaultStepsName) {
        JsonNode taskGroups = nzHelper.getTaskGroups(defs);
        Map<String, JsonNode> tasks = nzHelper.getTasks(taskGroups);
        for (JsonNode task : tasks.values()) {
            if (!nzHelper.isStepsDefined(task)) {
                nzHelper.setDefaultSteps(task, defaultStepsName);
            }
        }
    }

    /**
     * In the deep copy of steps node, the steps are replaced with overridden
     * steps defined at task level. The steps field defined in the task is
     * replaced with deep copy.
     * @param defs
     */
    public void expandOverriddenSteps(final JsonNode defs) {
        Map<String, JsonNode> stepsMap =
                nzHelper.getStepsMap(nzHelper.getStepsDef(defs));

        JsonNode taskGroups = nzHelper.getTaskGroups(defs);
        Map<String, JsonNode> allSteps = nzHelper.getSteps(taskGroups);

        for (JsonNode steps : allSteps.values()) {
            if (steps.isObject()) {
                String stepsName = nzHelper.getOverriddenStepsName(steps);
                if (isNull(stepsMap.get(stepsName))) {
                    String message = String.join(" ", "steps:", stepsName,
                            "not defined");
                    throw new CriticalException(message);
                }
                Map<String, JsonNode> overriddenSteps =
                        nzHelper.getOverridenSteps(stepsName, steps);
                ObjectNode expandedSteps =
                        (ObjectNode) stepsMap.get(stepsName).deepCopy();
                for (String stepName : overriddenSteps.keySet()) {
                    expandedSteps.set(stepName, overriddenSteps.get(stepName));
                }
                // ((ObjectNode) steps).remove(stepsName);
                ((ObjectNode) steps).set(stepsName, expandedSteps);
            }
        }
    }

    /**
     * Replace named steps with the deep copy of steps node. For example steps:
     * default is replaced with deep copy of default steps node.
     *
     * @param defs
     */
    public void expandSteps(final JsonNode defs) {
        Map<String, JsonNode> stepsMap =
                nzHelper.getStepsMap(nzHelper.getStepsDef(defs));

        JsonNode taskGroups = nzHelper.getTaskGroups(defs);
        Map<String, JsonNode> tasks = nzHelper.getTasks(taskGroups);

        for (String taskName : tasks.keySet()) {
            JsonNode task = tasks.get(taskName);
            JsonNode steps = task.get("steps");
            if (isNull(steps)) {
                throw new NoSuchElementException(
                        "steps not defined for task : " + taskName);
            }
            if (steps.isTextual()) {
                String stepsName = steps.asText();
                if (stepsMap.containsKey(stepsName)) {
                    ObjectNode expandedSteps =
                            (ObjectNode) stepsMap.get(stepsName).deepCopy();
                    ObjectNode stepsNode = mapper.createObjectNode();
                    stepsNode.set(stepsName, expandedSteps);
                    ((ObjectNode) task).set("steps", stepsNode);
                } else {
                    throw new NoSuchElementException(
                            "steps not defined : " + stepsName);
                }
            }
        }
    }
}
