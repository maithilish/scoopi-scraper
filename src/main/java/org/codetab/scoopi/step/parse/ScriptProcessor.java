package org.codetab.scoopi.step.parse;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.Validate.notNull;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.inject.Inject;
import javax.script.ScriptException;

import org.apache.commons.beanutils.ConvertUtils;
import org.codetab.scoopi.cache.ParserCache;
import org.codetab.scoopi.defs.IAxisDefs;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.TaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptProcessor {

    static final Logger LOGGER = LoggerFactory.getLogger(ScriptProcessor.class);

    @Inject
    private IAxisDefs axisDefs;
    @Inject
    private ScriptParser scriptParser;
    @Inject
    private ParserCache parserCache;
    @Inject
    private TaskInfo taskInfo;

    public void init(final Map<String, Object> scriptObjectMap) {
        notNull(scriptObjectMap, "scriptObjectMap must not be null");
        scriptParser.initScriptEngine(scriptObjectMap);
    }

    public Map<String, String> getScripts(final DataDef dataDef,
            final AxisName axisName) {
        Map<String, String> scripts = new HashMap<>();
        String script = axisDefs.getQuery(dataDef, axisName, "script");
        if (script.equals("undefined")) {
            throw new NoSuchElementException("script not defined");
        }
        scripts.put("script", script); //$NON-NLS-1$
        return scripts;
    }

    public String query(final Map<String, String> scripts)
            throws ScriptException {
        int key = parserCache.getKey(scripts);
        String value = parserCache.get(key);
        if (isNull(value)) {
            Object val = scriptParser.eval(scripts.get("script")); //$NON-NLS-1$
            value = ConvertUtils.convert(val);
            parserCache.put(key, value);
        }
        LOGGER.trace(taskInfo.getMarker(), "[{}], value: {}",
                taskInfo.getLabel(), value);
        return value;
    }

}
