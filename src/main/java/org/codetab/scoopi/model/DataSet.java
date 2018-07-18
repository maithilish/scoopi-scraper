package org.codetab.scoopi.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DataSet {

    private long id;
    private String name;
    private String group;
    private String col;
    private String row;
    private String fact;

    public DataSet(final String name, final String group, final String col,
            final String row, final String fact) {
        this.name = name;
        this.group = group;
        this.col = col;
        this.row = row;
        this.fact = fact;
    }

    /**
     * @param id
     */
    public void setId(final long id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the group
     */
    public String getGroup() {
        return group;
    }

    /**
     * @return the col
     */
    public String getCol() {
        return col;
    }

    /**
     * @return the row
     */
    public String getRow() {
        return row;
    }

    /**
     * @return the fact
     */
    public String getFact() {
        return fact;
    }

    @Override
    public boolean equals(final Object obj) {
        String[] excludes =
                {"id", "dnDetachedState", "dnFlags", "dnStateManager"};
        return EqualsBuilder.reflectionEquals(this, obj, excludes);
    }

    @Override
    public int hashCode() {
        String[] excludes =
                {"id", "dnDetachedState", "dnFlags", "dnStateManager"};
        return HashCodeBuilder.reflectionHashCode(this, excludes);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId()).append("name", name)
                .append("group", group).append("col", col).append("row", row)
                .append("fact", fact).toString();
    }

}
