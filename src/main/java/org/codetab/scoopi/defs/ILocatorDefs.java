package org.codetab.scoopi.defs;

import java.util.List;

import org.codetab.scoopi.model.LocatorGroup;

public interface ILocatorDefs {

    List<String> getGroups();

    LocatorGroup getLocatorGroup(String group);

    List<LocatorGroup> getLocatorGroups();
}
