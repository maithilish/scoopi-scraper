package org.codetab.scoopi.defs.yml.helper;

import static org.apache.commons.lang3.Validate.validState;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.StringJoiner;

import javax.inject.Inject;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.util.Util;

import com.fasterxml.jackson.databind.JsonNode;

public class AxisDefsHelper {

    @Inject
    private JsonNodeHelper jsonNodeHelper;

    public String getQuery(final DataDef dataDef, final AxisName axisName,
            final String queryType) {
        validState(dataDef.getDef() instanceof JsonNode, "def is not JsonNode");

        JsonNode def = (JsonNode) dataDef.getDef();
        String path = String.join("/", "", "axis",
                axisName.toString().toLowerCase(), "query", queryType);
        JsonNode query = def.at(path);
        if (query.isMissingNode()) {
            String key = new StringJoiner(":", "[", "]").add(dataDef.getName())
                    .add(axisName.toString()).add(queryType).toString();
            throw new NoSuchElementException(
                    String.join(" ", "query not found for", key));
        } else {
            return query.asText();
        }
    }

    public Optional<List<String>> getBreakAfters(final DataDef dataDef,
            final Axis axis) {
        validState(dataDef.getDef() instanceof JsonNode, "def is not JsonNode");

        String path =
                String.join("/", "", "axis", axis.getNameString(), "members");

        JsonNode def = (JsonNode) dataDef.getDef();
        List<JsonNode> jMemberList =
                jsonNodeHelper.findValues(def, path, "member");
        Optional<JsonNode> jMember = jsonNodeHelper.findByField(jMemberList,
                "name", axis.getMemberName());

        Optional<List<String>> breakAfters = Optional.empty();
        if (jMember.isPresent()) {
            List<String> breakAfterList = jsonNodeHelper
                    .getArrayAsStrings(jMember.get(), "breakAfter");
            breakAfters = Optional
                    .ofNullable(Collections.unmodifiableList(breakAfterList));
        }
        return breakAfters;
    }

    public Optional<Range<Integer>> getIndexRange(final DataDef dataDef,
            final Axis axis) {
        validState(dataDef.getDef() instanceof JsonNode, "def is not JsonNode");

        String path =
                String.join("/", "", "axis", axis.getNameString(), "members");

        JsonNode def = (JsonNode) dataDef.getDef();
        List<JsonNode> jMemberList =
                jsonNodeHelper.findValues(def, path, "member");
        Optional<JsonNode> jMember = jsonNodeHelper.findByField(jMemberList,
                "name", axis.getMemberName());

        Optional<Range<Integer>> indexRange = Optional.empty();
        if (jMember.isPresent()) {
            String value = jMember.get().path("indexRange").asText();
            if (StringUtils.isNotBlank(value)) {
                indexRange = Optional.ofNullable(Util.getRange(value));
            }
        }
        return indexRange;
    }

    public Optional<List<String>> getPrefixes(final DataDef dataDef,
            final AxisName axisName) {
        validState(dataDef.getDef() instanceof JsonNode, "def is not JsonNode");

        JsonNode def = (JsonNode) dataDef.getDef();
        String path = String.join("/", "", "axis",
                axisName.toString().toLowerCase(), "prefix");
        JsonNode jPrefixes = def.at(path);

        Optional<List<String>> prefix = null;
        if (jPrefixes.isMissingNode()) {
            prefix = Optional.empty();
        } else {
            // jPrefixes is the array so arrayName is null
            List<String> prefixList =
                    jsonNodeHelper.getArrayAsStrings(jPrefixes, null);
            prefix = Optional
                    .ofNullable(Collections.unmodifiableList(prefixList));
        }
        return prefix;
    }
}
