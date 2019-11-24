package org.codetab.scoopi.plugin.script;

import javax.script.ScriptEngine;

import org.codetab.scoopi.model.Plugin;

/**
 * <p>
 * Script interface.
 * @author Maithilish
 *
 * @param <T>
 *            input type
 * @param <U>
 *            output type
 */
public interface IScript {

    /**
     * <p>
     * Execute scripts on input
     * @param input
     * @return converted output
     * @throws Exception
     *             script error
     */
    Object execute(Object input) throws Exception;

    void setPlugin(Plugin plugin);

    void setScriptEngine(ScriptEngine engine);
}
