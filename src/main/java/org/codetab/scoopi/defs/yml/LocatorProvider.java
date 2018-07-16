package org.codetab.scoopi.defs.yml;

import static java.util.stream.Collectors.toList;

import java.util.List;

import javax.inject.Singleton;

import org.codetab.scoopi.defs.ILocatorProvider;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.LocatorGroup;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

@Singleton
public class LocatorProvider implements ILocatorProvider {

    private JsonNode defs;

    /**
     * on first invoke sets defs and subsequent invocations do nothing.
     * @param defs
     */
    public void init(final JsonNode locatorDefs) {
        if (this.defs == null) {
            this.defs = locatorDefs;
        }
    }

    @Override
    public List<String> getGroups() {
        return Lists.newArrayList(defs.fieldNames());
    }

    @Override
    public LocatorGroup getLocatorGroup(final String group) {
        LocatorGroup locatorGroup = new LocatorGroup();
        locatorGroup.setGroup(group);

        JsonNode groupNode = defs.at("/" + group);
        JsonNode nodes = groupNode.get("locators");
        for (int i = 0; i < nodes.size(); i++) {
            JsonNode node = nodes.get(i);
            Locator locator = new Locator();
            locator.setGroup(group);
            locator.setName(node.get("name").asText());
            locator.setUrl(node.get("url").asText());
            locatorGroup.getLocators().add(locator);
        }
        return locatorGroup;
    }

    @Override
    public List<LocatorGroup> getLocatorGroups() {
        return getGroups().stream().map(this::getLocatorGroup)
                .collect(toList());
    }
}
