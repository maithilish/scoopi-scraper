
package org.codetab.scoopi.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Locator implements Serializable {

    private final static long serialVersionUID = 1L;
    private Long id;
    private String name;
    private String group;
    private String url;
    private List<Document> documents;

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
    public void setId(Long value) {
        this.id = value;
    }

    /**
     * Gets the value of the documents property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the documents property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getDocuments().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Document
     * }
     *
     *
     */
    public List<Document> getDocuments() {
        if (documents == null) {
            documents = new ArrayList<Document>();
        }
        return this.documents;
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
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the group property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getGroup() {
        return group;
    }

    /**
     * Sets the value of the group property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setGroup(String value) {
        this.group = value;
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
    public void setUrl(String value) {
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
                .append("group", group).append("url", url)
                .append("documents", documents).toString();
    }

}
