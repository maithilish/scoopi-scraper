package org.codetab.scoopi.defs.yml;

import static org.codetab.scoopi.util.Util.spaceit;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.exception.ValidationException;
import org.codetab.scoopi.helper.IOHelper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

public class Yamls {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private ObjectMapper mapper;
    @Inject
    private IOHelper ioHelper;

    public List<JsonNode> loadYamls(final Collection<String> files)
            throws JsonProcessingException, IOException {
        List<JsonNode> nodes = new ArrayList<>();
        for (String file : files) {
            nodes.add(loadYaml(file));
        }
        return nodes;
    }

    public JsonNode loadYaml(final String file)
            throws JsonProcessingException, IOException {
        LOG.info("load defs {} ", file);
        try (InputStream ymlStream = ioHelper.getInputStream(file)) {
            JsonNode node = mapper.readTree(ymlStream);
            LOG.debug(spaceit(file, "loaded"));
            return node;
        }
    }

    public JsonNode mergeNodes(final List<JsonNode> nodesList) {
        LOG.info("merge defs");
        ObjectNode mergedNodes = mapper.createObjectNode();
        for (JsonNode nodes : nodesList) {
            Iterator<String> fieldNames = nodes.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                // existing node - if any or missing node
                JsonNode existingNode = mergedNodes.path(fieldName);

                // node copy to merge
                JsonNode node = nodes.path(fieldName);
                ObjectNode nodeCopy = node.deepCopy();

                if (existingNode.isMissingNode()) {
                    // add new field and node copy
                    mergedNodes.set(fieldName, nodeCopy);
                } else {
                    // add node copy to existing field
                    ObjectNode eNode = (ObjectNode) existingNode;
                    eNode.setAll(nodeCopy);
                }
            }
        }
        LOG.debug("defs merged");
        return mergedNodes;
    }

    public boolean validateSchema(final String schemaName,
            final InputStream schemaStream, final JsonNode node)
            throws ProcessingException, IOException, ValidationException {
        LOG.info("validate schema {}", schemaName);
        ObjectMapper jsonMapper = new ObjectMapper();
        JsonNode schemaNodes = jsonMapper.readTree(schemaStream);
        final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        final JsonSchema schema = factory.getJsonSchema(schemaNodes);
        ProcessingReport report = schema.validate(node);
        if (report.isSuccess()) {
            LOG.info("schema validation success");
        } else {
            LOG.error("schema validation failed");
            report.forEach(p -> LOG.error(pretty(p.asJson())));
            throw new ValidationException("invalid defs");
        }
        return true;
    }

    public String pretty(final JsonNode node) {
        try {
            return mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(node);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    public ObjectNode createEmptyObjectNode() {
        return mapper.createObjectNode();
    }

    public ObjectNode createObjectNode(final String name,
            final JsonNode defaultStepsCopy) {
        ObjectNode node = createEmptyObjectNode();
        node.set(name, defaultStepsCopy);
        return node;
    }

    public String toJson(final JsonNode node) throws JsonProcessingException {
        return mapper.writeValueAsString(node);
    }

    public JsonNode toJsonNode(final String json) throws IOException {
        return mapper.readTree(json);
    }
}
