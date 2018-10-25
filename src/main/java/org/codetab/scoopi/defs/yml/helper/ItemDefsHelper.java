package org.codetab.scoopi.defs.yml.helper;

import static org.apache.commons.lang3.Validate.validState;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringJoiner;

import org.assertj.core.util.Lists;
import org.codetab.scoopi.model.DataDef;

import com.fasterxml.jackson.databind.JsonNode;

public class ItemDefsHelper {

    public String getRegionQuery(final DataDef dataDef, final String name) {
        validState(dataDef.getDef() instanceof JsonNode, "def is not JsonNode");

        JsonNode def = (JsonNode) dataDef.getDef();
        String path = String.join("/", "", "items", name, "region");
        JsonNode query = def.at(path);
        if (query.isMissingNode()) {
            String key = new StringJoiner(":", "[", "]").add(dataDef.getName())
                    .add(name).toString();
            throw new NoSuchElementException(String.join(" ",
                    "dataDef items, region query not found", key));
        } else {
            return query.asText();
        }
    }

    public String getFieldQuery(final DataDef dataDef, final String name,
            final String fieldName) {
        validState(dataDef.getDef() instanceof JsonNode, "def is not JsonNode");

        JsonNode def = (JsonNode) dataDef.getDef();
        String path = String.join("/", "", "items", name, "fields", fieldName);
        JsonNode field = def.at(path);
        if (field.isMissingNode()) {
            String key = new StringJoiner(":", "[", "]").add(dataDef.getName())
                    .add(name).add(fieldName).toString();
            throw new NoSuchElementException(
                    String.join(" ", "dataDef items, field not found", key));
        } else {
            return field.asText();
        }
    }

    public List<String> getFieldNames(final DataDef dataDef,
            final String name) {
        validState(dataDef.getDef() instanceof JsonNode, "def is not JsonNode");

        JsonNode def = (JsonNode) dataDef.getDef();
        String path = String.join("/", "", "items", name, "fields");
        JsonNode fields = def.at(path);
        if (fields.isMissingNode()) {
            String key = new StringJoiner(":", "[", "]").add(dataDef.getName())
                    .add(name).toString();
            throw new NoSuchElementException(
                    String.join(" ", "dataDef items, fields not found", key));
        } else {
            return Lists.newArrayList(fields.fieldNames());
        }
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
