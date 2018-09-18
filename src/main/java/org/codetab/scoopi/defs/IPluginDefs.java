package org.codetab.scoopi.defs;

import java.util.List;
import java.util.Optional;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.InvalidDefException;
import org.codetab.scoopi.model.Plugin;

public interface IPluginDefs {

    Optional<List<Plugin>> getPlugins(String taskGroup, String taskName,
            String stepName) throws DefNotFoundException, InvalidDefException;

    String getPluginClass(Plugin plugin);

    String getPluginName(Plugin plugin);

    String getValue(Plugin plugin, String field);
}
