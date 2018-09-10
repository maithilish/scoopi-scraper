package org.codetab.scoopi.defs.yml;

import static java.util.Objects.isNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.defs.IDefs;
import org.codetab.scoopi.defs.yml.helper.DefsHelper;
import org.codetab.scoopi.exception.CriticalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

@Singleton
public class Defs implements IDefs {

    private static final Logger LOGGER = LoggerFactory.getLogger(Defs.class);

    @Inject
    private DefsHelper defsHelper;
    @Inject
    private LocatorDefs locatorDefs;
    @Inject
    private DataDefDefs dataDefDefs;
    @Inject
    private TaskDefs taskDefs;

    private JsonNode definedDefs;
    private JsonNode effectiveDefs;
    private Collection<String> defsFiles;

    @Override
    public void init() {
        try {
            if (isNull(defsFiles)) {
                defsFiles = defsHelper.getDefsFiles();
            }
            definedDefs = defsHelper.loadDefinedDefs(defsFiles);
            JsonNode defaultSteps = defsHelper.loadDefaultSteps();
            defsHelper.mergeDefaultSteps(definedDefs, defaultSteps);
            LOGGER.debug("defined defs {}", defsHelper.pretty(definedDefs));
            defsHelper.validateDefinedDefs(definedDefs);

            effectiveDefs = defsHelper.createEffectiveDefs(definedDefs);
            LOGGER.debug("effective defs {}", defsHelper.pretty(effectiveDefs));
            defsHelper.validateEffectiveDefs(effectiveDefs);
        } catch (Exception e) {
            throw new CriticalException("initialize defs", e);
        }
    }

    @Override
    public void initDefProviders() {
        ArrayList<Entry<String, JsonNode>> defsMap =
                Lists.newArrayList(effectiveDefs.fields());

        locatorDefs.init(getDefs("locatorGroups", defsMap));
        dataDefDefs.init(getDefs("dataDefs", defsMap));
        taskDefs.init(getDefs("taskGroups", defsMap));
    }

    @Override
    public void setDefsFiles(final Collection<String> defsFiles) {
        this.defsFiles = defsFiles;
    }

    private JsonNode getDefs(final String key,
            final ArrayList<Entry<String, JsonNode>> defsMap) {
        Optional<Entry<String, JsonNode>> entry = defsMap.stream()
                .filter(e -> e.getKey().equals(key)).findFirst();
        if (entry.isPresent()) {
            return entry.get().getValue();
        } else {
            throw new CriticalException(String.join(" ", "initialize defs: ",
                    key, "is not defined"));
        }
    }
}
