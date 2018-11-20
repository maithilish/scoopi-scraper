package org.codetab.scoopi.defs.yml;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.defs.ILocatorDef;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.LocatorGroup;

import com.fasterxml.jackson.databind.JsonNode;

@Singleton
public class LocatorDef implements ILocatorDef {

    @Inject
    private LocatorDefs locatorDefs;

    private JsonNode defs;
    private List<String> groupNames;
    private List<LocatorGroup> locatorGroups;

    @Override
    public void init(final Object locatorDefNodes) throws DefNotFoundException {
        Validate.validState(locatorDefNodes instanceof JsonNode,
                "locatorDefNodes is not JsonNode");

        this.defs = (JsonNode) locatorDefNodes;
        groupNames = locatorDefs.getGroupNames(defs);
        locatorGroups = locatorDefs.getLocatorGroups(defs);
    }

    @Override
    public List<String> getGroups() {
        return Collections.unmodifiableList(groupNames);
    }

    @Override
    public Optional<LocatorGroup> getLocatorGroup(final String group) {
        Optional<LocatorGroup> locatorGroup = locatorGroups.stream()
                .filter(lg -> lg.getGroup().equals(group)).findFirst();
        if (locatorGroup.isPresent()) {
            LocatorGroup copy = locatorGroup.get().copy();
            locatorGroup = Optional.ofNullable(copy);
        }
        return locatorGroup;
    }

    @Override
    public List<LocatorGroup> getLocatorGroups() {
        return locatorGroups;
    }
}
