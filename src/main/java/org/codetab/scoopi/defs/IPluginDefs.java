package org.codetab.scoopi.defs;

import java.util.List;
import java.util.Optional;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.InvalidDefException;
import org.codetab.scoopi.model.Plugin;

public interface IPluginDefs {

    Optional<List<Plugin>> getPlugins(String taskGroup, String taskName,
            String stepName) throws DefNotFoundException, InvalidDefException;

    Optional<List<Plugin>> getPlugins(Plugin plugin) throws InvalidDefException;

    String getPluginClass(Plugin plugin) throws DefNotFoundException;

    String getPluginName(Plugin plugin) throws DefNotFoundException;

    String getValue(Plugin plugin, String field) throws DefNotFoundException;

    String getValue(Plugin plugin, String field, String defaultValue);
}
