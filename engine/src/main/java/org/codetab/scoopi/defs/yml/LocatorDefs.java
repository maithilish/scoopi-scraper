package org.codetab.scoopi.defs.yml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.ObjectFactory;

import com.fasterxml.jackson.databind.JsonNode;

class LocatorDefs {

    @Inject
    private Jacksons jacksons;
    @Inject
    private ObjectFactory objectFactory;

    public List<LocatorGroup> getLocatorGroups(final JsonNode defs)
            throws DefNotFoundException {

        List<LocatorGroup> locatorGroups = new ArrayList<>();

        Iterator<Entry<String, JsonNode>> entries = defs.fields();
        while (entries.hasNext()) {
            Entry<String, JsonNode> entry = entries.next();

            String groupName = entry.getKey();
            LocatorGroup locatorGroup =
                    objectFactory.createLocatorGroup(groupName);
            locatorGroups.add(locatorGroup);

            JsonNode jGroup = entry.getValue();
            JsonNode jLocators = jGroup.path("locators");
            for (int i = 0; i < jLocators.size(); i++) {
                JsonNode jLocator = jLocators.get(i);
                String locatorName = jLocator.get("name").asText();
                String locatorUrl = jLocator.get("url").asText();
                Locator locator = objectFactory.createLocator(locatorName,
                        groupName, locatorUrl);
                locatorGroup.getLocators().add(locator);
            }
        }
        if (locatorGroups.isEmpty()) {
            throw new DefNotFoundException("locatorGroups");
        } else {
            return locatorGroups;
        }
    }

    public List<String> getGroupNames(final JsonNode defs)
            throws DefNotFoundException {
        List<String> groupNames = jacksons.getFieldNames(defs);
        if (groupNames.isEmpty()) {
            throw new DefNotFoundException("no locator groups found");
        } else {
            return groupNames;
        }
    }

}
