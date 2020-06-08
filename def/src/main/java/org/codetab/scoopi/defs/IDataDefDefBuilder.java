package org.codetab.scoopi.defs;

import org.codetab.scoopi.defs.yml.DataDefDefData;
import org.codetab.scoopi.exception.DefNotFoundException;

public interface IDataDefDefBuilder {

    byte[] serialize(DataDefDefData data);

    DataDefDefData deserialize(byte[] data);

    DataDefDefData buildData(Object defs) throws DefNotFoundException;

}
