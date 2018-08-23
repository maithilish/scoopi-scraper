package org.codetab.scoopi.defs.yml.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import org.apache.commons.lang3.Range;
import org.codetab.scoopi.defs.IDataDefDefs;
import org.codetab.scoopi.defs.yml.helper.AxisDefsHelper;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;

public class AxisDefsCache {

    @Inject
    private IDataDefDefs dataDefDefs;
    @Inject
    private AxisDefsHelper axisDefsHelper;

    private Map<String, List<String>> breakAfterCache = new HashMap<>();
    private Map<String, Range<Integer>> indexRangeCache = new HashMap<>();
    private Map<String, String> queryCache = new HashMap<>();
    private Map<String, Boolean> rangeAxisCache = new HashMap<>();

    public List<String> getBreakAfters(final String dataDef, final Axis axis)
            throws DataDefNotFoundException {
        String key = String.join("-", dataDef, axis.getName().toString(),
                axis.getMemberName());
        if (!breakAfterCache.containsKey(key)) {
            DataDef dDef = dataDefDefs.getDataDef(dataDef);
            List<String> breakAfters =
                    axisDefsHelper.getBreakAfters(dDef, axis);
            breakAfterCache.put(key, breakAfters);
        }
        return breakAfterCache.get(key);
    }

    public Range<Integer> getIndexRange(final String dataDef, final Axis axis)
            throws DataDefNotFoundException {
        String key = String.join("-", dataDef, axis.getName().toString(),
                axis.getMemberName());
        if (!indexRangeCache.containsKey(key)) {
            DataDef dDef = dataDefDefs.getDataDef(dataDef);
            Range<Integer> indexRange =
                    axisDefsHelper.getIndexRange(dDef, axis);
            indexRangeCache.put(key, indexRange);
        }
        return indexRangeCache.get(key);
    }

    public boolean isRangeAxis(final String dataDef, final Axis axis)
            throws DataDefNotFoundException {
        String key = String.join("-", dataDef, axis.getName().toString(),
                axis.getMemberName());
        if (!rangeAxisCache.containsKey(key)) {
            DataDef dDef = dataDefDefs.getDataDef(dataDef);
            boolean rangeAxis = axisDefsHelper.isRangeAxis(dDef, axis);
            rangeAxisCache.put(key, rangeAxis);
        }
        return rangeAxisCache.get(key);
    }

    public String getQuery(final String dataDef, final AxisName axisName,
            final String queryType) throws DataDefNotFoundException {
        String key = String.join("-", dataDef, axisName.toString(), queryType);
        if (!queryCache.containsKey(key)) {
            DataDef dDef = dataDefDefs.getDataDef(dataDef);
            try {
                String query =
                        axisDefsHelper.getQuery(dDef, axisName, queryType);
                queryCache.put(key, query);
            } catch (NoSuchElementException e) {
                queryCache.put(key, "undefined");
            }
        }
        return queryCache.get(key);
    }
}
