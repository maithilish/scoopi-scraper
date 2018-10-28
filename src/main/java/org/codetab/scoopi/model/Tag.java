package org.codetab.scoopi.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Tag implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> tags = new HashMap<>();

    public void add(final String key, final Object value) {
        tags.put(key, value);
    }

    public Object getValue(final String key) {
        return tags.get(key);
    }

    public Collection<Object> getValues() {
        return tags.values();
    }

    public void copyTags(final Tag toTag) {
        for (String key : tags.keySet()) {
            toTag.add(key, tags.get(key));
        }
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
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("tags", tags).toString();
    }
}
