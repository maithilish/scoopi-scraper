package org.codetab.scoopi.defs;

import org.codetab.scoopi.defs.yml.TaskDefData;
import org.codetab.scoopi.exception.DefNotFoundException;

public interface ITaskDefBuilder {

    byte[] serialize(TaskDefData data);

    TaskDefData deserialize(byte[] data);

    TaskDefData buildData(Object defs) throws DefNotFoundException;

}
