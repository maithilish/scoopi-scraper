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
    private Map<Integer, String> fieldQueryCache = new HashMap<>();
    private Map<Integer, List<String>> fieldNamesCache = new HashMap<>();

    public String getRegionQuery(final DataDef dataDef, final String name) {
        int key = Objects.hash(dataDef.getName(), name);
        if (!regionQueryCache.containsKey(key)) {
            try {
                String query = itemDefsHelper.getRegionQuery(dataDef, name);
                regionQueryCache.put(key, query);
            } catch (NoSuchElementException e) {
                regionQueryCache.put(key, "undefined");
            }
        }
        return regionQueryCache.get(key);
    }

    public String getFieldQuery(final DataDef dataDef, final String itemName,
            final String fieldName) {
        int key = Objects.hash(dataDef.getName(), itemName, fieldName);
        if (!fieldQueryCache.containsKey(key)) {
            try {
                String query = itemDefsHelper.getFieldQuery(dataDef, itemName,
                        fieldName);
                fieldQueryCache.put(key, query);
            } catch (NoSuchElementException e) {
                fieldQueryCache.put(key, "undefined");
            }
        }
        return fieldQueryCache.get(key);
    }

    public List<String> getFieldNames(final DataDef dataDef,
            final String name) {
        int key = Objects.hash(dataDef.getName(), name);
        if (!fieldNamesCache.containsKey(key)) {
            try {
                List<String> fieldNames =
                        itemDefsHelper.getFieldNames(dataDef, name);
                fieldNamesCache.put(key, fieldNames);
            } catch (NoSuchElementException e) {
                fieldNamesCache.put(key, new ArrayList<>());
            }
        }
        return fieldNamesCache.get(key);
    }

}
