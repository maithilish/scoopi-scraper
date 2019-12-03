package org.codetab.scoopi.plugin.script;

import static java.util.Objects.nonNull;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.Plugin;

public class ScriptExecutor {

    @Inject
    private ScriptFactory scriptFactory;
    @Inject
    private ScriptEnginePool scriptEnginePool;

    public Object execute(final List<Plugin> plugins, final Object input)
            throws DefNotFoundException, ScriptException, IOException,
            ClassNotFoundException, Exception {
        Object scriptOutput = input;
        Object scriptInput = input;
        for (Plugin plugin : plugins) {
            ScriptEngine engine = null;
            try {
                engine = scriptEnginePool.borrowObject(plugin);
                IScript script = scriptFactory.createScript(engine, plugin);
                scriptOutput = script.execute(scriptInput);
                scriptInput = scriptOutput;
            } finally {
                if (nonNull(engine)) {
                    scriptEnginePool.returnObject(plugin, engine);
                }
            }
        }
        return scriptOutput;
    }
}
