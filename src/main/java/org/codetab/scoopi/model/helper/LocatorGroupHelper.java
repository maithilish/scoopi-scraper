package org.codetab.scoopi.model.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IAxisDefs;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.model.Member;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.system.ErrorLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class LocatorGroupHelper {

    static final Logger LOGGER =
            LoggerFactory.getLogger(LocatorGroupHelper.class);

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
            Optional<String> optionalLinkGroup =
                    axisDefs.getLinkGroup(dataDef, row);
            if (optionalLinkGroup.isPresent()) {
                String linkGroup = optionalLinkGroup.get();
                String url = member.getAxis(AxisName.FACT).getValue();
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

    public List<Payload> createSeedPayloads(
            final List<LocatorGroup> locatorGroups, final String stepName,
            final String seederClzName) {
        List<Payload> payloads = new ArrayList<>();
        for (LocatorGroup locatorGroup : locatorGroups) {
            // for init payload, only stepName, className and taskGroup are
            // set. Next and previous steps, taskName, dataDef are undefined
            String undefined = "undefined";
            StepInfo stepInfo = objectFactory.createStepInfo(stepName,
                    undefined, undefined, seederClzName);
            JobInfo jobInfo = objectFactory.createJobInfo(0, undefined,
                    locatorGroup.getGroup(), undefined, undefined);
            Payload payload = objectFactory.createPayload(jobInfo, stepInfo,
                    locatorGroup);
            payloads.add(payload);
        }
        return payloads;
    }
}
