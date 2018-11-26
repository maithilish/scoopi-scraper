package org.codetab.scoopi.step.script;

import static java.util.Objects.isNull;
import static org.codetab.scoopi.util.Util.dashit;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.helper.IOHelper;
import org.codetab.scoopi.model.Plugin;
import org.codetab.scoopi.plugin.script.IScript;

@Singleton
public class ScriptFactory {

    @Inject
    private DInjector di;
    @Inject
    private IOHelper ioHelper;
    @Inject
    private IPluginDef pluginDef;

    // engine pool
    private Map<String, Stack<ScriptEngine>> engineMap = new HashMap<>();

    public IScript createScript(final ScriptEngine engine, final Plugin plugin)
            throws DefNotFoundException, ClassNotFoundException {
        IScript script = di.instance(plugin.getClassName(), IScript.class);
        script.setPlugin(plugin);
        script.setScriptEngine(engine);
        return script;
    }

    /**
     *
     * @param plugin
     * @return
     * @throws DefNotFoundException
     * @throws ScriptException
     * @throws IOException
     */
    public ScriptEngine getScriptEngine(final Plugin plugin)
            throws DefNotFoundException, ScriptException, IOException {
        String taskGroup = plugin.getTaskGroup();
        String taskName = plugin.getTaskName();
        String stepName = plugin.getStepName();
        String pluginName = plugin.getName();

        String key = dashit(taskGroup, taskName, stepName, pluginName);

        synchronized (this) {
            ScriptEngine engine = null;
            if (engineMap.containsKey(key)) {
                Stack<ScriptEngine> stack = engineMap.get(key);
                if (!stack.isEmpty()) {
                    engine = stack.pop();
                }
            } else {
                Stack<ScriptEngine> stack = new Stack<>();
                engineMap.put(key, stack);
            }
            if (isNull(engine)) {
                ScriptEngineManager engineManager = new ScriptEngineManager();
                engine = engineManager.getEngineByName("JavaScript");
                evalScripts(engine, plugin);
                engine.put("scoopiPluginKey", key);
            }
            return engine;
        }
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

    public void putScriptEngine(final ScriptEngine engine) {
        String key = (String) engine.get("scoopiPluginKey");
        Stack<ScriptEngine> stack = engineMap.get(key);
        stack.push(engine);
    }
}
