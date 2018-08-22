package org.codetab.scoopi.defs.yml;

import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import org.apache.commons.lang3.Range;
import org.codetab.scoopi.defs.IAxisDefs;
import org.codetab.scoopi.defs.yml.cache.AxisDefsCache;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;

public class AxisDefs implements IAxisDefs {

    @Inject
    private AxisDefsCache cache;

    @Override
    public List<String> getBreakAfters(final String dataDef, final Axis axis)
            throws DataDefNotFoundException {
        return cache.getBreakAfters(dataDef, axis);
    }

    @Override
    public int getStartIndex(final String dataDef, final Axis axis)
            throws DataDefNotFoundException {
        try {
            Range<Integer> indexRange = cache.getIndexRange(dataDef, axis);
            return indexRange.getMinimum();
        } catch (NoSuchElementException e) {
            return 1;
        }
    }

    @Override
    public int getEndIndex(final String dataDef, final Axis axis)
            throws DataDefNotFoundException {
        try {
            Range<Integer> indexRange = cache.getIndexRange(dataDef, axis);
            return indexRange.getMaximum();
        } catch (NoSuchElementException e) {
            return -1;
        }
    }

    @Override
    public String getQuery(final String dataDef, final AxisName axisName,
            final String queryType) throws DataDefNotFoundException {
        return cache.getQuery(dataDef, axisName, queryType);
    }
}
