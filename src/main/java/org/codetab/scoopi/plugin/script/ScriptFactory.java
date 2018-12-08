package org.codetab.scoopi.plugin.script;

import javax.inject.Inject;
import javax.script.ScriptEngine;

import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.Plugin;

class ScriptFactory {

    @Inject
    private DInjector di;

    public IScript createScript(final ScriptEngine engine, final Plugin plugin)
            throws DefNotFoundException, ClassNotFoundException {
        IScript script = di.instance(plugin.getClassName(), IScript.class);
        script.setPlugin(plugin);
        script.setScriptEngine(engine);
        return script;
    }

}
