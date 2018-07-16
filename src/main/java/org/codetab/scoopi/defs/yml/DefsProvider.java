package org.codetab.scoopi.defs.yml;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.defs.IDefsProvider;
import org.codetab.scoopi.defs.yml.helper.DefsHelper;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.messages.Messages;
import org.codetab.scoopi.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.google.common.collect.Lists;

@Singleton
public class DefsProvider implements IDefsProvider {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DefsProvider.class);

    @Inject
    private DefsHelper defsHelper;
    @Inject
    private LocatorProvider locatorProvider;
    @Inject
    private DataDefProvider dataDefProvider;
    @Inject
    private TaskProvider taskProvider;

    private JsonNode definedDefs;
    private JsonNode effectiveDefs;

    @Override
    public void init() {
        try {
            definedDefs = defsHelper.loadDefinedDefs();
            JsonNode defaultSteps = defsHelper.loadDefaultSteps();
            defsHelper.mergeDefaultSteps(definedDefs, defaultSteps);
            defsHelper.validateDefinedDefs(definedDefs);
            effectiveDefs = defsHelper.createEffectiveDefs(definedDefs);
            defsHelper.validateEffectiveDefs(effectiveDefs);

            LOGGER.info("defined defs {}", defsHelper.pretty(definedDefs));
            LOGGER.info("effective defs {}", defsHelper.pretty(effectiveDefs));

        } catch (ConfigNotFoundException | IOException | URISyntaxException
                | ProcessingException | NoSuchElementException e) {
            throw new CriticalException(Messages.getString("BeanService.2"), //$NON-NLS-1$
                    e);
        }
    }

    @Override
    public void initProviders() {
        ArrayList<Entry<String, JsonNode>> defsMap =
                Lists.newArrayList(effectiveDefs.fields());

        locatorProvider.init(getDefs("locatorGroups", defsMap));
        dataDefProvider.init(getDefs("dataDefs", defsMap));
        taskProvider.init(getDefs("taskGroups", defsMap));
    }

    private JsonNode getDefs(final String key,
            final ArrayList<Entry<String, JsonNode>> defsMap) {
        Optional<Entry<String, JsonNode>> entry = defsMap.stream()
                .filter(e -> e.getKey().equals(key)).findFirst();
        if (entry.isPresent()) {
            return entry.get().getValue();
        } else {
            throw new CriticalException(
                    Util.join("unable to initialize def providers, ", key,
                            " is not defined"));
        }
    }
}
