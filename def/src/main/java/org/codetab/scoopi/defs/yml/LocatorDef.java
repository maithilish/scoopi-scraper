package org.codetab.scoopi.defs.yml;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.defs.ILocatorDef;
import org.codetab.scoopi.model.LocatorGroup;

@Singleton
public class LocatorDef implements ILocatorDef {

    @Inject
    private LocatorDefData data;

    @Override
    public List<String> getGroups() {
        return Collections.unmodifiableList(data.getGroupNames());
    }

    @Override
    public Optional<LocatorGroup> getLocatorGroup(final String group) {
        Optional<LocatorGroup> locatorGroup = data.getLocatorGroups().stream()
                .filter(lg -> lg.getGroup().equals(group)).findFirst();
        if (locatorGroup.isPresent()) {
            LocatorGroup copy = locatorGroup.get().copy();
            locatorGroup = Optional.ofNullable(copy);
        }
        return locatorGroup;
    }

    @Override
    public List<LocatorGroup> getLocatorGroups() {
        return data.getLocatorGroups();
    }
}
