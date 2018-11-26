package org.codetab.scoopi.defs.yml;

import static org.codetab.scoopi.util.Util.spaceit;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.ValidationException;
import org.codetab.scoopi.helper.IOHelper;
import org.codetab.scoopi.system.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;

class Defs {

    private static final Logger LOGGER = LoggerFactory.getLogger(Defs.class);

    @Inject
    private Yamls yamls;
    @Inject
    private IOHelper ioHelper;
    @Inject
    private ConfigService configService;
    @Inject
    private Normalizer normalizer;

    public Collection<String> getDefsFiles()
            throws ConfigNotFoundException, IOException, URISyntaxException {
        String defsDir = configService.getConfig("scoopi.defs.dir"); //$NON-NLS-1$
        Collection<String> defsFiles =
                ioHelper.getFilesInDir(defsDir, new String[] {"yml", "yaml"});
        if (defsFiles.isEmpty()) {
            String message = spaceit("no definitions file found in", defsDir);
            throw new IllegalStateException(message);
        }
        return defsFiles;
    }

    public JsonNode loadDefinedDefs(final Collection<String> defsFiles)
            throws ConfigNotFoundException, IOException, URISyntaxException {
        LOGGER.info("load defined defs");
        List<JsonNode> nodesList = yamls.loadYamls(defsFiles);
        JsonNode defs = yamls.mergeNodes(nodesList);
        LOGGER.debug("defined defs loaded");
        return defs;
    }

    public JsonNode loadDefaultSteps()
            throws ConfigNotFoundException, IOException, URISyntaxException {
        LOGGER.info("load default steps");

        String defaultStepsFile =
                configService.getConfig("scoopi.defs.defaultStepsFile");

        JsonNode defaultSteps = yamls.loadYaml(defaultStepsFile);

        LOGGER.debug("default steps loaded");

        return defaultSteps;
    }

    public void mergeDefaultSteps(final JsonNode defs,
            final JsonNode defaultSteps) {
        Iterator<Entry<String, JsonNode>> it =
                defaultSteps.at("/steps").fields();
        while (it.hasNext()) {
            Entry<String, JsonNode> entry = it.next();
            String stepsName = entry.getKey();
            JsonNode stepsCopy = entry.getValue().deepCopy();
            // add it to defs steps node
            JsonNode defsSteps = defs.at("/steps");
            if (defsSteps.isMissingNode()) {
                ObjectNode node = yamls.createObjectNode(stepsName, stepsCopy);
                ((ObjectNode) defs).set("steps", node);
            } else {
                ((ObjectNode) defsSteps).set(stepsName, stepsCopy);
            }
        }
    }

    public void validateDefinedDefs(final JsonNode definedDefs)
            throws FileNotFoundException, ProcessingException, IOException,
            ConfigNotFoundException, ValidationException {
        LOGGER.info("validate defined defs");

        String schema = configService.getConfig("scoopi.defs.definedSchema"); //$NON-NLS-1$

        try (InputStream schemaStream = ioHelper.getInputStream(schema)) {
            yamls.validateSchema(schema, schemaStream, definedDefs);
        }
        LOGGER.debug("defined defs validated");
    }

    public void validateEffectiveDefs(final JsonNode effectiveDefs)
            throws FileNotFoundException, ProcessingException, IOException,
            ConfigNotFoundException, ValidationException {
        LOGGER.info("validate effective defs");

        String schema = configService.getConfig("scoopi.defs.effectiveSchema"); //$NON-NLS-1$
        yamls.validateSchema(schema, ioHelper.getInputStream(schema),
                effectiveDefs);

        LOGGER.debug("effectvie defs validated");
    }

    public JsonNode createEffectiveDefs(final JsonNode defs)
            throws IOException, DefNotFoundException {
        LOGGER.info("create effective defs");

        String defaultStepsName;
        try {
            defaultStepsName =
                    configService.getConfig("scoopi.defs.defaultSteps");
        } catch (ConfigNotFoundException e) {
            defaultStepsName = "jsoupDefault";
        }

        JsonNode eDefs = defs.deepCopy();
        // !! don't change order of these methods !!
        normalizer.addFactItem(eDefs);
        normalizer.addItemIndex(eDefs);
        normalizer.addItemOrder(eDefs);
        // defsNormalizer.addNoQuery(eDefs);

        // recursively expand top level steps
        normalizer.expandSteps(eDefs);

        // expand task steps
        normalizer.setDefaultSteps(eDefs, defaultStepsName);
        normalizer.expandTaskSteps(eDefs);

        LOGGER.debug("effectvie defs created");

        return eDefs;
    }

    public String pretty(final JsonNode node) {
        return yamls.pretty(node);
    }

}
