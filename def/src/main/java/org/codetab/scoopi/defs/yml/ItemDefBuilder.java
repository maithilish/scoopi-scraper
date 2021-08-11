package org.codetab.scoopi.defs.yml;

import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.defs.IDefBuilder;
import org.codetab.scoopi.defs.IDefData;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.Data;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Build ItemDefData from JsonNode. ItemDefData is serialized and stored in
 * IStore by DefBootstrap.
 * @author m
 *
 */
public class ItemDefBuilder implements IDefBuilder {

    @Inject
    private ItemDefs itemDefs;
    @Inject
    private Factory factory;

    @Override
    public IDefData buildData(final Object defs) throws DefNotFoundException {
        Validate.validState(defs instanceof JsonNode,
                "itemDefsNode is not JsonNode");
        JsonNode node = (JsonNode) defs;

        ItemDefData itemDefData = factory.createItemDefData();
        itemDefData.setQueryMap(itemDefs.getQueryMap(node));
        itemDefData.setItemAxisMap(itemDefs.getItemAxisMap(node));
        itemDefData.setDimAxisMap(itemDefs.getDimAxisMap(node));
        itemDefData.setFactAxisMap(itemDefs.getFactAxisMap(node));

        Map<String, Data> dataTemplates = itemDefs.generateDataTemplates(
                itemDefData.getItemAxisMap(), itemDefData.getDimAxisMap(),
                itemDefData.getFactAxisMap());
        itemDefData.setDataTemplateMap(dataTemplates);
        Map<String, JsonNode> itemNodeMap = itemDefs.getItemNodeMap(node);
        itemDefData.setItemAttributeMap(
                itemDefs.getItemAttributeMap(node, itemNodeMap));

        itemDefs.traceDataTemplates(dataTemplates);

        return itemDefData;
    }
}
