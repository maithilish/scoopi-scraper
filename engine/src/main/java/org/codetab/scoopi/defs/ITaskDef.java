package org.codetab.scoopi.defs;

import java.util.List;
import java.util.Optional;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.StepInfo;

public interface ITaskDef {

    void init(Object taskDefs) throws DefNotFoundException;

    /*
     * Tasks routine
     */
    List<String> getTaskNames(String group);

    Optional<String> getFirstTaskName(String taskGroup);

    String getFieldValue(String taskGroup, String taskName,
            String... fieldNames) throws DefNotFoundException;

    String getLive(String taskGroup) throws DefNotFoundException;

    /*
     * steps routines
     */

    String getStepsName(String taskGroup, String taskName)
            throws DefNotFoundException;

    StepInfo getNextStep(String taskGroup, String taskName, String stepName)
            throws DefNotFoundException;

}
