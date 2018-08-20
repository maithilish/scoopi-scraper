package org.codetab.scoopi.defs.yml.helper;

import java.util.NoSuchElementException;
import java.util.StringJoiner;

import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;

import com.fasterxml.jackson.databind.JsonNode;

public class QueryHelper {

    public String getQuery(final DataDef dataDef, final AxisName axisName,
            final String queryType) {
        Validate.validState(dataDef.getDef() instanceof JsonNode,
                "def is not JsonNode");

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
}
