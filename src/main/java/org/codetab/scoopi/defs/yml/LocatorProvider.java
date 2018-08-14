package org.codetab.scoopi.defs.yml;

import static java.util.stream.Collectors.toList;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.defs.ILocatorProvider;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.ObjectFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

@Singleton
public class LocatorProvider implements ILocatorProvider {

    @Inject
    private ObjectFactory objectFactory;

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
        LocatorGroup locatorGroup = objectFactory.createLocatorGroup(group);

        JsonNode groupNode = defs.at("/" + group);
        JsonNode nodes = groupNode.get("locators");
        for (int i = 0; i < nodes.size(); i++) {
            JsonNode node = nodes.get(i);

            String locatorName = node.get("name").asText();
            String locatorUrl = node.get("url").asText();
            Locator locator =
                    objectFactory.createLocator(locatorName, group, locatorUrl);
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
