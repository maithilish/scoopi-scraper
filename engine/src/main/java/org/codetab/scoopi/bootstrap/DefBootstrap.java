package org.codetab.scoopi.bootstrap;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IDataDefDefBuilder;
import org.codetab.scoopi.defs.IDef;
import org.codetab.scoopi.defs.IItemDefBuilder;
import org.codetab.scoopi.defs.ILocatorDefBuilder;
import org.codetab.scoopi.defs.IPluginDefBuilder;
import org.codetab.scoopi.defs.ITaskDefBuilder;
import org.codetab.scoopi.defs.yml.DataDefDefData;
import org.codetab.scoopi.defs.yml.ItemDefData;
import org.codetab.scoopi.defs.yml.LocatorDefData;
import org.codetab.scoopi.defs.yml.PluginDefData;
import org.codetab.scoopi.defs.yml.TaskDefData;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.InvalidDefException;
import org.codetab.scoopi.store.IStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefBootstrap {

    static final Logger LOGGER = LoggerFactory.getLogger(DefBootstrap.class);

    @Inject
    private IDef def;
    @Inject
    private ILocatorDefBuilder locatorDefBuilder;
    @Inject
    private IItemDefBuilder itemDefBuilder;
    @Inject
    private ITaskDefBuilder taskDefBuilder;
    @Inject
    private IPluginDefBuilder pluginDefBuilder;
    @Inject
    private IDataDefDefBuilder dataDefDefBuilder;

    public boolean bootstrap(final IStore store) {
        LOGGER.info("bootstrap defs");
        def.init();
        try {
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

            PluginDefData pluginDefData =
                    pluginDefBuilder.buildData(def.getDefsNode("taskGroups"));
            store.put("pluginDef", pluginDefBuilder.serialize(pluginDefData));

            DataDefDefData dataDefDefData =
                    dataDefDefBuilder.buildData(def.getDefsNode("dataDefs"));
            store.put("dataDefDef",
                    dataDefDefBuilder.serialize(dataDefDefData));

        } catch (DefNotFoundException | InvalidDefException e) {
            String message = "unable initialize defs";
            throw new CriticalException(message, e);
        }
        return true;
    }
}
