package org.codetab.scoopi.defs.yml;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.defs.IDefBuilder;
import org.codetab.scoopi.defs.IDefData;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.DataDef;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Build TaskDefData from JsonNode. TaskDefData is serialized and stored in
 * IStore by DefBootstrap.
 * @author m
 *
 */
public class DataDefDefBuilder implements IDefBuilder {

    @Inject
    private DataDefDefs dataDefDefs;

    private DataDefDefData dataDefDefData;

    @Override
    public IDefData buildData(final Object defs) throws DefNotFoundException {
        Validate.validState(defs instanceof JsonNode,
                "dataDefDefsNode is not JsonNode");
        try {
            dataDefDefData = new DataDefDefData();
            JsonNode node = (JsonNode) defs;
            List<DataDef> dataDefs = dataDefDefs.createDataDefs(node);
            dataDefDefData.setDefinedDataDefs(dataDefs);
            dataDefDefs.setDefs(dataDefDefData.getDefinedDataDefs());
            dataDefDefData.setDataDefMap(
                    dataDefDefs.toMap(dataDefDefData.getDefinedDataDefs()));

            dataDefDefs.traceDataDef(dataDefs);

            return dataDefDefData;
        } catch (IOException e) {
            throw new CriticalException("unable to create datadefs", e);
        }
    }
}
