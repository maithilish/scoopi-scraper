package org.codetab.scoopi.defs.yml.helper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

import javax.inject.Inject;

import org.codetab.scoopi.defs.yml.DefsNormalizer;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.ValidationException;
import org.codetab.scoopi.helper.IOHelper;
import org.codetab.scoopi.messages.Messages;
import org.codetab.scoopi.shared.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;

public class DefsHelper {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DefsHelper.class);

    @Inject
    private YamlHelper yamlHelper;
    @Inject
    private IOHelper ioHelper;
    @Inject
    private ConfigService configService;
    @Inject
    private DefsNormalizer defsNormalizer;

    public Collection<String> getDefsFiles()
            throws ConfigNotFoundException, IOException, URISyntaxException {
        String defsDir = configService.getConfig("scoopi.defs.dir"); //$NON-NLS-1$
        Collection<String> defsFiles =
                ioHelper.getFilesInDir(defsDir, new String[] {"yml", "yaml"});
        if (defsFiles.isEmpty()) {
            String message =
                    String.join(" ", "no definitions file found in", defsDir);
            throw new IllegalStateException(message);
        }
        return defsFiles;
    }

    public JsonNode loadDefinedDefs(final Collection<String> defsFiles)
            throws ConfigNotFoundException, IOException, URISyntaxException {
        LOGGER.info(Messages.getString("BeanService.0"), "BeanService"); //$NON-NLS-1$ //$NON-NLS-2$
        JsonNode defs = yamlHelper.loadYamls(defsFiles);
        LOGGER.debug(Messages.getString("BeanService.3"), "BeanService"); //$NON-NLS-1$ //$NON-NLS-2$
        return defs;
    }

    public JsonNode loadDefaultSteps()
            throws ConfigNotFoundException, IOException, URISyntaxException {
        LOGGER.info(Messages.getString("BeanService.0"), "BeanService"); //$NON-NLS-1$ //$NON-NLS-2$

        String defaultStepsFile =
                configService.getConfig("scoopi.defs.defaultSteps");

        JsonNode defaultSteps = yamlHelper.loadYaml(defaultStepsFile);

        LOGGER.debug(Messages.getString("BeanService.3"), "BeanService"); //$NON-NLS-1$ //$NON-NLS-2$

        return defaultSteps;
    }

    public void mergeDefaultSteps(final JsonNode defs,
            final JsonNode defaultSteps) {
        JsonNode defaultStepsCopy =
                defaultSteps.at("/steps/default").deepCopy();
        JsonNode steps = defs.at("/steps");
        if (steps.isMissingNode()) {
            ObjectNode node =
                    yamlHelper.createObjectNode("default", defaultStepsCopy);
            ((ObjectNode) defs).set("steps", node);
        } else {
            ((ObjectNode) steps).set("default", defaultStepsCopy);
        }
    }

    public void validateDefinedDefs(final JsonNode definedDefs)
            throws FileNotFoundException, ProcessingException, IOException,
            ConfigNotFoundException, ValidationException {
        LOGGER.info(Messages.getString("BeanService.0"), "BeanService"); //$NON-NLS-1$ //$NON-NLS-2$

        String schema = configService.getConfig("scoopi.defs.definedSchema"); //$NON-NLS-1$
        yamlHelper.validateSchema(schema, ioHelper.getInputStream(schema),
                definedDefs);

        LOGGER.debug(Messages.getString("BeanService.3"), "BeanService"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void validateEffectiveDefs(final JsonNode effectiveDefs)
            throws FileNotFoundException, ProcessingException, IOException,
            ConfigNotFoundException, ValidationException {
        LOGGER.info(Messages.getString("BeanService.0"), "BeanService"); //$NON-NLS-1$ //$NON-NLS-2$

        String schema = configService.getConfig("scoopi.defs.effectiveSchema"); //$NON-NLS-1$
        yamlHelper.validateSchema(schema, ioHelper.getInputStream(schema),
                effectiveDefs);

        LOGGER.debug(Messages.getString("BeanService.3"), "BeanService"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public JsonNode createEffectiveDefs(final JsonNode defs)
            throws IOException {
        JsonNode eDefs = defs.deepCopy();
        // !! don't change order of these methods !!
        defsNormalizer.addFactMember(eDefs);
        defsNormalizer.addMemberIndex(eDefs);
        defsNormalizer.addMemberOrder(eDefs);
        defsNormalizer.addNoQuery(eDefs);

        defsNormalizer.setDefaultSteps(eDefs);
        defsNormalizer.expandOverriddenSteps(eDefs);
        defsNormalizer.expandSteps(eDefs);
        return eDefs;
    }

    public String pretty(final JsonNode node) {
        return yamlHelper.pretty(node);
    }

}
