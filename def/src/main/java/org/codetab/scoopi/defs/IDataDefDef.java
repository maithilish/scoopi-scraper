package org.codetab.scoopi.defs;

import java.util.List;

import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.DataDef;

public interface IDataDefDef {

    Long getDataDefId(String name) throws DataDefNotFoundException;

    List<DataDef> getDefinedDataDefs();

    void updateDataDefs(List<DataDef> updatedDataDefs);
}
