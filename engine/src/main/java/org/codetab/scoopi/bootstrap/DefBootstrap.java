package org.codetab.scoopi.bootstrap;

import java.util.List;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IDataDefDef;
import org.codetab.scoopi.defs.IDef;
import org.codetab.scoopi.defs.IItemDefBuilder;
import org.codetab.scoopi.defs.ILocatorDefBuilder;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.defs.ITaskDefBuilder;
import org.codetab.scoopi.defs.yml.ItemDefData;
import org.codetab.scoopi.defs.yml.LocatorDefData;
import org.codetab.scoopi.defs.yml.TaskDefData;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.InvalidDefException;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.persistence.DataDefPersistence;
import org.codetab.scoopi.store.IStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

public class DefBootstrap {

    static final Logger LOGGER = LoggerFactory.getLogger(DefBootstrap.class);

    @Inject
    private IDef def;
    @Inject
    private ILocatorDefBuilder locatorDefBuilder;
    @Inject
    private IItemDefBuilder itemDefBuilder;
    @Inject
    private IDataDefDef dataDefDef;
    @Inject
    private ITaskDefBuilder taskDefBuilder;
    @Inject
    private IPluginDef pluginDef;
    @Inject
    private DataDefPersistence dataDefPersistence;

    public boolean bootstrap(final IStore store) {
        LOGGER.info("bootstrap defs");
        def.init();
        try {
            // FIXME - remove this
            def.initDefProviders();

            // def data build, serialize and store
            LocatorDefData locatorDefData = locatorDefBuilder
                    .buildData(def.getDefsNode("locatorGroups"));
            store.put("locatorDef",
                    locatorDefBuilder.serialize(locatorDefData));

            ItemDefData itemDefData =
                    itemDefBuilder.buildData(def.getDefsNode("dataDefs"));
            store.put("itemDef", itemDefBuilder.serialize(itemDefData));

            TaskDefData taskDefData =
                    taskDefBuilder.buildData(def.getDefsNode("taskGroups"));
            store.put("taskDef", taskDefBuilder.serialize(taskDefData));

            store.put("pluginDef", pluginDef);
        } catch (DefNotFoundException | InvalidDefException
                | JsonProcessingException e) {
            String message = "unable initialize defs";
            throw new CriticalException(message, e);
        }
        return true;
    }

    public boolean updateDataDefs(final IStore store) {
        LOGGER.info("update dataDefs");
        if (dataDefPersistence.persistDataDef()) {
            List<DataDef> newDataDefs = dataDefDef.getDefinedDataDefs();
            List<DataDef> oldDataDefs = dataDefPersistence.loadDataDefs();
            List<DataDef> effDataDefs = oldDataDefs;
            // old list is updated with changes
            boolean isChanged = dataDefPersistence.markForUpdation(newDataDefs,
                    oldDataDefs);
            if (isChanged) {
                dataDefPersistence.storeDataDefs(oldDataDefs);
                effDataDefs = dataDefPersistence.loadDataDefs();
            }
            dataDefDef.updateDataDefs(effDataDefs);
        }

        store.put("dataDefDef", dataDefDef);
        return true;
    }

}
