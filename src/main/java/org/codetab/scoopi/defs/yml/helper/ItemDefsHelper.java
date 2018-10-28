package org.codetab.scoopi.defs.yml.helper;

import static org.apache.commons.lang3.Validate.validState;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.StringJoiner;

import javax.inject.Inject;

import org.codetab.scoopi.model.DataDef;

import com.fasterxml.jackson.databind.JsonNode;

public class ItemDefsHelper {

    @Inject
    private JsonNodeHelper jsonNodeHelper;

    public String getRegionQuery(final DataDef dataDef,
            final String itemsName) {
        validState(dataDef.getDef() instanceof JsonNode, "def is not JsonNode");

        JsonNode def = (JsonNode) dataDef.getDef();
        String path = String.join("/", "", "items", itemsName, "region");
        JsonNode query = def.at(path);
        if (query.isMissingNode()) {
            String key = new StringJoiner(":", "[", "]").add(dataDef.getName())
                    .add(itemsName).toString();
            throw new NoSuchElementException(String.join(" ",
                    "dataDef items, region query not found", key));
        } else {
            return query.asText();
        }
    }

    public String getItemQuery(final DataDef dataDef, final String itemsName,
            final String itemName) {
        validState(dataDef.getDef() instanceof JsonNode, "def is not JsonNode");

        JsonNode def = (JsonNode) dataDef.getDef();
        String path = String.join("/", "", "items", itemsName, "items");
        JsonNode items = def.at(path);
        List<JsonNode> itemList = items.findValues("item");
        Optional<JsonNode> item =
                jsonNodeHelper.findByField(itemList, "name", itemName);
        String key = new StringJoiner(":", "[", "]").add(dataDef.getName())
                .add(itemsName).add(itemName).toString();
        if (item.isPresent()) {
            JsonNode selector = item.get().path("selector");
            if (selector.isMissingNode()) {
                throw new NoSuchElementException(
                        String.join(" ", "dataDef items, item selector", key));
            } else {
                return selector.asText();
            }
        } else {
            throw new NoSuchElementException(
                    String.join(" ", "dataDef items, child items", key));
        }
    }

    public List<String> getItemNames(final DataDef dataDef,
            final String itemsName) {
        validState(dataDef.getDef() instanceof JsonNode, "def is not JsonNode");

        JsonNode def = (JsonNode) dataDef.getDef();
        String path = String.join("/", "", "items", itemsName, "items");
        JsonNode items = def.at(path);
        List<JsonNode> itemList = items.findValues("item");
        if (itemList.isEmpty()) {
            String key = new StringJoiner(":", "[", "]").add(dataDef.getName())
                    .add(itemsName).toString();
            throw new NoSuchElementException(String.join(" ",
                    "dataDef items, child items not found", key));
        }
        List<String> itemNames = new ArrayList<>();
        for (JsonNode item : itemList) {
            itemNames.addAll(item.findValuesAsText("name"));
        }
        return itemNames;

    }

    // public Optional<List<String>> getBreakAfters(final DataDef dataDef,
    // final String name) {
    // validState(dataDef.getDef() instanceof JsonNode, "def is not JsonNode");
    //
    // JsonNode def = (JsonNode) dataDef.getDef();
    // String path = String.join("/", "", "items", name);
    // JsonNode item = def.at(path);
    //
    // Optional<List<String>> breakAfters = Optional.empty();
    // if (!item.isMissingNode()) {
    // List<String> breakAfterList =
    // jsonNodeHelper.getArrayAsStrings(item, "breakAfter");
    // if (nonNull(breakAfterList)) {
    // breakAfters = Optional.ofNullable(
    // Collections.unmodifiableList(breakAfterList));
    // }
    // }
    // return breakAfters;
    // }
    //
    // public Optional<Range<Integer>> getIndexRange(final DataDef dataDef,
    // final String name) {
    // validState(dataDef.getDef() instanceof JsonNode, "def is not JsonNode");
    //
    // JsonNode def = (JsonNode) dataDef.getDef();
    // String path = String.join("/", "", "items", name);
    // JsonNode item = def.at(path);
    //
    // Optional<Range<Integer>> indexRange = Optional.empty();
    // if (!item.isMissingNode()) {
    // String value = item.path("indexRange").asText();
    // if (StringUtils.isNotBlank(value)) {
    // // TODO - extract Util.getRange() to separate class
    // indexRange = Optional.ofNullable(Util.getRange(value));
    // }
    // }
    // return indexRange;
    // }

}
