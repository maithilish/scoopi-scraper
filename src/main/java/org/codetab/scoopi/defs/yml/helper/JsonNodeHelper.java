package org.codetab.scoopi.defs.yml.helper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonNodeHelper {

    /**
     * return list of items at specified path.
     *
     * <pre>
     * axis:
     *     col:
     *        members:
     *            member:
     *            member:
     *
     * path - /axis/col/members
     * item - member
     *
     * then return list of member
     *
     * </pre>
     *
     * @param node
     * @param path
     * @param item
     * @return
     */
    public List<JsonNode> findValues(final JsonNode node, final String path,
            final String item) {
        return node.at(path).findValues(item);
    }

    /**
     * find node with matching field
     *
     * <pre>
     * member: { name: price }
     * member: { name: high }
     *
     * then findField(nodeList, "name","price")
     * returns the first member which has field name with match price
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
     * @return
     */
    public List<String> getArrayAsStrings(final JsonNode node,
            final String arrayName) {
        List<String> values = new ArrayList<>();
        JsonNode array = null;
        if (arrayName == null) {
            array = node;
        } else {
            array = node.path(arrayName);
        }
        if (array.isArray()) {
            Iterator<JsonNode> it = array.iterator();
            while (it.hasNext()) {
                values.add(it.next().asText());
            }
        }
        return values;
    }
}
