package org.codetab.scoopi.defs.yml.helper;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.Validate.validState;

import java.util.ArrayList;
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
        String path = String.join("/", "", dataDef.getName(), "axis",
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

    public List<String> getBreakAfters(final DataDef dataDef, final Axis axis) {
        validState(dataDef.getDef() instanceof JsonNode, "def is not JsonNode");

        String path = String.join("/", "", dataDef.getName(), "axis",
                axis.getNameString(), "members");

        JsonNode def = (JsonNode) dataDef.getDef();
        List<JsonNode> jMemberList =
                jsonNodeHelper.findValues(def, path, "member");
        Optional<JsonNode> jMember = jsonNodeHelper.findByField(jMemberList,
                "name", axis.getMemberName());

        List<String> breakAfters;
        if (jMember.isPresent()) {
            breakAfters = jsonNodeHelper.getArrayAsStrings(jMember.get(),
                    "breakAfter");
        } else {
            breakAfters = new ArrayList<>();
        }
        return breakAfters;
    }

    public Range<Integer> getIndexRange(final DataDef dataDef,
            final Axis axis) {
        validState(dataDef.getDef() instanceof JsonNode, "def is not JsonNode");

        String path = String.join("/", "", dataDef.getName(), "axis",
                axis.getNameString(), "members");

        JsonNode def = (JsonNode) dataDef.getDef();
        List<JsonNode> jMemberList =
                jsonNodeHelper.findValues(def, path, "member");
        Optional<JsonNode> jMember = jsonNodeHelper.findByField(jMemberList,
                "name", axis.getMemberName());

        Range<Integer> indexRange = null;
        if (jMember.isPresent()) {
            String value = jMember.get().path("indexRange").asText();
            if (StringUtils.isNotBlank(value)) {
                indexRange = Util.getRange(value);
            }
        }
        if (isNull(indexRange)) {
            throw new NoSuchElementException("indexRange not defined");
        }
        return indexRange;
    }

}
