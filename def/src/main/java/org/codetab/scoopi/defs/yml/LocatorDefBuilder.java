package org.codetab.scoopi.defs.yml;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.defs.IDefBuilder;
import org.codetab.scoopi.defs.IDefData;
import org.codetab.scoopi.exception.DefNotFoundException;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Build LocatorDefData from JsonNode. LocatorDefData is serialized and stored
 * in IStore by DefBootstrap.
 * @author m
 *
 */
public class LocatorDefBuilder implements IDefBuilder {

    @Inject
    private LocatorDefs locatorDefs;

    private LocatorDefData locatorDefData;

    @Override
    public IDefData buildData(final Object defs) throws DefNotFoundException {
        Validate.validState(defs instanceof JsonNode,
                "locatorDefNode is not JsonNode");
        JsonNode node = (JsonNode) defs;

        locatorDefData = new LocatorDefData();
        locatorDefData.setGroupNames(locatorDefs.getGroupNames(node));
        locatorDefData.setLocatorGroups(locatorDefs.getLocatorGroups(node));

        return locatorDefData;
    }
}
