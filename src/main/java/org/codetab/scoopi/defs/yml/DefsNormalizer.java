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
import org.codetab.scoopi.util.Util;

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
    public void addMemberIndex(final JsonNode nodes) throws IOException {
        JsonNode dataDefs = nodes.at("/dataDefs");
        List<JsonNode> members = dataDefs.findValues("member");
        for (JsonNode member : members) {
            if (isNull(member.findValue("index"))
                    && isNull(member.findValue("indexRange"))) {
                ((ObjectNode) member).put("index", 0);
            }
        }
    }

    /**
     * If order field is not defined, then add order
     * @param nodes
     * @throws IOException
     */
    public void addMemberOrder(final JsonNode nodes) throws IOException {
        JsonNode dataDefs = nodes.at("/dataDefs");
        List<JsonNode> membersList = dataDefs.findValues("members");
        for (JsonNode members : membersList) {
            List<JsonNode> memberList = members.findValues("member");
            for (int i = 0; i < memberList.size(); i++) {
                JsonNode member = memberList.get(i);
                if (isNull(member.findValue("order"))) {
                    ((ObjectNode) member).put("order", i);
                }
            }
        }
    }

    /**
     * Add default members field to fact axis. Any existing members field is
     * replaced with default members
     * @param nodes
     * @throws IOException
     */
    public void addFactMember(final JsonNode nodes) throws IOException {
        JsonNode dataDefs = nodes.at("/dataDefs");
        ArrayList<String> names = Lists.newArrayList(dataDefs.fieldNames());
        for (String name : names) {
            String path = Util.join("/", name + "/axis/fact");
            JsonNode fact = dataDefs.at(path);
            String memberYml = "[{\"member\": {\"name\": \"fact\"}}]";
            JsonNode member = mapper.readTree(memberYml);
            ((ObjectNode) fact).replace("members", member);
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
    public void setDefaultSteps(final JsonNode defs) {
        JsonNode taskGroups = nzHelper.getTaskGroups(defs);
        Map<String, JsonNode> tasks = nzHelper.getTasks(taskGroups);
        for (JsonNode task : tasks.values()) {
            if (!nzHelper.isStepsDefined(task)) {
                nzHelper.setDefaultSteps(task);
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
