package org.codetab.scoopi.defs.yml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.defs.IItemDefs;
import org.codetab.scoopi.defs.yml.cache.ItemDefsCache;
import org.codetab.scoopi.model.DataDef;

@Singleton
public class ItemDefs implements IItemDefs {

    @Inject
    private ItemDefsCache cache;

    @Override
    public List<String> getFieldNames(final DataDef dataDef,
            final String itemName) {
        return cache.getFieldNames(dataDef, itemName);
    }

    @Override
    public Map<String, String> getQueries(final DataDef dataDef,
            final String itemName, final String fieldName) {

        Map<String, String> queries = new HashMap<>();
        queries.put("region", cache.getRegionQuery(dataDef, itemName));

        String fieldQuery = cache.getFieldQuery(dataDef, itemName, fieldName);
        String[] parts = fieldQuery.split(" attribute: ");
        queries.put("field", parts[0]);
        if (parts.length > 1) {
            queries.put("attribute", parts[1]);
        }
        return queries;
    }

}
