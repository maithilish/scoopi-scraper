package org.codetab.scoopi.defs.yml;

import static org.codetab.scoopi.util.Util.dashit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.codetab.scoopi.model.Filter;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Query;

import com.fasterxml.jackson.databind.JsonNode;

class ItemAttributes {

    @Inject
    private Jacksons jacksons;
    @Inject
    private ObjectFactory objectFactory;
    @Inject
    private IndexRangeFactory indexRangeFactory;

    public Map<String, Optional<Query>> getItemQueryMap(final JsonNode defs,
            final Map<String, JsonNode> itemMap) {

        Map<String, Optional<Query>> map = new HashMap<>();

        for (String key : itemMap.keySet()) {
            JsonNode jItem = itemMap.get(key);
            String selector = jItem.path("selector").asText();
            String nameSelector = jItem.path("nameSelector").asText();
            String script = jItem.path("script").asText();

            Query query = objectFactory.createQuery();
            if (StringUtils.isNotBlank(selector)) {
                query.setQuery("selector", selector);
            }
            if (StringUtils.isNotBlank(nameSelector)) {
                query.setQuery("nameSelector", nameSelector);
            }
            if (StringUtils.isNotBlank(script)) {
                query.setQuery("script", script);
            }
            if (StringUtils.isAllBlank(selector, nameSelector, script)) {
                query = null;
            }
            map.put(key, Optional.ofNullable(query));
        }
        return map;
    }

    public Map<String, Range<Integer>> getIndexRangeMap(final JsonNode defs,
            final Map<String, JsonNode> itemMap) {
        // handle indexRange, index and breakAfter
        Map<String, Range<Integer>> map = new HashMap<>();

        for (String key : itemMap.keySet()) {
            JsonNode jItem = itemMap.get(key);

            Range<Integer> indexRange = Range.between(1, 1);

            String indexRangeStr = jItem.path("indexRange").asText();
            if (StringUtils.isNotBlank(indexRangeStr)) {
                indexRange = indexRangeFactory.createRange(indexRangeStr);
            } else {
                String indexStr = jItem.path("index").asText();
                JsonNode jBreakAfter = jItem.path("breakAfter");
                if (StringUtils.isNotBlank(indexStr)) {
                    if (jBreakAfter.isMissingNode()) {
                        // if index is 5 then 5-5
                        indexRange = indexRangeFactory
                                .createRange(dashit(indexStr, indexStr));
                    } else {
                        // if index is 5 and breakAfter defined then 5-
                        indexRange = indexRangeFactory
                                .createRange(dashit(indexStr, ""));
                    }
                }
            }
            map.put(key, indexRange);
        }
        return map;
    }

    public Map<String, Optional<List<String>>> getBreakAfterMap(
            final JsonNode defs, final Map<String, JsonNode> itemMap) {

        Map<String, Optional<List<String>>> map = new HashMap<>();

        for (String key : itemMap.keySet()) {
            JsonNode jItem = itemMap.get(key);
            Optional<List<String>> breakAfter = Optional.ofNullable(
                    jacksons.getArrayAsStrings(jItem, "breakAfter"));
            map.put(key, breakAfter);
        }
        return map;
    }

    public Map<String, Optional<List<String>>> getPrefixMap(final JsonNode defs,
            final Map<String, JsonNode> itemMap) {

        Map<String, Optional<List<String>>> map = new HashMap<>();

        for (String key : itemMap.keySet()) {
            JsonNode jItem = itemMap.get(key);
            Optional<List<String>> prefix = Optional
                    .ofNullable(jacksons.getArrayAsStrings(jItem, "prefix"));
            map.put(key, prefix);
        }
        return map;
    }

    public Map<String, Optional<List<Filter>>> getFilterMap(final JsonNode defs,
            final Map<String, JsonNode> itemMap) {
        Map<String, Optional<List<Filter>>> map = new HashMap<>();

        for (String key : itemMap.keySet()) {
            List<Filter> filters = new ArrayList<>();
            JsonNode jItem = itemMap.get(key);
            List<JsonNode> jFilters = jItem.at("/filters").findValues("filter");
            for (JsonNode jFilter : jFilters) {
                String type = jFilter.path("type").asText();
                String pattern = jFilter.path("pattern").asText();
                Filter filter = objectFactory.createFilter(type, pattern);
                filters.add(filter);
            }
            Optional<List<Filter>> optional = Optional.empty();
            if (!filters.isEmpty()) {
                optional = Optional.ofNullable(filters);
            }
            map.put(key, optional);
        }
        return map;
    }

    public Map<String, Optional<String>> getLinkGroupMap(final JsonNode defs,
            final Map<String, JsonNode> itemMap) {

        Map<String, Optional<String>> map = new HashMap<>();

        for (String key : itemMap.keySet()) {
            JsonNode jItem = itemMap.get(key);
            String linkGroup = jItem.path("linkGroup").asText();
            if (StringUtils.isBlank(linkGroup)) {
                map.put(key, Optional.empty());
            } else {
                map.put(key, Optional.ofNullable(linkGroup));
            }
        }
        return map;
    }

}
