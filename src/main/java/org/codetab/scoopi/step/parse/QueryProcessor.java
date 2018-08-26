package org.codetab.scoopi.step.parse;

import static java.util.Objects.isNull;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.inject.Inject;
import javax.script.ScriptException;

import org.codetab.scoopi.cache.ParserCache;
import org.codetab.scoopi.defs.IAxisDefs;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;

public class QueryProcessor {

    @Inject
    private IAxisDefs axisDefs;
    @Inject
    private ParserCache parserCache;

    public Map<String, String> getQueries(final DataDef dataDef,
            final AxisName axisName) {
        Map<String, String> queries = new HashMap<>();
        String region = axisDefs.getQuery(dataDef, axisName, "region");
        String field = axisDefs.getQuery(dataDef, axisName, "field");
        if (region.equals("undefined") || field.equals("undefined")) {
            throw new NoSuchElementException("query not defined");
        }
        // optional attribute
        String attribute = axisDefs.getQuery(dataDef, axisName, "attribute");
        if (attribute.equals("undefined")) {
            attribute = "";
        }

        queries.put("region", region); //$NON-NLS-1$
        queries.put("field", field); //$NON-NLS-1$
        queries.put("attribute", attribute); //$NON-NLS-1$ //$NON-NLS-2$

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
