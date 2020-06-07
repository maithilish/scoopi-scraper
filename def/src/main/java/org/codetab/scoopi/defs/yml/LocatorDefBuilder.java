package org.codetab.scoopi.defs.yml;

import javax.inject.Inject;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.defs.ILocatorDefBuilder;
import org.codetab.scoopi.exception.DefNotFoundException;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Build TaskDefData from JsonNode. TaskDefData is serialized and stored in
 * IStore by DefBootstrap.
 * @author m
 *
 */
public class LocatorDefBuilder implements ILocatorDefBuilder {

    @Inject
    private LocatorDefs locatorDefs;
    @Inject
    private LocatorDefData locatorDefData;

    @Override
    public byte[] serialize(final LocatorDefData data) {
        return SerializationUtils.serialize(data);
    }

    @Override
    public LocatorDefData deserialize(final byte[] data) {
        return SerializationUtils.deserialize(data);
    }

    @Override
    public LocatorDefData buildData(final Object defs)
            throws DefNotFoundException {
        Validate.validState(defs instanceof JsonNode,
                "locatorDefNode is not JsonNode");
        JsonNode node = (JsonNode) defs;

        locatorDefData.setGroupNames(locatorDefs.getGroupNames(node));
        locatorDefData.setLocatorGroups(locatorDefs.getLocatorGroups(node));

        return locatorDefData;
    }
}
