package org.codetab.scoopi.defs;

import java.util.List;
import java.util.Optional;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.LocatorGroup;

public interface ILocatorDef {

    void init(Object locatorDefs) throws DefNotFoundException;

    List<String> getGroups();

    Optional<LocatorGroup> getLocatorGroup(String group);

    List<LocatorGroup> getLocatorGroups();

}
