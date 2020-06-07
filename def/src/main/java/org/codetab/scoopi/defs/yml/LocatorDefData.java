package org.codetab.scoopi.defs.yml;

import java.io.Serializable;
import java.util.List;

import org.codetab.scoopi.model.LocatorGroup;

public class LocatorDefData implements Serializable {

    private static final long serialVersionUID = -2725080632696512078L;

    private List<String> groupNames;
    private List<LocatorGroup> locatorGroups;

    public LocatorDefData() {
    }

    public List<String> getGroupNames() {
        return groupNames;
    }

    public void setGroupNames(final List<String> groupNames) {
        this.groupNames = groupNames;
    }

    public List<LocatorGroup> getLocatorGroups() {
        return locatorGroups;
    }

    public void setLocatorGroups(final List<LocatorGroup> locatorGroups) {
        this.locatorGroups = locatorGroups;
    }
}
