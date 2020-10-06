package org.codetab.scoopi.model;

import static org.codetab.scoopi.util.Util.dashit;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Plugin implements Serializable {

    private static final long serialVersionUID = 2968170159654889364L;

    private final String name;
    private final String className;
    private final String taskGroup;
    private final String taskName;
    private final String stepName;
    private final String defJson;
    private final Object def;

    private final transient Map<Object, Object> cache;

    public Plugin(final String name, final String className,
            final String taskGroup, final String taskName,
            final String stepName, final String defJson, final Object def) {
        this.name = name;
        this.className = className;
        this.taskGroup = taskGroup;
        this.taskName = taskName;
        this.stepName = stepName;
        this.defJson = defJson;
        this.def = def;
        cache = new ConcurrentHashMap<>();
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
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

    public void put(final Object key, final Object value) {
        cache.put(key, value);
    }

    public Object get(final Object key) {
        return cache.get(key);
    }

    @Override
    public String toString() {
        return "Plugin: " + dashit(taskGroup, taskName, stepName, name);
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
