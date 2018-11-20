package org.codetab.scoopi.defs.yml;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codetab.scoopi.model.Filter;
import org.codetab.scoopi.model.Query;

public class ItemAttribute {

    private String key; // [dataDefName-itemName]
    private Optional<Query> query;
    private Range<Integer> indexRange;
    private Optional<List<String>> prefix;
    private Optional<List<String>> breakAfter;
    private Optional<List<Filter>> filter;
    private Optional<String> linkGroup;

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public Optional<Query> getQuery() {
        return query;
    }

    public void setQuery(final Optional<Query> query) {
        this.query = query;
    }

    public Range<Integer> getIndexRange() {
        return indexRange;
    }

    public void setIndexRange(final Range<Integer> indexRange) {
        this.indexRange = indexRange;
    }

    public Optional<List<String>> getPrefix() {
        return prefix;
    }

    public void setPrefix(final Optional<List<String>> prefix) {
        this.prefix = prefix;
    }

    public Optional<List<String>> getBreakAfter() {
        return breakAfter;
    }

    public void setBreakAfter(final Optional<List<String>> breakAfter) {
        this.breakAfter = breakAfter;
    }

    public Optional<List<Filter>> getFilter() {
        return filter;
    }

    public void setFilter(Optional<List<Filter>> filter) {
        this.filter = filter;
    }

    public Optional<String> getLinkGroup() {
        return linkGroup;
    }

    public void setLinkGroup(final Optional<String> linkGroup) {
        this.linkGroup = linkGroup;
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
                + ", breakAfter=" + breakAfter + ", filter=" + filter + "]";
    }

}
