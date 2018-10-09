package org.codetab.scoopi.defs.yml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.StringJoiner;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.defs.ITaskDefs;
import org.codetab.scoopi.defs.yml.helper.StepDefsHelper;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.StepInfo;

import com.fasterxml.jackson.databind.JsonNode;

@Singleton
public class TaskDefs implements ITaskDefs {

    @Inject
    private ObjectFactory factory;
    @Inject
    private StepDefsHelper stepDefsHelper;

    private JsonNode defs;

    /**
     * on first invoke sets defs and subsequent invocations do nothing.
     * @param defs
     */
    public void init(final JsonNode taskDefs) {
        if (this.defs == null) {
            this.defs = taskDefs;
        }
    }

    /*
     * Task routines
     */

    @Override
    public List<String> getTaskNames(final String taskGroup) {
        List<String> taskNames = new ArrayList<>();
        String path = "/" + taskGroup;
        Iterator<Entry<String, JsonNode>> it = defs.at(path).fields();
        while (it.hasNext()) {
            Entry<String, JsonNode> entry = it.next();
            if (entry.getValue().isObject()) {
                taskNames.add(entry.getKey());
            }
        }
        return taskNames;
    }

    @Override
    public Optional<String> getFirstTaskName(final String taskGroup) {
        List<String> taskNames = getTaskNames(taskGroup);
        if (taskNames.size() > 0) {
            return Optional.of(taskNames.get(0));
        } else {
            return Optional.empty();
        }
    }

    /*
     * Steps routines
     */
    @Override
    public StepInfo getNextStep(final String taskGroup, final String taskName,
            final String currentStepName) throws DefNotFoundException {
        String stepsName =
                stepDefsHelper.getStepsName(defs, taskGroup, taskName);
        ArrayList<Entry<String, JsonNode>> entries = stepDefsHelper
                .getStepsList(defs, taskGroup, taskName, stepsName);
        for (Entry<String, JsonNode> entry : entries) {
            String nextStepName = entry.getKey();
            JsonNode step = entry.getValue();
            if (step.findValue("previous").asText().equals(currentStepName)) {
                String previousStepName = currentStepName;
                String nextStepNameOfNextStep = step.findValue("next").asText();
                String className = step.findValue("class").asText();
                StepInfo nextStep = factory.createStepInfo(nextStepName,
                        previousStepName, nextStepNameOfNextStep, className);
                return nextStep;
            }
        }
        throw new DefNotFoundException(
                String.join(" ", "next step for taskGroup:", taskGroup,
                        "taskName:", taskName, "stepName:", currentStepName));
    }

    @Override
    public String getStepsName(final String taskGroup, final String taskName)
            throws DefNotFoundException {
        return stepDefsHelper.getStepsName(defs, taskGroup, taskName);
    }

    @Override
    public ArrayList<Entry<String, JsonNode>> getStepsList(
            final String taskGroup, final String taskName,
            final String stepsName) throws DefNotFoundException {
        return stepDefsHelper.getStepsList(defs, taskGroup, taskName,
                stepsName);
    }

    @Override
    public String getFieldValue(final String taskGroup, final String taskName,
            final String... fieldNames) throws DefNotFoundException {
        StringJoiner joiner =
                new StringJoiner("/").add("").add(taskGroup).add(taskName);
        for (String field : fieldNames) {
            joiner.add(field);
        }
        String path = joiner.toString();
        JsonNode live = defs.at(path);
        if (live.isMissingNode()) {
            throw new DefNotFoundException(path);
        } else {
            return live.asText();
        }
    }

    @Override
    public String getLive(final String taskGroup) throws DefNotFoundException {
        String path = String.join("/", "", taskGroup, "live");
        JsonNode live = defs.at(path);
        if (live.isMissingNode()) {
            throw new DefNotFoundException(path);
        } else {
            return live.asText();
        }
    }
}
