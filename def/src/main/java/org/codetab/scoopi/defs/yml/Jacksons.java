package org.codetab.scoopi.defs.yml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.codetab.scoopi.exception.DefNotFoundException;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.gson.JsonParser;

public class Jacksons {

    public List<String> getFieldNames(final JsonNode jNode) {
        return Lists.newArrayList(jNode.fieldNames());
    }

    public List<String> getFieldNames(final JsonNode jNode,
            final String... pathParts) {
        String path = path(pathParts);
        return Lists.newArrayList(jNode.at(path).fieldNames());
    }

    public String getFieldValue(final JsonNode jNode,
            final String... fieldNames) throws DefNotFoundException {
        String path = path(fieldNames);
        JsonNode node = jNode.at(path);
        if (node.isMissingNode()) {
            throw new DefNotFoundException(path);
        } else {
            return node.asText();
        }
    }

    public String path(final String... parts) {
        StringJoiner joiner = new StringJoiner("/").add("");
        for (String part : parts) {
            joiner.add(part);
        }
        return joiner.toString();
    }

    /**
     * find node with matching field
     *
     * <pre>
     * item: { name: price }
     * item: { name: high }
     *
     * then findField(nodeList, "name","price")
     * returns the first item which has field name with match price
     * </pre>
     * <p>
     * </p>
     * @param nodes
     * @param field
     * @param match
     * @return
     */
    public Optional<JsonNode> findByField(final List<JsonNode> nodes,
            final String fieldName, final String match) {

        // if node is not found, jsonNode.path returns missing node and
        // asText() returns empty string which is safe to chain

        return nodes.stream().filter(node -> {
            return node.path(fieldName).asText().equals(match);
        }).findFirst();
    }

    /**
     * find array inside the node and return its content as list of string
     * @param node
     * @param arrayName,
     *            if null then node itself is the array else node is searched
     *            for named array
     * @return null if no such array
     */
    public List<String> getArrayAsStrings(final JsonNode node,
            final String arrayName) {
        List<String> values = null;
        JsonNode array = null;
        if (arrayName == null) {
            array = node;
        } else {
            array = node.path(arrayName);
        }
        if (array.isArray()) {
            values = new ArrayList<>();
            Iterator<JsonNode> it = array.iterator();
            while (it.hasNext()) {
                values.add(it.next().asText());
            }
        }
        return values;
    }

    public String parseJson(final String json) {
        return JsonParser.parseString(json).toString();
    }
}
