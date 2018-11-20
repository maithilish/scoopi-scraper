package org.codetab.scoopi.defs;

import org.codetab.scoopi.exception.DataDefNotFoundException;

public interface IDataDefDef {

    void init(Object dataDefDefs);

    Long getDataDefId(String name) throws DataDefNotFoundException;

}
