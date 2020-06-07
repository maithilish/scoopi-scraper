package org.codetab.scoopi.defs.yml;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codetab.scoopi.model.Filter;
import org.codetab.scoopi.model.Query;

public class ItemAttribute implements Serializable {

    private static final long serialVersionUID = 8028091758239615034L;

    private final String key; // [dataDefName-itemName]
    private final Query query;
    private final Range<Integer> indexRange;
    private final List<String> prefix;
    private final List<String> breakAfter;
    private final List<Filter> filter;
    private final String linkGroup;
    private final List<String> linkBreakOn;

    private ItemAttribute(final Builder builder) {
        this.key = builder.aKey;
        this.query = builder.aQuery;
        this.indexRange = builder.aIndexRange;
        this.prefix = builder.aPrefix;
        this.breakAfter = builder.aBreakAfter;
        this.filter = builder.aFilter;
        this.linkGroup = builder.aLinkGroup;
        this.linkBreakOn = builder.aLinkBreakOn;
    }

    public String getKey() {
        return key;
    }

    public Query getQuery() {
        return query;
    }

    public Range<Integer> getIndexRange() {
        return indexRange;
    }

    public List<String> getPrefix() {
        return prefix;
    }

    public List<String> getBreakAfter() {
        return breakAfter;
    }

    public List<Filter> getFilter() {
        return filter;
    }

    public String getLinkGroup() {
        return linkGroup;
    }

    public List<String> getLinkBreakOn() {
        return linkBreakOn;
    }

    public static class Builder {

        // fields are prefixed with a to avoid cs hides field message
        private String aKey;
        private Query aQuery;
        private Range<Integer> aIndexRange;
        private List<String> aPrefix;
        private List<String> aBreakAfter;
        private List<Filter> aFilter;
        private String aLinkGroup;
        private List<String> aLinkBreakOn;

        public Builder setKey(final String key) {
            this.aKey = key;
            return this;
        }

        public Builder setQuery(final Query query) {
            this.aQuery = query;
            return this;
        }

        public Builder setIndexRange(final Range<Integer> indexRange) {
            this.aIndexRange = indexRange;
            return this;
        }

        public Builder setPrefix(final List<String> prefix) {
            this.aPrefix = prefix;
            return this;
        }

        public Builder setBreakAfter(final List<String> breakAfter) {
            this.aBreakAfter = breakAfter;
            return this;
        }

        public Builder setFilter(final List<Filter> filter) {
            this.aFilter = filter;
            return this;
        }

        public Builder setLinkGroup(final String linkGroup) {
            this.aLinkGroup = linkGroup;
            return this;
        }

        public Builder setLinkBreakOn(final List<String> linkBreakOn) {
            this.aLinkBreakOn = linkBreakOn;
            return this;
        }

        ItemAttribute build() {
            return new ItemAttribute(this);
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
        return "ItemAttribute [key=" + key + ", query=" + query
                + ", indexRange=" + indexRange + ", prefix=" + prefix
                + ", breakAfter=" + breakAfter + ", filter=" + filter
                + ", linkGroup=" + linkGroup + ", linkBreakOn=" + linkBreakOn
                + "]";
    }
}
