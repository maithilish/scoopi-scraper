package org.codetab.scoopi.defs.yml.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import javax.inject.Inject;

import org.codetab.scoopi.defs.yml.helper.ItemDefsHelper;
import org.codetab.scoopi.model.DataDef;

public class ItemDefsCache {

    @Inject
    private ItemDefsHelper itemDefsHelper;

    private Map<Integer, String> regionQueryCache = new HashMap<>();
    private Map<Integer, String> itemQueryCache = new HashMap<>();
    private Map<Integer, List<String>> itemNamesCache = new HashMap<>();

    public String getRegionQuery(final DataDef dataDef,
            final String itemsName) {
        int key = Objects.hash(dataDef.getName(), itemsName);
        if (!regionQueryCache.containsKey(key)) {
            try {
                String query =
                        itemDefsHelper.getRegionQuery(dataDef, itemsName);
                regionQueryCache.put(key, query);
            } catch (NoSuchElementException e) {
                regionQueryCache.put(key, "undefined");
            }
        }
        return regionQueryCache.get(key);
    }

    public String getItemQuery(final DataDef dataDef, final String itemsName,
            final String itemName) {
        int key = Objects.hash(dataDef.getName(), itemsName, itemName);
        if (!itemQueryCache.containsKey(key)) {
            try {
                String query = itemDefsHelper.getItemQuery(dataDef, itemsName,
                        itemName);
                itemQueryCache.put(key, query);
            } catch (NoSuchElementException e) {
                itemQueryCache.put(key, "undefined");
            }
        }
        return itemQueryCache.get(key);
    }

    public List<String> getItemNames(final DataDef dataDef,
            final String itemsName) {
        int key = Objects.hash(dataDef.getName(), itemsName);
        if (!itemNamesCache.containsKey(key)) {
            try {
                List<String> itemNames =
                        itemDefsHelper.getItemNames(dataDef, itemsName);
                itemNamesCache.put(key, itemNames);
            } catch (NoSuchElementException e) {
                itemNamesCache.put(key, new ArrayList<>());
            }
        }
        return itemNamesCache.get(key);
    }

}
