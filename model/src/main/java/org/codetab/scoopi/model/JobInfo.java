package org.codetab.scoopi.model;

import static org.codetab.scoopi.util.Util.dashit;

import java.io.Serializable;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class JobInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private final String locator;
    private final String group;
    private final String task;
    private final String steps;
    private final String dataDef;
    private final String label;
    private final Marker jobMarker;
    private final Marker jobAbortedMarker;

    JobInfo(final String locator, final String group, final String task,
            final String steps, final String dataDef) {

        // Validate.notNull(id, "id must not be null");
        Validate.notNull(locator, "locator name must not be null");
        Validate.notNull(group, "group must not be null");
        Validate.notNull(task, "task must not be null");
        Validate.notNull(dataDef, "dataDef must not be null");

        // this.id = id;
        this.locator = locator;
        this.group = group;
        this.task = task;
        this.steps = steps;
        this.dataDef = dataDef;
        this.label = String.join(":", locator, task, dataDef);

        final String markerName = dashit("task", locator, group, task);
        jobMarker = MarkerManager.getMarker(markerName);
        jobAbortedMarker = MarkerManager.getMarker("task-aborted");
        jobAbortedMarker.addParents(jobMarker);
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
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

    public String getSteps() {
        return steps;
    }

    public String getDataDef() {
        return dataDef;
    }

    public String getLabel() {
        return label;
    }

    public Marker getJobMarker() {
        return jobMarker;
    }

    public Marker getJobAbortedMarker() {
        return jobAbortedMarker;
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
                + ", task=" + task + ", steps=" + steps + ", dataDef=" + dataDef
                + "]";
    }
}
