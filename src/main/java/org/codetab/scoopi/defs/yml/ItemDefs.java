package org.codetab.scoopi.defs.yml;

import static org.codetab.scoopi.util.Util.dashit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.Filter;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Query;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

class ItemDefs {

    @Inject
    private Jacksons jacksons;
    @Inject
    private ObjectFactory objectFactory;
    @Inject
    private ItemAttributes itemAttributes;
    @Inject
    private ItemAttributeFactory itemAttributeFactory;

    public Map<String, Query> getQueryMap(final JsonNode defs) {

        Map<String, Query> map = new HashMap<>();

        Iterator<String> dataDefNames = defs.fieldNames();
        while (dataDefNames.hasNext()) {
            String dataDefName = dataDefNames.next();

            String path = jacksons.path(dataDefName, "query", "block");
            String block = defs.at(path).asText();

            path = jacksons.path(dataDefName, "query", "selector");
            String selector = defs.at(path).asText();

            path = jacksons.path(dataDefName, "query", "script");
            String script = defs.at(path).asText();

            Query query = objectFactory.createQuery();
            if (StringUtils.isNotBlank(block)) {
                query.setQuery("block", block);
            }
            if (StringUtils.isNotBlank(selector)) {
                query.setQuery("selector", selector);
            }
            if (StringUtils.isNotBlank(script)) {
                query.setQuery("script", script);
            }
            map.put(dataDefName, query);
        }
        return map;
    }

    public Map<String, List<Axis>> getItemAxisMap(final JsonNode defs) {
        return getItemMap(defs, "items", "item");
    }

    public Map<String, List<Axis>> getDimAxisMap(final JsonNode defs) {
        return getItemMap(defs, "dims", "dim");
    }

    public Map<String, List<Axis>> getFactAxisMap(final JsonNode defs) {
        return getItemMap(defs, "fact", "fact");
    }

    private Map<String, List<Axis>> getItemMap(final JsonNode defs,
            final String type, final String axisName) {
        Map<String, List<Axis>> map = new HashMap<>();
        Iterator<String> dataDefNames = defs.fieldNames();
        while (dataDefNames.hasNext()) {
            String dataDefName = dataDefNames.next();
            String path = jacksons.path(dataDefName, type);
            List<JsonNode> jItems = defs.at(path).findValues("item");
            List<Axis> list = new ArrayList<>();
            for (JsonNode jItem : jItems) {
                // default null
                String itemName = jItem.path("name").asText(null);
                String match = jItem.path("match").asText(null);
                String value = jItem.path("value").asText(null);

                Integer index = null;
                String indexStr = jItem.path("index").asText();
                if (StringUtils.isNotBlank(indexStr)) {
                    index = Integer.valueOf(indexStr);
                }

                Integer order = null;
                String orderStr = jItem.path("order").asText();
                if (StringUtils.isNotBlank(orderStr)) {
                    order = Integer.valueOf(orderStr);
                }

                Axis axis = objectFactory.createAxis(axisName, itemName, value,
                        match, index, order);
                list.add(axis);
            }
            map.put(dataDefName, list);
        }
        return map;
    }

    public Map<String, Data> generateDataTemplates(
            final Map<String, List<Axis>> itemAxisMap,
            final Map<String, List<Axis>> dimAxisMap,
            final Map<String, List<Axis>> factAxisMap) {
        Map<String, Data> map = new HashMap<>();

        for (String dataDef : itemAxisMap.keySet()) {
            Data data = objectFactory.createData(dataDef);
            List<Axis> itemAxisList = itemAxisMap.get(dataDef);
            List<Axis> dimAxisList = dimAxisMap.get(dataDef);
            List<Axis> factAxisList = factAxisMap.get(dataDef);

            int arraySize = dimAxisList.size() + 2;
            @SuppressWarnings("unchecked")
            List<Axis>[] a = new ArrayList[arraySize];
            int index = 0;
            a[index++] = itemAxisList;
            for (Axis axis : dimAxisList) {
                List<Axis> list = Lists.newArrayList(axis);
                a[index++] = list;
            }
            a[index] = factAxisList;

            List<List<Axis>> cartList = Lists.cartesianProduct(a);
            for (List<Axis> itemAxis : cartList) {
                Item item = objectFactory.createItem();
                item.setAxes(itemAxis);
                data.addItem(item);
            }
            map.put(dataDef, data);
        }
        return map;
    }

    public Map<String, JsonNode> getItemNodeMap(final JsonNode defs) {

        Map<String, JsonNode> map = new HashMap<>();

        Iterator<Entry<String, JsonNode>> entries = defs.fields();
        while (entries.hasNext()) {
            Entry<String, JsonNode> entry = entries.next();
            String dataDefName = entry.getKey();
            JsonNode jDataDef = entry.getValue();
            // all item of items and dims
            List<JsonNode> jItems = jDataDef.findValues("item");
            for (JsonNode jItem : jItems) {
                String itemName = jItem.path("name").asText();
                String key = dashit(dataDefName, itemName);
                map.put(key, jItem);
            }
        }
        return map;
    }

    public Map<String, ItemAttribute> getItemAttributeMap(final JsonNode defs,
            final Map<String, JsonNode> itemNodeMap) {
        Map<String, Optional<Query>> queryMap =
                itemAttributes.getItemQueryMap(defs, itemNodeMap);
        Map<String, Range<Integer>> indexRangeMap =
                itemAttributes.getIndexRangeMap(defs, itemNodeMap);
        Map<String, Optional<List<String>>> breakAfterMap =
                itemAttributes.getBreakAfterMap(defs, itemNodeMap);
        Map<String, Optional<List<Filter>>> filterMap =
                itemAttributes.getFilterMap(defs, itemNodeMap);
        Map<String, Optional<List<String>>> prefixMap =
                itemAttributes.getPrefixMap(defs, itemNodeMap);
        Map<String, Optional<String>> linkGroupMap =
                itemAttributes.getLinkGroupMap(defs, itemNodeMap);

        Map<String, ItemAttribute> map = new HashMap<>();

        for (String key : itemNodeMap.keySet()) {
            Optional<Query> query = queryMap.get(key);
            Range<Integer> indexRange = indexRangeMap.get(key);
            Optional<List<String>> breakAfter = breakAfterMap.get(key);
            Optional<List<String>> prefix = prefixMap.get(key);
            Optional<List<Filter>> filter = filterMap.get(key);
            Optional<String> linkGroup = linkGroupMap.get(key);
            ItemAttribute itemAttribute = itemAttributeFactory.create(key,
                    query, indexRange, breakAfter, filter, prefix, linkGroup);
            map.put(key, itemAttribute);
        }
        return map;
    }

}
