
package org.codetab.scoopi.model;

import static java.util.Objects.isNull;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DataDef implements Serializable {

    private static final long serialVersionUID = 8992597470530894703L;

    private Long id;
    private String name;
    private Date fromDate;
    private Date toDate;
    private String defJson;
    private Object def;

    DataDef() {
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is {@link Long }
     *
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value
     *            allowed object is {@link Long }
     *
     */
    public void setId(final Long value) {
        this.id = value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setName(final String value) {
        this.name = value;
    }

    /**
     * Gets the value of the fromDate property.
     *
     * @return possible object is {@link String }
     *
     */
    public Date getFromDate() {
        return fromDate;
    }

    /**
     * Sets the value of the fromDate property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setFromDate(final Date value) {
        this.fromDate = value;
    }

    /**
     * Gets the value of the toDate property.
     *
     * @return possible object is {@link String }
     *
     */
    public Date getToDate() {
        return toDate;
    }

    /**
     * Sets the value of the toDate property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setToDate(final Date value) {
        this.toDate = value;
    }

    public String getDefJson() {
        return defJson;
    }

    public void setDefJson(final String defJson) {
        this.defJson = defJson;
    }

    public Object getDef() {
        return def;
    }

    public void setDef(final Object def) {
        this.def = def;
    }

    public boolean equalsForDef(final Object obj) {
        if (isNull(obj)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        DataDef rhs = (DataDef) obj;
        return new EqualsBuilder().append(name, rhs.getName())
                .append(defJson, rhs.getDefJson()).isEquals();
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
                .append("id", getId()).append("name", getName())
                .append("fromDate", fromDate).append("toDate", toDate)
                .toString();
    }

}
