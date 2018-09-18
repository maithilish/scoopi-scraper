package org.codetab.scoopi.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Plugin {

    private final String taskGroup;
    private final String taskName;
    private final String stepName;
    private final String defJson;
    private final Object def;

    public Plugin(final String taskGroup, final String taskName,
            final String stepName, final String defJson, final Object def) {
        this.taskGroup = taskGroup;
        this.taskName = taskName;
        this.stepName = stepName;
        this.defJson = defJson;
        this.def = def;
    }

    public String getTaskGroup() {
        return taskGroup;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getStepName() {
        return stepName;
    }

    public String getDefJson() {
        return defJson;
    }

    public Object getDef() {
        return def;
    }

    @Override
    public String toString() {
        return "Plugin [taskGroup=" + taskGroup + ", taskName=" + taskName
                + ", stepName=" + stepName + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
