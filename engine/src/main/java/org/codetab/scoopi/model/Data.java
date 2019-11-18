package org.codetab.scoopi.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public final class Data extends DataComponent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String dataDef;
    private Long dataDefId;
    private Long documentId;
    private Tag tag = new Tag();
    private List<DataComponent> items = new ArrayList<>();

    Data() {
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

    public String getDataDef() {
        return dataDef;
    }

    public void setDataDef(final String dataDef) {
        this.dataDef = dataDef;
    }

    public Long getDataDefId() {
        return dataDefId;
    }

    public void setDataDefId(final Long dataDefId) {
        this.dataDefId = dataDefId;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(final Long documentId) {
        this.documentId = documentId;
    }

    public List<Item> getItems() {
        DataIterator it = iterator();
        List<Item> list = new ArrayList<>();
        while (it.hasNext()) {
            DataComponent dc = it.next();
            if (dc instanceof Item) {
                list.add((Item) dc);
            }
        }
        return list;
    }

    public DataIterator iterator() {
        return new DataIterator(items.iterator());
    }

    public void setItems(final List<DataComponent> items) {
        this.items = items;
    }

    public void addItem(final DataComponent item) {
        items.add(item);
    }

    public void removeItem(final DataComponent item) {
        items.remove(item);
    }

    public void addTag(final String key, final Object value) {
        tag.add(key, value);
    }

    public void copyTags(final Data toData) {
        tag.copyTags(toData.tag);
    }

    public Object getTagValue(final String key) {
        return tag.getValue(key);
    }

    /**
     * Deep Copy
     * @return deep copy of Data
     */
    public Data copy() {
        Data copy = new Data();
        copy.id = id;
        copy.name = name;
        copy.dataDef = dataDef;
        copy.dataDefId = dataDefId;
        copy.documentId = documentId;
        for (DataComponent item : items) {
            copy.addItem(item.copy());
        }
        return copy;
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
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("name", name).append("dataDef", dataDef)
                .append("dataDefId", dataDefId).append("documentId", documentId)
                .toString();
    }

    public String toTraceString() {
        String line = System.lineSeparator();
        String str = toString();
        return String.join(line, str, "items:", items.toString(), line);
    }

    public String toStringIds() {
        return "Data [id=" + id + ", dataDefId=" + dataDefId + ", documentId="
                + documentId + "]";
    }
}
