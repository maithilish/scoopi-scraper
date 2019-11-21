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

import org.codetab.scoopi.config.ConfigService;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.ObjectFactory;
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
