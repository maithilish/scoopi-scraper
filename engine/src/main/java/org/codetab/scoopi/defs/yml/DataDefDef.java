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
import org.codetab.scoopi.persistence.DataDefPersistence;

import com.fasterxml.jackson.databind.JsonNode;

@Singleton
public class DataDefDef implements IDataDefDef {

    @Inject
    private DataDefDefs dataDefDefs;
    @Inject
    private DataDefPersistence dataDefPersistence;

    private Map<String, DataDef> dataDefMap;

    @Override
    public void init(final Object dataDefNodes) {

        Validate.validState(dataDefNodes instanceof JsonNode,
                "dataDefNodes is not JsonNode");

        try {
            JsonNode defs = (JsonNode) dataDefNodes;
            List<DataDef> definedDataDefs = dataDefDefs.createDataDefs(defs);
            List<DataDef> dataDefs;
            if (persist()) {
                dataDefs = updateDataDefs(definedDataDefs);
            } else {
                dataDefs = definedDataDefs;
            }
            dataDefDefs.setDefs(dataDefs);
            dataDefMap = dataDefDefs.toMap(dataDefs);

        } catch (IOException e) {
            throw new CriticalException("unable to create datadefs", e);
        }
    }

    private List<DataDef> updateDataDefs(final List<DataDef> definedDataDefs) {
        List<DataDef> updatedDataDefs;
        List<DataDef> loadedDataDefs = dataDefPersistence.loadDataDefs();
        boolean updates =
                dataDefDefs.markForUpdation(definedDataDefs, loadedDataDefs);
        if (updates) {
            dataDefPersistence.storeDataDefs(loadedDataDefs);
            updatedDataDefs = dataDefPersistence.loadDataDefs();
        } else {
            updatedDataDefs = loadedDataDefs;
        }
        return updatedDataDefs;
    }

    private boolean persist() {
        return dataDefPersistence.persistDataDef();
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

}
