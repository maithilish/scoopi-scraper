package org.codetab.scoopi.defs;

import java.util.List;
import java.util.Optional;

import org.codetab.scoopi.model.LocatorGroup;

public interface ILocatorDef {

    List<String> getGroups();

    Optional<LocatorGroup> getLocatorGroup(String group);

    List<LocatorGroup> getLocatorGroups();

}
