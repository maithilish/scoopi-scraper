package org.codetab.scoopi.defs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.StepInfo;

import com.fasterxml.jackson.databind.JsonNode;

public interface ITaskProvider {

    /*
     * Tasks routine
     */
    List<String> getTaskNames(String group);

    String getDataDefName(String taskGroup, String taskName)
            throws DefNotFoundException;

    /*
     * steps routines
     */
    StepInfo getNextStep(String taskGroup, String taskName,
            String currentStepName) throws DefNotFoundException;

    String getStepsName(String taskGroup, String taskName)
            throws DefNotFoundException;

    ArrayList<Entry<String, JsonNode>> getSteps(String taskGroup,
            String taskName, String stepName) throws DefNotFoundException;
}
