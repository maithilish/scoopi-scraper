package org.codetab.scoopi.step.process;

import static org.codetab.scoopi.util.Util.spaceit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.codetab.scoopi.defs.IItemDef;
import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.log.Log.CAT;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class LocatorGroupFactory {

    static final Logger LOGGER =
            LoggerFactory.getLogger(LocatorGroupFactory.class);

    @Inject
    private IItemDef itemDef;
    @Inject
    private ObjectFactory objectFactory;
    @Inject
    private ErrorLogger errorLogger;

    public List<LocatorGroup> createLocatorGroups(final String dataDef,
            final List<Item> items, final String locatorName) {
        Map<String, LocatorGroup> lgs = new HashMap<>();

        for (Item item : items) {

            Axis axis = item.getFirstAxis();
            Optional<String> oLinkGroup =
                    itemDef.getLinkGroup(dataDef, axis.getItemName());
            Optional<List<String>> oLinkBreakOn =
                    itemDef.getLinkBreakOn(dataDef, axis.getItemName());

            if (oLinkGroup.isPresent()) {
                String linkGroup = oLinkGroup.get();
                String url = axis.getValue();
                boolean createLink = true;
                if (oLinkBreakOn.isPresent()) {
                    if (oLinkBreakOn.get().contains(url)) {
                        createLink = false;
                    }
                }
                if (StringUtils.isNotBlank(url) && createLink) {
                    Locator locator = objectFactory.createLocator(locatorName,
                            linkGroup, url);
                    if (!lgs.containsKey(linkGroup)) {
                        LocatorGroup lg =
                                objectFactory.createLocatorGroup(linkGroup);
                        lgs.put(linkGroup, lg);
                    }
                    LocatorGroup lg = lgs.get(linkGroup);
                    lg.getLocators().add(locator);
                }
            } else {
                String label = String.join(":", dataDef,
                        item.getAxes().get(0).getItemName());
                String message = spaceit(
                        "create locator from link, no linkGroup defined for item:",
                        label);
                errorLogger.log(CAT.ERROR, message);
            }
        }
        return Lists.newArrayList(lgs.values());
    }
}
