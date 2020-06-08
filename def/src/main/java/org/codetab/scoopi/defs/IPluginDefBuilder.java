package org.codetab.scoopi.defs;

import org.codetab.scoopi.defs.yml.PluginDefData;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.InvalidDefException;

public interface IPluginDefBuilder {

    byte[] serialize(PluginDefData data);

    PluginDefData deserialize(byte[] data);

    PluginDefData buildData(Object defs)
            throws DefNotFoundException, InvalidDefException;

}
