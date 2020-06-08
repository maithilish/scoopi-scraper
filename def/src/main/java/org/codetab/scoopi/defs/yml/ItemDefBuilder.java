package org.codetab.scoopi.defs.yml;

import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.defs.IItemDefBuilder;
import org.codetab.scoopi.exception.DefNotFoundException;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Build ItemDefData from JsonNode. ItemDefData is serialized and stored in
 * IStore by DefBootstrap.
 * @author m
 *
 */
public class ItemDefBuilder implements IItemDefBuilder {

    @Inject
    private ItemDefs itemDefs;
    @Inject
    private ItemDefData itemDefData;

    @Override
    public byte[] serialize(final ItemDefData data) {
        return SerializationUtils.serialize(data);
    }

    @Override
    public ItemDefData deserialize(final byte[] data) {
        return SerializationUtils.deserialize(data);
    }

    @Override
    public ItemDefData buildData(final Object defs)
            throws DefNotFoundException {
        Validate.validState(defs instanceof JsonNode,
                "itemDefsNode is not JsonNode");
        JsonNode node = (JsonNode) defs;

        itemDefData.setQueryMap(itemDefs.getQueryMap(node));
        itemDefData.setItemAxisMap(itemDefs.getItemAxisMap(node));
        itemDefData.setDimAxisMap(itemDefs.getDimAxisMap(node));
        itemDefData.setFactAxisMap(itemDefs.getFactAxisMap(node));

        itemDefData.setDataTemplateMap(itemDefs.generateDataTemplates(
                itemDefData.getItemAxisMap(), itemDefData.getDimAxisMap(),
                itemDefData.getFactAxisMap()));
        Map<String, JsonNode> itemNodeMap = itemDefs.getItemNodeMap(node);
        itemDefData.setItemAttributeMap(
                itemDefs.getItemAttributeMap(node, itemNodeMap));

        return itemDefData;
    }
}
