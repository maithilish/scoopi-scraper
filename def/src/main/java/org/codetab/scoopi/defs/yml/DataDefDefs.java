package org.codetab.scoopi.defs.yml;

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

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.ObjectFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

class DataDefDefs {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private Configs configs;
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
            Date fromDate = configs.getRunDateTime();
            Date toDate = configs.getHighDate();
            DataDef dataDef = objectFactory.createDataDef(dataDefName, fromDate,
                    toDate, defJson);
            dataDefs.add(dataDef);
        }
        return dataDefs;
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

    public void traceDataDef(final List<DataDef> dataDefs) {
        for (DataDef dataDef : dataDefs) {
            String markerName = dashit("datadef", dataDef.getName());
            Marker marker = MarkerManager.getMarker(markerName);
            LOG.trace(marker, "datadef: {}{}{}{}", dataDef.getName(), LINE,
                    dataDef.getDefJson());
        }
    }
}
