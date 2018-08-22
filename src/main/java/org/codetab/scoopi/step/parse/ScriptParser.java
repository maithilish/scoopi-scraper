package org.codetab.scoopi.step.parse;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.validState;

import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.codetab.scoopi.exception.CriticalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptParser {

    static final Logger LOGGER = LoggerFactory.getLogger(ScriptParser.class);

    private ScriptEngine jsEngine;

    // TODO check whether this class can be singleton

    public void initScriptEngine(final Map<String, Object> scriptObjectMap) {
        if (nonNull(jsEngine)) {
            return;
        }
        LOGGER.debug("{}", "initialize script engine");
        ScriptEngineManager scriptEngineMgr = new ScriptEngineManager();
        jsEngine = scriptEngineMgr.getEngineByName("JavaScript"); //$NON-NLS-1$
        if (isNull(jsEngine)) {
            throw new CriticalException(
                    "script engine lib not found in classpath");
        }
        for (String key : scriptObjectMap.keySet()) {
            jsEngine.put(key, scriptObjectMap.get(key));
        }
    }

    public Object eval(final String script) throws ScriptException {
        validState(nonNull(jsEngine), "script engine not initialized");
        return jsEngine.eval(script);
    }
}
