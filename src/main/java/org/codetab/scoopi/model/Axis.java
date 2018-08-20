package org.codetab.scoopi.model;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Data is composed members and each member defines a data point through a set
 * of axis.
 * <p>
 * The member defined in datadef are used to create initial set of Axis and
 * whose Cartesian set is assigned to Member. In other words, member defined in
 * definition files are converted to set of Axis which is assigned to model
 * Member.
 * </p>
 * <p>
 * Fields such as breakAfter, indexRange etc., defined in member definition are
 * required only to parse data and hence, they don't find place in Axis.
 * </p>
 * @author maithilish
 *
 */
public final class Axis implements Comparable<Axis>, Serializable {

    private static final long serialVersionUID = 1L;

    private final AxisName name;
    private final String memberName;
    private String value;
    private String match;
    private Integer index;
    private Integer order;

    public Axis(final AxisName name, final String memberName) {
        this.name = name;
        this.memberName = memberName;
    }

    public AxisName getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getMemberName() {
        return memberName;
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

    /**
     * Deep Copy
     * @return deep copy of Axis
     */
    public Axis copy() {
        Axis copy = new Axis(this.name, this.memberName);
        copy.match = match;
        copy.value = value;
        copy.index = index;
        copy.order = order;
        return copy;
    }

    @Override
    public int compareTo(final Axis other) {
        // compare Enum AxisName
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
                .append("name", name).append("memberName", memberName)
                .append("value", value).append("match", match)
                .append("index", index).append("order", order).toString();
        return String.join("", System.lineSeparator(), "  ", str);
    }
}
