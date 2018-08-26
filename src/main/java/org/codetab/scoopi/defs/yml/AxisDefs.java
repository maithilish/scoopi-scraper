package org.codetab.scoopi.defs.yml;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.Range;
import org.codetab.scoopi.defs.IAxisDefs;
import org.codetab.scoopi.defs.yml.cache.AxisDefsCache;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;

public class AxisDefs implements IAxisDefs {

    @Inject
    private AxisDefsCache cache;

    @Override
    public String getQuery(final DataDef dataDef, final AxisName axisName,
            final String queryType) {
        return cache.getQuery(dataDef, axisName, queryType);
    }

    @Override
    public Optional<List<String>> getBreakAfters(final DataDef dataDef,
            final Axis axis) {
        return cache.getBreakAfters(dataDef, axis);
    }

    @Override
    public Optional<Range<Integer>> getIndexRange(final DataDef dataDef,
            final Axis axis) {
        return cache.getIndexRange(dataDef, axis);
    }

    @Override
    public Optional<List<String>> getPrefixes(final DataDef dataDef,
            final AxisName axisName) {
        return cache.getPrefixes(dataDef, axisName);
    }

}
