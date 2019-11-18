package org.codetab.scoopi.pool;

import static org.codetab.scoopi.util.Util.dashit;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.helper.IOHelper;
import org.codetab.scoopi.model.Plugin;

class ScriptEngineFactory
        extends BaseKeyedPooledObjectFactory<Plugin, ScriptEngine> {

    @Inject
    private IOHelper ioHelper;
    @Inject
    private IPluginDef pluginDef;

    @Override
    public ScriptEngine create(final Plugin plugin) throws Exception {
        String taskGroup = plugin.getTaskGroup();
        String taskName = plugin.getTaskName();
        String stepName = plugin.getStepName();
        String pluginName = plugin.getName();

        String key = dashit(taskGroup, taskName, stepName, pluginName);
        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngine engine = engineManager.getEngineByName("JavaScript");
        evalScripts(engine, plugin);
        engine.put("scoopiPluginKey", key);
        return engine;
    }

    @Override
    public PooledObject<ScriptEngine> wrap(final ScriptEngine value) {
        return new DefaultPooledObject<ScriptEngine>(value);
    }

    private void evalScripts(final ScriptEngine engine, final Plugin plugin)
            throws DefNotFoundException, ScriptException, IOException {
        String mainScript = pluginDef.getValue(plugin, "script");
        Reader reader = ioHelper.getReader(mainScript);
        engine.eval(reader);
        reader.close();

        Optional<List<String>> scripts =
                pluginDef.getArrayValues(plugin, "scripts");
        if (scripts.isPresent()) {
            for (String script : scripts.get()) {
                reader = ioHelper.getReader(script);
                engine.eval(reader);
                reader.close();
            }
        }
    }
}
