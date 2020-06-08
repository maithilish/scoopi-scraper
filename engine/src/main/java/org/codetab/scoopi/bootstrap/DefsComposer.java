package org.codetab.scoopi.bootstrap;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IDef;
import org.codetab.scoopi.defs.yml.DataDefDefBuilder;
import org.codetab.scoopi.defs.yml.DataDefDefData;
import org.codetab.scoopi.defs.yml.ItemDefBuilder;
import org.codetab.scoopi.defs.yml.ItemDefData;
import org.codetab.scoopi.defs.yml.LocatorDefBuilder;
import org.codetab.scoopi.defs.yml.LocatorDefData;
import org.codetab.scoopi.defs.yml.PluginDefBuilder;
import org.codetab.scoopi.defs.yml.PluginDefData;
import org.codetab.scoopi.defs.yml.TaskDefBuilder;
import org.codetab.scoopi.defs.yml.TaskDefData;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.InvalidDefException;
import org.codetab.scoopi.store.IStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefsComposer {

    static final Logger LOGGER = LoggerFactory.getLogger(DefsComposer.class);

    @Inject
    private IDef def;
    @Inject
    private LocatorDefBuilder locatorDefBuilder;
    @Inject
    private ItemDefBuilder itemDefBuilder;
    @Inject
    private TaskDefBuilder taskDefBuilder;
    @Inject
    private PluginDefBuilder pluginDefBuilder;
    @Inject
    private DataDefDefBuilder dataDefDefBuilder;
    @Inject
    private IStore store;

    public void compose() {
        LOGGER.info("bootstrap def data and push to store");
        def.init();
        try {
            // def data build, serialize and store
            LocatorDefData locatorDefData = (LocatorDefData) locatorDefBuilder
                    .buildData(def.getDefsNode("locatorGroups"));
            store.put("locatorDef", locatorDefData);

            ItemDefData itemDefData = (ItemDefData) itemDefBuilder
                    .buildData(def.getDefsNode("dataDefs"));
            store.put("itemDef", itemDefData);

            TaskDefData taskDefData = (TaskDefData) taskDefBuilder
                    .buildData(def.getDefsNode("taskGroups"));
            store.put("taskDef", taskDefData);

            PluginDefData pluginDefData = (PluginDefData) pluginDefBuilder
                    .buildData(def.getDefsNode("taskGroups"));
            store.put("pluginDef", pluginDefData);

            DataDefDefData dataDefDefData = (DataDefDefData) dataDefDefBuilder
                    .buildData(def.getDefsNode("dataDefs"));
            store.put("dataDefDef", dataDefDefData);

        } catch (DefNotFoundException | InvalidDefException e) {
            String message = "unable initialize defs";
            throw new CriticalException(message, e);
        }
    }
}
