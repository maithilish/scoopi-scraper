package org.codetab.scoopi.defs;

import java.util.List;

import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;

public interface IAxisDefs {

    List<String> getBreakAfters(String dataDef, Axis axis)
            throws DataDefNotFoundException;

    int getStartIndex(String dataDef, Axis axis)
            throws DataDefNotFoundException;

    int getEndIndex(String dataDef, Axis axis) throws DataDefNotFoundException;

    boolean isRangeAxis(String dataDef, Axis axis)
            throws DataDefNotFoundException;

    String getQuery(String dataDef, AxisName axisName, String queryType)
            throws DataDefNotFoundException;

}
