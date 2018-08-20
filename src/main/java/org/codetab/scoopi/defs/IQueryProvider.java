package org.codetab.scoopi.defs;

import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.AxisName;

public interface IQueryProvider {

    String getQuery(String dataDef, AxisName axisName, String queryType)
            throws DataDefNotFoundException;
}
