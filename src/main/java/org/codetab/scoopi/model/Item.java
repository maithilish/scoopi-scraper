package org.codetab.scoopi.model;

import static java.util.Objects.isNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private List<Axis> axes;

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

    public List<Axis> getAxes() {
        return axes;
    }

    public void setAxes(final List<Axis> axes) {
        this.axes = axes;
    }

    public List<String> getItemNames() {
        // fact should be at the end
        List<String> names = axes.stream().map(Axis::getItemName)
                .collect(Collectors.toList());
        return names;
    }

    public Axis getAxis(final String itemName) {
        return axes.stream().filter(a -> a.getItemName().equals(itemName))
                .findFirst().get();
    }

    public Map<String, Axis> getAxisMap() {
        Map<String, Axis> axisMap = new HashMap<>();
        axes.stream().forEach(a -> axisMap.put(a.getAxisName().toString(), a));
        return axisMap;
    }

    public void addAxis(final Axis axis) {
        if (isNull(axes)) {
            axes = new ArrayList<>();
        }
        axes.add(axis);
    }

    public String getValue(final String itemName) {
        return getAxis(itemName).getValue();
    }

    public void setValue(final String itemName, final String value) {
        getAxis(itemName).setValue(value);
    }

    public Axis getFirstAxis() {
        return axes.get(0);
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
