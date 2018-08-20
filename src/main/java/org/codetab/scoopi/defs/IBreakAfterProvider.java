package org.codetab.scoopi.defs;

import java.util.List;

import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.Axis;

public interface IBreakAfterProvider {

    List<String> getBreakAfters(String dataDef, Axis axis, String memberName)
            throws DataDefNotFoundException;
}
