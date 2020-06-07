package org.codetab.scoopi.defs.yml;

import static java.util.Objects.isNull;
import static org.codetab.scoopi.util.Util.LINE;
import static org.codetab.scoopi.util.Util.spaceit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.defs.IDataDefDef;
import org.codetab.scoopi.defs.IDef;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.InvalidDefException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

@Singleton
public class Def implements IDef {

    private static final Logger LOGGER = LoggerFactory.getLogger(Def.class);

    @Inject
    private Defs defs;
    // @Inject
    // private ILocatorDef locatorDef;
    @Inject
    private IDataDefDef dataDefDef;
    // @Inject
    // private IItemDef itemDef;
    // @Inject
    // private ITaskDef taskDef;
    @Inject
    private IPluginDef pluginDef;

    private JsonNode definedDefs;
    private JsonNode effectiveDefs;
    private Collection<String> defsFiles;

    private ArrayList<Entry<String, JsonNode>> defsMap;

    @Override
    public void init() {
        try {
            if (isNull(defsFiles)) {
                defsFiles = defs.getDefsFiles();
            }
            // load defined defs and validate
            definedDefs = defs.loadDefinedDefs(defsFiles);
            JsonNode defaultSteps = defs.loadDefaultSteps();
            defs.mergeDefaultSteps(definedDefs, defaultSteps);
            LOGGER.debug("--- defined defs ---{}{}", LINE,
                    defs.pretty(definedDefs));
            defs.validateDefinedDefs(definedDefs);

            // create effective defs and validate
            effectiveDefs = defs.createEffectiveDefs(definedDefs);
            LOGGER.debug("--- effective defs ---{}{}", LINE,
                    defs.pretty(effectiveDefs));
            defs.validateEffectiveDefs(effectiveDefs);
            defsMap = Lists.newArrayList(effectiveDefs.fields());
        } catch (Exception e) {
            throw new CriticalException("unable to initialize defs", e);
        }
    }

    // FIXME - Remove this after bootfix
    @Override
    public void initDefProviders() throws DefNotFoundException,
            InvalidDefException, JsonProcessingException {
        // locatorDef.init(getDefs("locatorGroups", defsMap));
        // taskDef.init(getDefs("taskGroups", defsMap));
        // itemDef.init(getDefs("dataDefs", defsMap));

        pluginDef.init(getDefs("taskGroups", defsMap));
        dataDefDef.init(getDefs("dataDefs", defsMap));
    }

    @Override
    public JsonNode getDefsNode(final String defsName) {
        Optional<Entry<String, JsonNode>> entry = defsMap.stream()
                .filter(e -> e.getKey().equals(defsName)).findFirst();
        if (entry.isPresent()) {
            return entry.get().getValue();
        } else {
            throw new CriticalException(spaceit("initialize defs, def:",
                    defsName, "is not defined"));
        }
    }

    @Override
    public void setDefsFiles(final Collection<String> defsFiles) {
        this.defsFiles = defsFiles;
    }

    // FIXME - remove this after bootfix mig
    private JsonNode getDefs(final String key,
            final List<Entry<String, JsonNode>> defsMap) {
        Optional<Entry<String, JsonNode>> entry = defsMap.stream()
                .filter(e -> e.getKey().equals(key)).findFirst();
        if (entry.isPresent()) {
            return entry.get().getValue();
        } else {
            throw new CriticalException(
                    spaceit("initialize defs, def:", key, "is not defined"));
        }
    }

}
