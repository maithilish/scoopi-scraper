package org.codetab.scoopi.defs;

import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.DataDef;

public interface IDataDefProvider {

    DataDef getDataDef(String name) throws DataDefNotFoundException;

    Long getDataDefId(String name) throws DataDefNotFoundException;

    Data getDataTemplate(String dataDef);
}
