
package org.codetab.scoopi.model;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Document implements Serializable {

    private final static long serialVersionUID = 1L;
    private Long id;
    private Object documentObject;
    private String name;
    // FIXME - dbfix, remove from and todate
    private Date fromDate;
    private Date toDate;
    private String url;

    Document() {
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
     * Gets the value of the documentObject property.
     *
     * @return possible object is {@link Object }
     *
     */
    public Object getDocumentObject() {
        return documentObject;
    }

    /**
     * Sets the value of the documentObject property.
     *
     * @param value
     *            allowed object is {@link Object }
     *
     */
    public void setDocumentObject(final Object value) {
        this.documentObject = value;
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

    /**
     * Gets the value of the url property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the value of the url property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setUrl(final String value) {
        this.url = value;
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
                .append("url", url).toString();
    }

}
