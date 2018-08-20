package org.codetab.scoopi.defs.yml;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.defs.IDataDefProvider;
import org.codetab.scoopi.defs.yml.helper.DataDefHelper;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.persistence.DataDefPersistence;
import org.codetab.scoopi.util.Util;

import com.fasterxml.jackson.databind.JsonNode;

@Singleton
public class DataDefProvider implements IDataDefProvider {

    @Inject
    private DataDefHelper dataDefHelper;
    @Inject
    private DataDefPersistence dataDefPersistence;

    private JsonNode defs;

    private Map<String, DataDef> dataDefMap;
    private Map<String, Data> dataTemplateMap = new HashMap<>();

    private boolean consistent = false;

    /**
     * on first invoke sets defs and subsequent invocations do nothing.
     * @param defs
     */
    public void init(final JsonNode dataDefDefs) {

        requireNonNull(dataDefDefs, "dataDefDefs must not be null");

        if (consistent) {
            return;
        }

        try {
            defs = dataDefDefs;
            List<DataDef> definedDataDefs = dataDefHelper.createDataDefs(defs);
            List<DataDef> dataDefs;
            if (persist()) {
                dataDefs = updateDataDefs(definedDataDefs);
            } else {
                dataDefs = definedDataDefs;
            }
            dataDefHelper.setDefs(dataDefs);
            dataDefMap = dataDefHelper.toMap(dataDefs);
            createAndCacheDataTemplate();
            consistent = true;
        } catch (IOException e) {
            throw new CriticalException("unable to create datadefs", e);
        }
    }

    private List<DataDef> updateDataDefs(final List<DataDef> definedDataDefs) {
        List<DataDef> updatedDataDefs;
        List<DataDef> loadedDataDefs = dataDefPersistence.loadDataDefs();
        boolean updates =
                dataDefHelper.markForUpdation(definedDataDefs, loadedDataDefs);
        if (updates) {
            dataDefPersistence.storeDataDefs(loadedDataDefs);
            updatedDataDefs = dataDefPersistence.loadDataDefs();
        } else {
            updatedDataDefs = loadedDataDefs;
        }
        return updatedDataDefs;
    }

    private void createAndCacheDataTemplate() {
        for (DataDef dataDef : dataDefMap.values()) {
            List<Set<Axis>> axisSets = dataDefHelper.getAxisSets(dataDef);
            Data data = dataDefHelper.getData(dataDef, axisSets);
            dataTemplateMap.put(dataDef.getName(), data);
        }
    }

    private boolean persist() {
        return dataDefPersistence.persistDataDef();
    }

    // accessor methods
    @Override
    public DataDef getDataDef(final String name)
            throws DataDefNotFoundException {
        DataDef dataDef = dataDefMap.get(name);
        if (nonNull(dataDef)) {
            return dataDef;
        } else {
            throw new DataDefNotFoundException(name);
        }
    }

    @Override
    public Long getDataDefId(final String name)
            throws DataDefNotFoundException {
        return getDataDef(name).getId();
    }

    @Override
    public Data getDataTemplate(final String dataDef) {
        requireNonNull(dataDef, "dataDefName must not be null");

        Data data = dataTemplateMap.get(dataDef);
        if (nonNull(data)) {
            return data.copy();
        } else {
            throw new NoSuchElementException(
                    Util.join("data template for datadef: ", dataDef));
        }
    }
}
