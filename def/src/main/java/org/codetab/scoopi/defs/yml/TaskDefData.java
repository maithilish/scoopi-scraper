package org.codetab.scoopi.defs.yml;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.codetab.scoopi.model.StepInfo;

public class TaskDefData implements Serializable {

    private static final long serialVersionUID = -65043580910323065L;

    private Map<String, List<String>> taskNamesMap;
    private Map<String, String> stepsNameMap;
    private Map<String, Map<String, StepInfo>> stepsMap;
    private Map<String, Map<String, StepInfo>> nextStepsMap;

    private String defsJson;

    public Map<String, List<String>> getTaskNamesMap() {
        return taskNamesMap;
    }

    public void setTaskNamesMap(final Map<String, List<String>> taskNamesMap) {
        this.taskNamesMap = taskNamesMap;
    }

    public Map<String, String> getStepsNameMap() {
        return stepsNameMap;
    }

    public void setStepsNameMap(final Map<String, String> stepsNameMap) {
        this.stepsNameMap = stepsNameMap;
    }

    public Map<String, Map<String, StepInfo>> getStepsMap() {
        return stepsMap;
    }

    public void setStepsMap(final Map<String, Map<String, StepInfo>> stepsMap) {
        this.stepsMap = stepsMap;
    }

    public Map<String, Map<String, StepInfo>> getNextStepsMap() {
        return nextStepsMap;
    }

    public void setNextStepsMap(
            final Map<String, Map<String, StepInfo>> nextStepsMap) {
        this.nextStepsMap = nextStepsMap;
    }

    public String getDefsJson() {
        return defsJson;
    }

    public void setDefsJson(final String defsJson) {
        this.defsJson = defsJson;
    }

}
