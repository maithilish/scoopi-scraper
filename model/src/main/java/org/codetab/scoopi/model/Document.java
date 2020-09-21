
package org.codetab.scoopi.model;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.codetab.scoopi.util.LzCompressUtil;

public class Document implements Serializable {

    private static final long serialVersionUID = 8309456077125211948L;

    private Long id;
    private String name;
    private Date fromDate;
    private String url;
    private Fingerprint locatorId;
    private Object documentObject;
    private boolean compressed = false;

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

    public void compress() {
        if (!compressed) {
            documentObject = LzCompressUtil.compress((byte[]) documentObject);
            compressed = true;
        }
    }

    public void decompress() {
        if (compressed) {
            documentObject = LzCompressUtil.decompress((byte[]) documentObject);
            compressed = false;
        }
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

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(final Date fromDate) {
        this.fromDate = fromDate;
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

    public Fingerprint getLocatorId() {
        return locatorId;
    }

    public void setLocatorId(final Fingerprint locatorId) {
        this.locatorId = locatorId;
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
        String[] excludes = {"id"};
        return EqualsBuilder.reflectionEquals(this, obj, excludes);
    }

    @Override
    public int hashCode() {
        String[] excludes = {"id"};
        return HashCodeBuilder.reflectionHashCode(this, excludes);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId()).append("name", getName())
                .append("url", url).toString();
    }

}
