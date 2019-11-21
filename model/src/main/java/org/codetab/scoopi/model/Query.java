package org.codetab.scoopi.model;

import static java.util.Objects.isNull;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Query {

    private Map<String, String> queries;

    Query() {
        queries = new HashMap<>();
    }

    public void setQuery(final String type, final String query) {
        queries.put(type, query);
    }

    public String getQuery(final String type) {
        String query = queries.get(type);
        if (isNull(query)) {
            throw new NoSuchElementException();
        }
        return query;
    }

    public Query copy() {
        Query copy = new Query();
        for (String key : queries.keySet()) {
            copy.setQuery(key, queries.get(key));
        }
        return copy;
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
        return "Query [queries=" + queries + "]";
    }

}
