package org.codetab.scoopi.defs.yml.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.Range;
import org.codetab.scoopi.defs.yml.helper.AxisDefsHelper;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;

public class AxisDefsCache {

    @Inject
    private AxisDefsHelper axisDefsHelper;

    private Map<String, String> queryCache = new HashMap<>();
    private Map<String, Optional<List<String>>> prefixCache = new HashMap<>();
    private Map<String, Optional<List<String>>> breakAfterCache =
            new HashMap<>();
    private Map<String, Optional<Range<Integer>>> indexRangeCache =
            new HashMap<>();

    public String getQuery(final DataDef dataDef, final AxisName axisName,
            final String queryType) {
        String key = String.join("-", dataDef.getName(), axisName.toString(),
                queryType);
        if (!queryCache.containsKey(key)) {
            try {
                String query =
                        axisDefsHelper.getQuery(dataDef, axisName, queryType);
                queryCache.put(key, query);
            } catch (NoSuchElementException e) {
                queryCache.put(key, "undefined");
            }
        }
        return queryCache.get(key);
    }

    public Optional<List<String>> getPrefixes(final DataDef dataDef,
            final AxisName axisName) {
        String key = String.join("-", dataDef.getName(), axisName.toString());
        if (!prefixCache.containsKey(key)) {
            try {
                Optional<List<String>> prefixes =
                        axisDefsHelper.getPrefixes(dataDef, axisName);
                prefixCache.put(key, prefixes);
            } catch (NoSuchElementException e) {
                prefixCache.put(key, Optional.empty());
            }
        }
        return prefixCache.get(key);
    }

    public Optional<List<String>> getBreakAfters(final DataDef dataDef,
            final Axis axis) {
        String key = String.join("-", dataDef.getName(),
                axis.getName().toString(), axis.getMemberName());
        if (!breakAfterCache.containsKey(key)) {
            try {
                Optional<List<String>> breakAfters =
                        axisDefsHelper.getBreakAfters(dataDef, axis);
                breakAfterCache.put(key, breakAfters);
            } catch (NoSuchElementException e) {
                breakAfterCache.put(key, Optional.empty());
            }
        }
        return breakAfterCache.get(key);
    }

    public Optional<Range<Integer>> getIndexRange(final DataDef dataDef,
            final Axis axis) {
        String key = String.join("-", dataDef.getName(),
                axis.getName().toString(), axis.getMemberName());
        if (!indexRangeCache.containsKey(key)) {
            try {
                Optional<Range<Integer>> indexRange =
                        axisDefsHelper.getIndexRange(dataDef, axis);
                indexRangeCache.put(key, indexRange);
            } catch (NoSuchElementException e) {
                indexRangeCache.put(key, Optional.empty());
            }
        }
        return indexRangeCache.get(key);
    }
}
