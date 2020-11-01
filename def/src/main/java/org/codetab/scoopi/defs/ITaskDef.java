package org.codetab.scoopi.defs;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.StepInfo;

public interface ITaskDef {

    // Tasks routine
    List<String> getTaskNames(String group);

    Optional<String> getFirstTaskName(String taskGroup);

    String getFieldValue(String taskGroup, String taskName,
            String... fieldNames) throws DefNotFoundException, IOException;

    String getLive(String taskGroup) throws DefNotFoundException, IOException;

    // steps routines
    String getStepsName(String taskGroup, String taskName)
            throws DefNotFoundException;

    StepInfo getNextStep(String taskGroup, String taskName, String stepName)
            throws DefNotFoundException;

}
