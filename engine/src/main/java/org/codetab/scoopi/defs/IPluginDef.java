package org.codetab.scoopi.defs;

import java.util.List;
import java.util.Optional;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.InvalidDefException;
import org.codetab.scoopi.model.Plugin;

public interface IPluginDef {

    void init(Object taskDefs) throws DefNotFoundException, InvalidDefException;

    Optional<List<Plugin>> getPlugins(String taskGroup, String taskName,
            String stepName) throws DefNotFoundException, InvalidDefException;

    Optional<List<Plugin>> getPlugins(Plugin plugin) throws InvalidDefException;

    String getValue(Plugin plugin, String field) throws DefNotFoundException;

    String getValue(Plugin plugin, String field, String defaultValue);

    Optional<List<String>> getArrayValues(Plugin plugin, String field);
}
