package org.codetab.scoopi.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public final class Item extends DataComponent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String group;
    private Data parent;
    private Set<Axis> axes = new HashSet<Axis>();

    Item() {
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(final String group) {
        this.group = group;
    }

    public Data getParent() {
        return parent;
    }

    public void setParent(final Data parent) {
        this.parent = parent;
    }

    public Set<Axis> getAxes() {
        return axes;
    }

    public void setAxes(final Set<Axis> axes) {
        this.axes = axes;
    }

    public Axis getAxis(final AxisName axisName) {
        return axes.stream().filter(a -> a.getName().equals(axisName))
                .findFirst().get();
    }

    public Map<String, Axis> getAxisMap() {
        Map<String, Axis> axisMap = new HashMap<>();
        axes.stream().forEach(a -> axisMap.put(a.getName().toString(), a));
        return axisMap;
    }

    public void addAxis(final Axis axis) {
        axes.add(axis);
    }

    public String getValue(final AxisName axisName) {
        return getAxis(axisName).getValue();
    }

    public void setValue(final AxisName axisName, final String value) {
        getAxis(axisName).setValue(value);
    }

    /**
     * Deep Copy
     * @return deep copy of Item
     */
    public Item copy() {
        Item item = new Item();
        item.id = id;
        item.name = name;
        item.group = group;
        for (Axis axis : axes) {
            item.addAxis(axis.copy());
        }
        return item;
    }

    public StringBuilder traceItem() {
        String line = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append("Item=[name=");
        sb.append(getName());
        sb.append(",group=");
        sb.append(getGroup());
        sb.append("]");
        sb.append(line);
        axes.stream().forEach(sb::append);
        return sb;
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
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("axes", axes).toString();
    }
}
