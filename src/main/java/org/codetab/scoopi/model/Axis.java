package org.codetab.scoopi.model;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public final class Axis implements Comparable<Axis>, Serializable {

    private static final long serialVersionUID = 1L;

    private AxisName name;
    private String value;
    private String match;
    private Integer index;
    private Integer order;

    public Axis() {
    }

    public AxisName getName() {
        return name;
    }

    public void setName(final AxisName name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(final String match) {
        this.match = match;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(final Integer index) {
        this.index = index;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(final Integer order) {
        this.order = order;
    }

    @Override
    public int compareTo(final Axis other) {
        // String name is converted to Enum AxisName and compared
        AxisName a1 = name;
        AxisName a2 = other.name;
        return a1.compareTo(a2);
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
        String str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("name", name).append("value", value)
                .append("match", match).append("index", index)
                .append("order", order).toString();
        return String.join("", System.lineSeparator(), "  ", str);
    }
}
