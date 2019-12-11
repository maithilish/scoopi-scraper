package org.codetab.scoopi.bootstrap;

import java.util.List;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IDataDefDef;
import org.codetab.scoopi.defs.IDef;
import org.codetab.scoopi.defs.IItemDef;
import org.codetab.scoopi.defs.ILocatorDef;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.InvalidDefException;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.persistence.DataDefPersistence;
import org.codetab.scoopi.store.IStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefBootstrap {

    static final Logger LOGGER = LoggerFactory.getLogger(DefBootstrap.class);

    @Inject
    private IDef def;
    @Inject
    private ILocatorDef locatorDef;
    @Inject
    private IDataDefDef dataDefDef;
    @Inject
    private IItemDef itemDef;
    @Inject
    private ITaskDef taskDef;
    @Inject
    private IPluginDef pluginDef;
    @Inject
    private DataDefPersistence dataDefPersistence;

    public boolean bootstrap(final IStore store) {
        LOGGER.info("bootstrap defs");
        def.init();
        try {
            def.initDefProviders();

            store.put("locatorDef", locatorDef);
            store.put("itemDef", itemDef);
            store.put("taskDef", taskDef);
            store.put("pluginDef", pluginDef);
        } catch (DefNotFoundException | InvalidDefException e) {
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
