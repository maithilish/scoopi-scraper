package org.codetab.scoopi.defs.yml.helper;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.commons.lang3.Validate.validState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.util.Lists;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.Member;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.system.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;

public class DataDefHelper {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(DataDefHelper.class);

    @Inject
    private ConfigService configService;
    @Inject
    private ObjectFactory objectFactory;
    @Inject
    private YamlHelper yamlHelper;

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
        ArrayList<String> names = Lists.newArrayList(defs.fieldNames());
        for (String name : names) {
            String path = String.join("/", "", name);
            JsonNode dataDefNode = defs.at(path);
            String defJson = yamlHelper.toJson(dataDefNode);
            Date fromDate = configService.getRunDateTime();
            Date toDate = configService.getHighDate();
            DataDef dataDef = objectFactory.createDataDef(name, fromDate,
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
            JsonNode def = yamlHelper.toJsonNode(json);
            dataDef.setDef(def);
        }
    }

    /**
     * Members/member in each axis in datadef is converted to Axis and added to
     * set.
     * <p>
     * For example, for
     * </p>
     *
     * <pre>
     * col
     *    members: [
     *        member: {name: date, index: 0},
     *    ]
     * row
     *    members: [
     *        member: {name: price, index: 0},
     *        member: {name: high, index: 1}
     *    ]
     * </pre>
     * <p>
     * returns list of two sets
     * </p>
     *
     * <pre>
     * [name: date, index: 0]
     * and
     * [name: price, index: 0], [name: high, index: 1]
     * </pre>
     *
     * @param dataDef
     * @return list of Set<Axis>
     */
    public List<Set<Axis>> getAxisSets(final DataDef dataDef) {

        validState(dataDef.getDef() instanceof JsonNode, "def is not JsonNode");

        JsonNode def = (JsonNode) dataDef.getDef();
        String path = String.join("/", "", "axis");
        JsonNode jAxes = def.at(path);
        List<Set<Axis>> axisSets = new ArrayList<>();
        for (String jAxisName : Lists.newArrayList(jAxes.fieldNames())) {
            AxisName axisName = AxisName.valueOf(jAxisName.toUpperCase());
            path = String.join("/", "", jAxisName);
            JsonNode jAxis = jAxes.at(path);
            List<JsonNode> jMembers = jAxis.findValues("member");
            Set<Axis> axisSet = new HashSet<>();
            for (JsonNode jMember : jMembers) {
                JsonNode memberName = jMember.get("name");
                JsonNode value = jMember.get("value");
                JsonNode match = jMember.get("match");
                JsonNode index = jMember.get("index");
                JsonNode order = jMember.get("order");

                Axis axis = null;
                if (nonNull(memberName)) {
                    axis = objectFactory.createAxis(axisName,
                            memberName.asText());
                } else {
                    String message = String.join(" ",
                            "unable to create datadef:", dataDef.getName(),
                            "- member name not defined");
                    throw new CriticalException(message);
                }
                if (nonNull(value)) {
                    axis.setValue(value.asText());
                }
                if (nonNull(match)) {
                    axis.setMatch(match.asText());
                }
                if (nonNull(index)) {
                    axis.setIndex(index.asInt());
                }
                if (nonNull(order)) {
                    axis.setOrder(order.asInt());
                }
                axisSet.add(axis);
            }
            axisSets.add(axisSet);
        }
        return axisSets;
    }

    public Data getData(final DataDef dataDef, final List<Set<Axis>> axisSets) {
        Data data = objectFactory.createData("price");
        Set<List<Axis>> axesSets = Sets.cartesianProduct(axisSets);
        for (List<Axis> axes : axesSets) {
            Member member = objectFactory.createMember();
            member.getAxes().addAll(axes);
            data.getMembers().add(member);
        }
        return data;
    }

    public Map<String, DataDef> toMap(final List<DataDef> dataDefs) {
        Map<String, DataDef> dataDefMap = new HashMap<>();
        for (DataDef dataDef : dataDefs) {
            dataDefMap.put(dataDef.getName(), dataDef);
        }
        return dataDefMap;
    }
}
