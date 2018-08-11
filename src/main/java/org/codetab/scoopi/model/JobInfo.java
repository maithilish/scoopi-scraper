package org.codetab.scoopi.model;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class JobInfo {

    private final long id;
    private final String locator;
    private final String group;
    private final String task;
    private final String dataDef;
    private final String label;

    JobInfo(final long id, final String locator, final String group,
            final String task, final String dataDef) {

        Validate.notNull(id, "id must not be null");
        Validate.notNull(locator, "locator name must not be null");
        Validate.notNull(group, "group must not be null");
        Validate.notNull(task, "task must not be null");
        Validate.notNull(dataDef, "dataDef must not be null");

        this.id = id;
        this.locator = locator;
        this.group = group;
        this.task = task;
        this.dataDef = dataDef;
        this.label = String.join(":", locator, group, dataDef);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return locator;
    }

    public String getGroup() {
        return group;
    }

    public String getTask() {
        return task;
    }

    public String getDataDef() {
        return dataDef;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return "JobInfo [id=" + id + ", locator=" + locator + ", group=" + group
                + ", task=" + task + ", dataDef=" + dataDef + "]";
    }
}
