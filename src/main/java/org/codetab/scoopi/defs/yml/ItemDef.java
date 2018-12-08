package org.codetab.scoopi.defs.yml;

import static org.codetab.scoopi.util.Util.dashit;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.defs.IItemDef;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.Filter;
import org.codetab.scoopi.model.Query;

import com.fasterxml.jackson.databind.JsonNode;

@Singleton
public class ItemDef implements IItemDef {

    @Inject
    private ItemDefs itemDefs;

    private JsonNode defs;
    private Map<String, List<Axis>> itemAxisMap;
    private Map<String, List<Axis>> dimAxisMap;
    private Map<String, List<Axis>> factAxisMap;
    private Map<String, Query> queryMap;
    private Map<String, ItemAttribute> itemAttributeMap;
    private Map<String, Data> dataTemplateMap;

    @Override
    public void init(final Object dataDefNodes) throws DefNotFoundException {
        Validate.validState(dataDefNodes instanceof JsonNode,
                "dataDefNodes is not JsonNode");

        this.defs = (JsonNode) dataDefNodes;
        queryMap = itemDefs.getQueryMap(defs);
        itemAxisMap = itemDefs.getItemAxisMap(defs);
        dimAxisMap = itemDefs.getDimAxisMap(defs);
        factAxisMap = itemDefs.getFactAxisMap(defs);

        dataTemplateMap = itemDefs.generateDataTemplates(itemAxisMap,
                dimAxisMap, factAxisMap);

        Map<String, JsonNode> itemNodeMap = itemDefs.getItemNodeMap(defs);
        itemAttributeMap = itemDefs.getItemAttributeMap(defs, itemNodeMap);
    }

    @Override
    public Query getQuery(final String dataDef) {
        return queryMap.get(dataDef).copy();
    }

    @Override
    public Data getDataTemplate(final String dataDef) {
        return dataTemplateMap.get(dataDef).copy();
    }

    @Override
    public Optional<Query> getItemQuery(final String dataDef,
            final String itemName) {
        Optional<Query> query = Optional.empty();
        String key = dashit(dataDef, itemName);
        Optional<Query> opt = itemAttributeMap.get(key).getQuery();
        if (opt.isPresent()) {
            query = Optional.ofNullable(opt.get().copy());
        }
        return query;
    }

    @Override
    public Range<Integer> getIndexRange(final String dataDef,
            final String itemName) {
        String key = dashit(dataDef, itemName);
        // range is immutable so no copy
        // default indexRange 1-1 so not optional
        return itemAttributeMap.get(key).getIndexRange();
    }

    @Override
    public Optional<List<String>> getBreakAfter(final String dataDef,
            final String itemName) {
        String key = dashit(dataDef, itemName);
        Optional<List<String>> breakAfter =
                itemAttributeMap.get(key).getBreakAfter();
        if (breakAfter.isPresent()) {
            List<String> copy = Collections.unmodifiableList(breakAfter.get());
            breakAfter = Optional.ofNullable(copy);
        }
        return breakAfter;
    }

    @Override
    public Optional<List<Filter>> getFilter(final String dataDef,
            final String itemName) {
        String key = dashit(dataDef, itemName);
        Optional<List<Filter>> filter = itemAttributeMap.get(key).getFilter();
        if (filter.isPresent()) {
            List<Filter> copy = Collections.unmodifiableList(filter.get());
            filter = Optional.ofNullable(copy);
        }
        return filter;
    }

    @Override
    public Optional<List<String>> getPrefix(final String dataDef,
            final String itemName) {
        String key = dashit(dataDef, itemName);
        Optional<List<String>> prefix = itemAttributeMap.get(key).getPrefix();
        if (prefix.isPresent()) {
            List<String> copy = Collections.unmodifiableList(prefix.get());
            prefix = Optional.ofNullable(copy);
        }
        return prefix;
    }

    @Override
    public Optional<String> getLinkGroup(final String dataDef,
            final String itemName) {
        String key = dashit(dataDef, itemName);
        return itemAttributeMap.get(key).getLinkGroup();
    }

    @Override
    public Optional<List<String>> getLinkBreakOn(final String dataDef,
            final String itemName) {
        String key = dashit(dataDef, itemName);
        Optional<List<String>> linkBreakOn =
                itemAttributeMap.get(key).getLinkBreakOn();
        if (linkBreakOn.isPresent()) {
            List<String> copy = Collections.unmodifiableList(linkBreakOn.get());
            linkBreakOn = Optional.ofNullable(copy);
        }
        return linkBreakOn;
    }

}
