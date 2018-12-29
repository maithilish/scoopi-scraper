package org.codetab.scoopi.step.parse;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.codetab.scoopi.util.Util.LINE;
import static org.codetab.scoopi.util.Util.spaceit;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.inject.Inject;
import javax.script.ScriptException;

import org.codetab.scoopi.exception.InvalidDefException;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.TaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValueProcessor {

    static final Logger LOGGER = LoggerFactory.getLogger(ValueProcessor.class);

    @Inject
    private ScriptProcessor scriptProcessor;
    @Inject
    private QueryProcessor queryProcessor;
    @Inject
    private PrefixProcessor prefixProcessor;
    @Inject
    private QueryVarSubstitutor varSubstitutor;
    @Inject
    private BreakAfter breakAfter;
    @Inject
    private TaskInfo taskInfo;

    private Map<String, Object> scriptObjectMap;

    public void setAxisValues(final String dataDef, final Item item,
            final Map<String, Integer> indexMap, final Indexer indexer,
            final IValueParser valueParser) throws ScriptException,
            IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, InvalidDefException {
        // as index of AxisName.FACT is zero, process in reverse so that
        // all other axis are processed before the fact
        for (Axis axis : item.getAxes()) {
            axis.setIndex(indexMap.get(axis.getItemName()));
            // if (isNull(axis.getIndex())) {
            // }
        }

        for (Axis axis : item.getAxes()) {
            String axisName = axis.getAxisName();
            String itemName = axis.getItemName();
            if (isNull(axis.getValue()) && nonNull(axis.getMatch())) {
                axis.setValue(axis.getMatch().replaceAll("\\\\", ""));
            }

            if (isNull(axis.getValue())) {
                String value = null;
                try {
                    StringBuilder trace = new StringBuilder();
                    Map<String, String> scripts =
                            scriptProcessor.getScripts(dataDef, itemName);
                    appendQueryTrace(trace, "", scripts);

                    Map<String, String> varValues = varSubstitutor
                            .getVarValueMap(scripts, item.getAxes(), axis);
                    varSubstitutor.replaceVariables(scripts, varValues);
                    appendQueryTrace(trace, "    >>>", scripts);

                    logQueryTrace(itemName, trace);

                    scriptProcessor.init(scriptObjectMap); // lazy

                    value = scriptProcessor.query(scripts);
                } catch (NoSuchElementException e) {
                }

                if (isNull(value)) {
                    try {
                        StringBuilder trace = new StringBuilder();
                        Map<String, String> queries = queryProcessor
                                .getQueries(dataDef, axisName, itemName);
                        appendQueryTrace(trace, "", queries);

                        Map<String, String> varValues = varSubstitutor
                                .getVarValueMap(queries, item.getAxes(), axis);
                        varSubstitutor.replaceVariables(queries, varValues);
                        appendQueryTrace(trace, "    >>>", queries);

                        logQueryTrace(itemName, trace);

                        value = queryProcessor.query(queries, valueParser);

                    } catch (NoSuchElementException e) {
                    }
                }

                Optional<List<String>> breakAfters =
                        breakAfter.getBreakAfters(dataDef, itemName);
                if (breakAfters.isPresent()) {
                    if (isNull(value)) {
                        throw new InvalidDefException(spaceit("axis:", axisName,
                                "value is null, unable to apply breakAfter"));
                    } else {
                        if (breakAfter.check(breakAfters, value)) {
                            indexer.markBreakAfter(itemName);
                        }
                    }
                }

                if (nonNull(value)) {
                    Optional<List<String>> prefixes =
                            prefixProcessor.getPrefixes(dataDef, itemName);
                    if (prefixes.isPresent()) {
                        value = prefixProcessor.prefixValue(value,
                                prefixes.get());
                        LOGGER.trace(taskInfo.getMarker(), "prefixed value: {}",
                                value);
                    }
                }
                axis.setValue(value);
            }
        }
    }

    public void addScriptObject(final String key, final Object value) {
        if (isNull(scriptObjectMap)) {
            scriptObjectMap = new HashMap<>();
        }
        scriptObjectMap.put(key, value);
    }

    private void appendQueryTrace(final StringBuilder trace,
            final String message, final Map<String, String> queries) {
        if (!LOGGER.isTraceEnabled()) {
            return;
        }
        String[] keys = new String[] {"script", "region", "field", "attribute"};
        String line = LINE;
        trace.append(message);
        trace.append(line);
        for (String key : keys) {
            String value = queries.get(key);
            if (nonNull(value)) {
                trace.append("  ");
                trace.append(key);
                trace.append(": ");
                trace.append(queries.get(key));
                trace.append(line);
            }
        }
    }

    private void logQueryTrace(final String itemName,
            final StringBuilder trace) {
        LOGGER.trace(taskInfo.getMarker(), "[{}]:{} query{}{}",
                taskInfo.getLabel(), itemName, LINE, trace.toString());
    }

}
