package org.codetab.scoopi.defs.yml;

import static org.apache.commons.lang3.Validate.notNull;
import static org.codetab.scoopi.util.Util.LINE;
import static org.codetab.scoopi.util.Util.dashit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import org.apache.commons.lang3.time.DateUtils;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.system.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

class DataDefDefs {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(DataDefDefs.class);

    @Inject
    private ConfigService configService;
    @Inject
    private ObjectFactory objectFactory;
    @Inject
    private Yamls yamls;

    /**
     * Construct DataDefs from JsonNode.
     * @param JsonNode
     *            defs
     * @return list of DataDef
     * @throws JsonProcessingException
     */
    public List<DataDef> createDataDefs(final JsonNode defs)
            throws JsonProcessingException {
        List<DataDef> dataDefs = new ArrayList<>();
        Iterator<Entry<String, JsonNode>> entries = defs.fields();
        while (entries.hasNext()) {
            Entry<String, JsonNode> entry = entries.next();
            String dataDefName = entry.getKey();
            JsonNode jDataDef = entry.getValue();
            String defJson = yamls.toJson(jDataDef);
            Date fromDate = configService.getRunDateTime();
            Date toDate = configService.getHighDate();
            DataDef dataDef = objectFactory.createDataDef(dataDefName, fromDate,
                    toDate, defJson);
            dataDefs.add(dataDef);
        }
        return dataDefs;
    }

    /**
     * <p>
     * Compares active and new list of datadef and if any active datadef has a
     * new datadef version, then the active datadef highDate is reset to
     * runDateTime and new datadef is added to active list. For any new datadef,
     * there is no active datadef, then the new datadef is added active list.
     * Later updated active list is persisted to store.
     * @param dataDefs
     *            active list - existing active datadefs, not null
     * @param newDataDefs
     *            list of new datadefs, not null
     * @return true if active list is modified
     */
    public boolean markForUpdation(final List<DataDef> newDataDefs,
            final List<DataDef> oldDataDefs) {
        notNull(newDataDefs, "newDataDefs must not be null");
        notNull(oldDataDefs, "oldDataDefs must not be null");

        boolean updates = false;
        for (DataDef newDataDef : newDataDefs) {
            String name = newDataDef.getName();
            String message = null;
            try {
                DataDef oldDataDef = oldDataDefs.stream()
                        .filter(e -> e.getName().equals(name)).findFirst()
                        .get();
                if (oldDataDef.equalsForDef(newDataDef)) {
                    // no change
                    message = "no changes";
                } else {
                    // changed - update old and insert changed
                    message = "changed, insert new version";
                    updates = true;
                    Date toDate = DateUtils
                            .addSeconds(configService.getRunDateTime(), -1);
                    oldDataDef.setToDate(toDate);
                    oldDataDefs.add(newDataDef);
                }
            } catch (NoSuchElementException e) {
                // not exists - add new
                message = "not in store, insert new version";
                updates = true;
                oldDataDefs.add(newDataDef);
            }
            LOGGER.info("dataDef: {}, {}", name, message);
        }
        return updates;
    }

    /**
     * Convert defJson string to JsonNode and assign it to DataDef.def field.
     * @param list
     *            of dataDefs
     * @throws IOException
     */
    public void setDefs(final List<DataDef> dataDefs) throws IOException {
        for (DataDef dataDef : dataDefs) {
            String json = dataDef.getDefJson();
            JsonNode def = yamls.toJsonNode(json);
            dataDef.setDef(def);
        }
    }

    public Map<String, DataDef> toMap(final List<DataDef> dataDefs) {
        Map<String, DataDef> dataDefMap = new HashMap<>();
        for (DataDef dataDef : dataDefs) {
            dataDefMap.put(dataDef.getName(), dataDef);
        }
        return dataDefMap;
    }

    public void traceDataDefs(final Map<String, DataDef> dataDefMap,
            final Map<String, Data> dataTemplateMap) {
        LOGGER.trace("--- datadefs and data templates ---");
        for (String dataDefName : dataDefMap.keySet()) {
            String markerName = dashit("datadef", dataDefName);
            Marker marker = MarkerFactory.getMarker(markerName);
            DataDef dataDef = dataDefMap.get(dataDefName);
            Data dataTemplate = dataTemplateMap.get(dataDefName);
            LOGGER.trace(marker, "{}{}{}", dataDef, LINE, dataDef.getDefJson());
            LOGGER.trace(marker, "data template:{}{}", LINE,
                    dataTemplate.toTraceString());
        }
    }
}
