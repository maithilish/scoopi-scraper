package org.codetab.scoopi.defs;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.Range;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;

public interface IAxisDefs {

    String getQuery(DataDef dataDef, AxisName axisName, String queryType);

    Optional<List<String>> getBreakAfters(DataDef dataDef, Axis axis);

    Optional<Range<Integer>> getIndexRange(DataDef dataDef, Axis axis);

    Optional<List<String>> getPrefixes(DataDef dataDef, AxisName axisName);
}
