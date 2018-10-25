package org.codetab.scoopi.model.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.codetab.scoopi.defs.IAxisDefs;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.model.Member;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.system.ErrorLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class LocatorGroupFactory {

    static final Logger LOGGER =
            LoggerFactory.getLogger(LocatorGroupFactory.class);

    @Inject
    private IAxisDefs axisDefs;
    @Inject
    private ObjectFactory objectFactory;
    @Inject
    private ErrorLogger errorLogger;

    public List<LocatorGroup> createLocatorGroups(final DataDef dataDef,
            final List<Member> members, final String locatorName) {
        Map<String, LocatorGroup> lgs = new HashMap<>();
        for (Member member : members) {
            Axis row = member.getAxis(AxisName.ROW);
            Axis fact = member.getAxis(AxisName.FACT);
            if (StringUtils.isBlank(fact.getValue())) {
                continue;
            }
            Optional<String> optionalLinkGroup =
                    axisDefs.getLinkGroup(dataDef, row);
            if (optionalLinkGroup.isPresent()) {
                String linkGroup = optionalLinkGroup.get();
                String url = fact.getValue();
                Locator locator = objectFactory.createLocator(locatorName,
                        linkGroup, url);
                if (!lgs.containsKey(linkGroup)) {
                    LocatorGroup lg =
                            objectFactory.createLocatorGroup(linkGroup);
                    lgs.put(linkGroup, lg);
                }
                LocatorGroup lg = lgs.get(linkGroup);
                lg.getLocators().add(locator);
            } else {
                String label = String.join(":", dataDef.getName(),
                        row.getMemberName());
                String message = String.join(" ",
                        "create locator from link, no linkGroup defined for member:",
                        label);
                errorLogger.log(CAT.ERROR, message);
            }
        }
        return Lists.newArrayList(lgs.values());
    }
}
