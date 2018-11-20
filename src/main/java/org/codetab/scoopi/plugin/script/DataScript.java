package org.codetab.scoopi.plugin.script;

import javax.inject.Inject;
import javax.script.Invocable;
import javax.script.ScriptEngine;

import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.model.Plugin;

public class DataScript implements IScript {

    private ScriptEngine scriptEngine;
    private Plugin plugin;

    @Inject
    private IPluginDef pluginDef;

    @Override
    public Object execute(Object input) throws Exception {
        String functionName =
                pluginDef.getValue(plugin, "entryPoint", "execute");
        Invocable invocable = (Invocable) scriptEngine;
        return invocable.invokeFunction(functionName, input);
    }

    @Override
    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setScriptEngine(ScriptEngine scriptEngine) {
        this.scriptEngine = scriptEngine;
    }

}
