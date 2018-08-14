package org.codetab.scoopi.defs.yml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.defs.ITaskProvider;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.util.Util;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

@Singleton
public class TaskProvider implements ITaskProvider {

    @Inject
    private ObjectFactory factory;

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
        String path = "/" + taskGroup;
        return Lists.newArrayList(defs.at(path).fieldNames());
    }

    /*
     * Steps routines
     */
    @Override
    public StepInfo getNextStep(final String taskGroup, final String taskName,
            final String currentStepName) throws DefNotFoundException {
        String stepsName = getStepsName(taskGroup, taskName);
        ArrayList<Entry<String, JsonNode>> entries =
                getSteps(taskGroup, taskName, stepsName);
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
                Util.join("next step for taskGroup: ", taskGroup, " taskName: ",
                        taskName, " stepName: ", currentStepName));
    }

    @Override
    public String getStepsName(final String taskGroup, final String taskName)
            throws DefNotFoundException {
        String path = String.join("/", "", taskGroup, taskName, "steps");
        JsonNode steps = defs.at(path);
        Iterator<String> it = steps.fieldNames();
        if (it.hasNext()) {
            return it.next();
        } else {
            throw new DefNotFoundException(Util.join("step name at ", path));
        }
    }

    @Override
    public ArrayList<Entry<String, JsonNode>> getSteps(final String taskGroup,
            final String taskName, final String stepName)
            throws DefNotFoundException {
        String path =
                String.join("/", "", taskGroup, taskName, "steps", stepName);
        JsonNode steps = defs.at(path);
        if (steps.isMissingNode()) {
            throw new DefNotFoundException(Util.join("steps at ", path));
        } else {
            return Lists.newArrayList(steps.fields());
        }
    }

    @Override
    public String getFieldValue(final String taskGroup, final String taskName,
            final String fieldName) throws DefNotFoundException {
        String path = String.join("/", "", taskGroup, taskName, fieldName);
        JsonNode live = defs.at(path);
        if (live.isMissingNode()) {
            throw new DefNotFoundException(path);
        } else {
            return live.asText();
        }
    }
}
