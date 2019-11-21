package org.codetab.scoopi.defs.yml;

import static java.util.Objects.nonNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.defs.IDataDefDef;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.DataDef;

import com.fasterxml.jackson.databind.JsonNode;

@Singleton
public class DataDefDef implements IDataDefDef {

    @Inject
    private DataDefDefs dataDefDefs;

    private Map<String, DataDef> dataDefMap;

    private List<DataDef> definedDataDefs;

    @Override
    public void init(final Object dataDefNodes) {
        Validate.validState(dataDefNodes instanceof JsonNode,
                "dataDefNodes is not JsonNode");
        try {
            JsonNode defs = (JsonNode) dataDefNodes;
            definedDataDefs = dataDefDefs.createDataDefs(defs);
            dataDefDefs.setDefs(definedDataDefs);
            dataDefMap = dataDefDefs.toMap(definedDataDefs);
        } catch (IOException e) {
            throw new CriticalException("unable to create datadefs", e);
        }
    }

    @Override
    public void updateDataDefs(final List<DataDef> dataDefs) {
        try {
            dataDefDefs.setDefs(dataDefs);
            dataDefMap = dataDefDefs.toMap(dataDefs);
        } catch (IOException e) {
            throw new CriticalException("unable to update datadefs", e);
        }
    }

    @Override
    public Long getDataDefId(final String name)
            throws DataDefNotFoundException {
        DataDef dataDef = dataDefMap.get(name);
        if (nonNull(dataDef)) {
            return dataDef.getId();
        } else {
            throw new DataDefNotFoundException(name);
        }
    }

    @Override
    public List<DataDef> getDefinedDataDefs() {
        return definedDataDefs;
    }

}
