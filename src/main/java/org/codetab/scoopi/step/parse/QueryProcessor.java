package org.codetab.scoopi.step.parse;

import static java.util.Objects.isNull;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.inject.Inject;
import javax.script.ScriptException;

import org.codetab.scoopi.defs.IItemDef;
import org.codetab.scoopi.model.Query;
import org.codetab.scoopi.step.parse.cache.ParserCache;

public class QueryProcessor {

    @Inject
    private IItemDef itemDef;
    @Inject
    private ParserCache parserCache;

    public Map<String, String> getQueries(final String dataDef,
            final String axisName, String itemName) {
        Map<String, String> queries = new HashMap<>();
        Query query = itemDef.getQuery(dataDef);
        Optional<Query> itemQuery = itemDef.getItemQuery(dataDef, itemName);

        String block;
        String selector;
        if (axisName.toLowerCase().equals("fact")) {
            block = query.getQuery("block");

            selector = query.getQuery("selector");

        } else {
            if (itemQuery.isPresent()) {
                block = query.getQuery("block");
                selector = itemQuery.get().getQuery("selector");
            } else {
                throw new NoSuchElementException();
            }
        }

        String[] parts = selector.split(" attribute: ");

        queries.put("block", block); //$NON-NLS-1$
        queries.put("selector", parts[0]);
        if (parts.length > 1) {
            queries.put("attribute", parts[1]);
        }
        return queries;
    }

    public String query(final Map<String, String> queries,
            final IValueParser valueParser) throws ScriptException {
        int key = parserCache.getKey(queries);
        String value = parserCache.get(key);
        if (isNull(value)) {
            value = valueParser.parseValue(queries);
            parserCache.put(key, value);
        }
        return value;
    }

}
