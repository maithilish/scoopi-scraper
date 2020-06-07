package org.codetab.scoopi.defs;

import org.codetab.scoopi.defs.yml.ItemDefData;
import org.codetab.scoopi.exception.DefNotFoundException;

public interface IItemDefBuilder {

    byte[] serialize(ItemDefData data);

    ItemDefData deserialize(byte[] data);

    ItemDefData buildData(Object defs) throws DefNotFoundException;

}
