package org.codetab.scoopi.defs.yml.helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.codetab.scoopi.exception.ValidationException;
import org.codetab.scoopi.helper.IOHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

public class YamlHelper {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(YamlHelper.class);

    @Inject
    private ObjectMapper mapper;
    @Inject
    private IOHelper ioHelper;

    public JsonNode loadYamls(final Collection<String> files)
            throws JsonProcessingException, IOException {
        List<JsonNode> nodes = new ArrayList<>();
        for (String file : files) {
            nodes.add(loadYaml(file));
        }
        return mergeNodes(nodes);
    }

    public JsonNode loadYaml(final String file)
            throws JsonProcessingException, IOException {
        LOGGER.info("load defs {} ", file);
        InputStream ymlStream = ioHelper.getInputStream(file);
        return mapper.readTree(ymlStream);
    }

    public JsonNode mergeNodes(final List<JsonNode> nodes) {
        LOGGER.info("merge defs");
        ObjectNode mNode = (ObjectNode) mapper.createObjectNode();
        for (JsonNode node : nodes) {
            ObjectNode copy = node.deepCopy();
            mNode.setAll(copy);
        }
        LOGGER.debug("merged defs");
        return mNode;
    }

    public boolean validateSchema(final String schemaName,
            final InputStream schemaStream, final JsonNode node)
            throws ProcessingException, IOException, ValidationException {
        LOGGER.info("validate schema {}", schemaName);
        ObjectMapper jsonMapper = new ObjectMapper();
        JsonNode schemaNodes = jsonMapper.readTree(schemaStream);
        final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        final JsonSchema schema = factory.getJsonSchema(schemaNodes);
        ProcessingReport report = schema.validate(node);
        if (report.isSuccess()) {
            LOGGER.info("schema validation success");
        } else {
            LOGGER.error("schema validation failed");
            report.forEach(p -> LOGGER.error(pretty(p.asJson())));
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
}
