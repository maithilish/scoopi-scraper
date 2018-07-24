package org.codetab.scoopi.model;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;

import com.google.inject.assistedinject.Assisted;

public class JobInfo {

    private final long id;
    private final String locator;
    private final String group;
    private final String task;
    private final String dataDef;
    private final String label;

    @Inject
    public JobInfo(@Assisted("id") final long id,
            @Assisted("locator") final String locator,
            @Assisted("group") final String group,
            @Assisted("task") final String task,
            @Assisted("dataDef") final String dataDef) {

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
    public String toString() {
        return "JobInfo [id=" + id + ", locator=" + locator + ", group=" + group
                + ", task=" + task + ", dataDef=" + dataDef + "]";
    }
}
