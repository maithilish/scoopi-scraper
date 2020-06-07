package org.codetab.scoopi.defs;

import org.codetab.scoopi.defs.yml.LocatorDefData;
import org.codetab.scoopi.exception.DefNotFoundException;

public interface ILocatorDefBuilder {

    byte[] serialize(LocatorDefData data);

    LocatorDefData deserialize(byte[] data);

    LocatorDefData buildData(Object defs) throws DefNotFoundException;

}
