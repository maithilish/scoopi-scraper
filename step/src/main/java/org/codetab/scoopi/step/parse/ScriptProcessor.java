package org.codetab.scoopi.step.parse;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.Validate.notNull;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.inject.Inject;
import javax.script.ScriptException;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.defs.IItemDef;
import org.codetab.scoopi.model.Query;
import org.codetab.scoopi.model.TaskInfo;
import org.codetab.scoopi.step.parse.cache.ParserCache;

public class ScriptProcessor {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private IItemDef itemDef;
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

    public Map<String, String> getScripts(final String dataDef,
            final String itemName) {
        Map<String, String> scripts = new HashMap<>();
        Optional<Query> query = itemDef.getItemQuery(dataDef, itemName);
        if (query.isPresent()) {
            try {
                String script = query.get().getQuery("script");
                scripts.put("script", script); //$NON-NLS-1$
            } catch (NoSuchElementException e) {
            }
        }
        if (scripts.size() == 0) {
            throw new NoSuchElementException("script not defined");
        }
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
        LOG.trace(taskInfo.getMarker(), "[{}], value: {}", taskInfo.getLabel(),
                value);
        return value;
    }

}
