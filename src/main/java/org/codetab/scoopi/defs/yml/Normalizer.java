package org.codetab.scoopi.defs.yml;

import static org.codetab.scoopi.util.Util.spaceit;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import org.codetab.scoopi.exception.DefNotFoundException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Methods to normalise user defs and to create effective defs.
 *
 * @author maithilish
 *
 */
public class Normalizer {

    @Inject
    private Normalizers normalizers;
    @Inject
    private TaskDefs taskDefs;
    @Inject
    private StepDefs stepDefs;

    /**
     * If index or indexRange fields are not defined, then adds index as one
     * @param defs
     * @throws IOException
     */
    public void addItemIndex(final JsonNode defs) throws IOException {
        JsonNode dataDefs = defs.at("/dataDefs");
        List<JsonNode> items = dataDefs.findValues("items");
        normalizers.addIndex(items);
        items = dataDefs.findValues("dims");
        normalizers.addIndex(items);
        items = dataDefs.findValues("fact");
        normalizers.addIndex(items);
    }

    /**
     * If order field is not defined, then add (zero based) order
     * @param defs
     * @throws IOException
     */
    public void addItemOrder(final JsonNode defs) throws IOException {
        JsonNode dataDefs = defs.at("/dataDefs");
        List<JsonNode> items = dataDefs.findValues("items");
        normalizers.addOrder(items);
        items = dataDefs.findValues("dims");
        normalizers.addOrder(items);
        items = dataDefs.findValues("fact");
        normalizers.addOrder(items);
    }

    /**
     * If fact dim is not defined, then adds default fact dim
     * @param defs
     * @throws IOException
     */
    public void addFactItem(final JsonNode defs) throws IOException {
        JsonNode dataDefs = defs.at("/dataDefs");
        Iterator<Entry<String, JsonNode>> entries = dataDefs.fields();
        while (entries.hasNext()) {
            Entry<String, JsonNode> entry = entries.next();
            normalizers.addFact(entry);
        }
    }

    /**
     * If steps is not defined for the task, then adds field - steps: default
     * @param defs
     */
    public void setDefaultSteps(final JsonNode defs,
            final String defaultStepsName) {
        JsonNode taskGroupsDef = defs.at("/taskGroups");
        Map<String, JsonNode> taskMap = taskDefs.getAllTasks(taskGroupsDef);
        for (JsonNode task : taskMap.values()) {
            if (!stepDefs.isStepsDefined(task) && task.isObject()) {
                normalizers.setDefaultSteps(task, defaultStepsName);
            }
        }
    }

    /**
     * For named steps, replace steps name with actual steps object from top
     * level. If steps is object, then it means it is overridden steps. For it,
     * get steps object from top level and merge or replace its step[s] with
     * overridden step[s] and finally replace the task steps object with the new
     * merged steps object
     *
     * @param defs
     * @throws DefNotFoundException
     */
    public void expandTaskSteps(final JsonNode defs)
            throws DefNotFoundException {

        JsonNode topStepsDef = defs.at("/steps");
        Map<String, JsonNode> topStepsMap =
                stepDefs.getTopStepsMap(topStepsDef);

        JsonNode taskGroupsDef = defs.at("/taskGroups");
        Map<String, JsonNode> tasksMap = taskDefs.getAllTasks(taskGroupsDef);
        Map<String, JsonNode> stepsMap = stepDefs.getStepsNodeMap(tasksMap);

        for (String key : stepsMap.keySet()) {
            JsonNode jTask = tasksMap.get(key);
            JsonNode jSteps = stepsMap.get(key);
            if (jSteps.isMissingNode()) {
                String message = spaceit("steps not defined for task:", key);
                throw new NoSuchElementException(message);
            }
            // named steps
            if (jSteps.isTextual()) {
                String stepsName = jTask.path("steps").asText();
                JsonNode expandedStepsCopy =
                        stepDefs.getTopSteps(topStepsMap, stepsName).deepCopy();
                normalizers.expandTaskSteps(jTask, expandedStepsCopy,
                        stepsName);
            }
            // steps with overridden step[s]
            // replace step
            if (jSteps.isObject()) {
                String stepsName = stepDefs.getStepsName(jSteps);
                JsonNode expandedStepsCopy =
                        stepDefs.getTopSteps(topStepsMap, stepsName).deepCopy();
                Map<String, JsonNode> overriddenSteps =
                        stepDefs.getStepNodes(jSteps, stepsName);
                // replace or insert overridden steps to expanded steps copy
                for (String stepName : overriddenSteps.keySet()) {
                    JsonNode overriddenStep = overriddenSteps.get(stepName);
                    if (stepDefs.isStepDefined(expandedStepsCopy, stepName)) {
                        normalizers.replaceStep(expandedStepsCopy,
                                overriddenStep.deepCopy(), stepName);
                    } else {
                        normalizers.insertStep(expandedStepsCopy,
                                overriddenStep.deepCopy(), stepName);
                    }
                }
                // replace with expanded and overridden steps
                normalizers.replaceSteps(jSteps, expandedStepsCopy, stepsName);
            }
        }
    }

    public void expandSteps(final JsonNode defs) throws DefNotFoundException {
        JsonNode topStepsDef = defs.at("/steps");
        Map<String, JsonNode> topStepsMap =
                stepDefs.getTopStepsMap(topStepsDef);

        for (String stepsName : topStepsMap.keySet()) {
            JsonNode steps = topStepsMap.get(stepsName);
            JsonNode extSteps = normalizers.expandSteps(topStepsMap, steps);
            ((ObjectNode) topStepsDef).set(stepsName, extSteps);
        }

        // topStepsMap = stepDefs.getTopStepsMap(topStepsDef);
        // for (String stepsName : topStepsMap.keySet()) {
        // System.out.println(stepsName + " >> ");
        // System.out.println(yamls.pretty(topStepsMap.get(stepsName)));
        // }
    }

}
