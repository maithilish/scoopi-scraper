package org.codetab.scoopi.defs.yml;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.defs.IDataDefDefBuilder;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.exception.DefNotFoundException;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Build TaskDefData from JsonNode. TaskDefData is serialized and stored in
 * IStore by DefBootstrap.
 * @author m
 *
 */
public class DataDefDefBuilder implements IDataDefDefBuilder {

    @Inject
    private DataDefDefs dataDefDefs;
    @Inject
    private DataDefDefData dataDefDefData;

    @Override
    public byte[] serialize(final DataDefDefData data) {
        return SerializationUtils.serialize(data);
    }

    @Override
    public DataDefDefData deserialize(final byte[] data) {
        return SerializationUtils.deserialize(data);
    }

    @Override
    public DataDefDefData buildData(final Object defs)
            throws DefNotFoundException {
        Validate.validState(defs instanceof JsonNode,
                "dataDefDefsNode is not JsonNode");
        try {
            JsonNode node = (JsonNode) defs;
            dataDefDefData.setDefinedDataDefs(dataDefDefs.createDataDefs(node));
            dataDefDefs.setDefs(dataDefDefData.getDefinedDataDefs());
            dataDefDefData.setDataDefMap(
                    dataDefDefs.toMap(dataDefDefData.getDefinedDataDefs()));
            return dataDefDefData;
        } catch (IOException e) {
            throw new CriticalException("unable to create datadefs", e);
        }
    }
}
