package org.codetab.scoopi.defs.yml.helper;

import java.util.ArrayList;
import java.util.Map.Entry;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.InvalidDefException;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

public class StepDefsHelper {

    /**
     * Get steps object of a task
     * @param taskGroupDef
     * @param taskGroup
     * @param taskName
     * @return
     * @throws DefNotFoundException
     * @throws InvalidDefException
     */
    public Entry<String, JsonNode> getSteps(final JsonNode taskGroupDef,
            final String taskGroup, final String taskName)
            throws DefNotFoundException, InvalidDefException {
        String path = String.join("/", "", taskGroup, taskName, "steps");
        JsonNode jSteps = taskGroupDef.at(path);
        if (jSteps.isMissingNode()) {
            throw new DefNotFoundException(
                    String.join(" ", "steps at path:", path));
        }
        ArrayList<Entry<String, JsonNode>> jStepsList =
                Lists.newArrayList(jSteps.fields());
        if (jStepsList.size() != 1) {
            throw new IllegalStateException(
                    String.join(" ", "one steps object expected at path:", path,
                            "but found: ", String.valueOf(jStepsList.size())));
        }
        return jStepsList.get(0);
    }

    /**
     * Get steps name of a task
     * @param taskGroupDefs
     * @param taskGroup
     * @param taskName
     * @return
     * @throws DefNotFoundException
     */
    public String getStepsName(final JsonNode taskGroupDefs,
            final String taskGroup, final String taskName)
            throws DefNotFoundException {
        String path = String.join("/", "", taskGroup, taskName, "steps");
        JsonNode jSteps = taskGroupDefs.at(path);
        ArrayList<String> jStepsList = Lists.newArrayList(jSteps.fieldNames());
        if (jStepsList.size() != 1) {
            throw new IllegalStateException(
                    String.join(" ", "one steps object expected at path:", path,
                            "but found: ", String.valueOf(jStepsList.size())));
        }
        return jStepsList.get(0);
    }

    /**
     * Get list of steps of a steps defined for a task.
     * @param taskGroupDefs
     * @param taskGroup
     * @param taskName
     * @param stepsName
     * @return
     * @throws DefNotFoundException
     */
    public ArrayList<Entry<String, JsonNode>> getStepsList(
            final JsonNode taskGroupDefs, final String taskGroup,
            final String taskName, final String stepsName)
            throws DefNotFoundException {
        String path =
                String.join("/", "", taskGroup, taskName, "steps", stepsName);
        JsonNode steps = taskGroupDefs.at(path);
        if (steps.isMissingNode()) {
            throw new DefNotFoundException(
                    String.join(" ", "steps at path:", path));
        } else {
            return Lists.newArrayList(steps.fields());
        }
    }

}
