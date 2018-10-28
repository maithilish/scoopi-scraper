package org.codetab.scoopi.step.parse;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.codetab.scoopi.util.Util.LINE;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.inject.Inject;
import javax.script.ScriptException;

import org.apache.commons.lang3.Range;
import org.codetab.scoopi.defs.yml.AxisDefs;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;
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
    private AxisDefs axisDefs;
    @Inject
    private TaskInfo taskInfo;

    private Map<String, Object> scriptObjectMap = new HashMap<>();

    public void setAxisValues(final DataDef dataDef, final Item item,
            final IValueParser valueParser)
            throws ScriptException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        // as index of AxisName.FACT is zero, process in reverse so that
        // all other axis are processed before the fact
        for (AxisName axisName : AxisName.getReverseValues()) {
            Axis axis = null;
            try {
                axis = item.getAxis(axisName);
            } catch (NoSuchElementException e) {
                continue;
            }

            if (axis.getIndex() == null) {
                int startIndex = 1;
                Optional<Range<Integer>> indexRange =
                        axisDefs.getIndexRange(dataDef, axis);
                if (indexRange.isPresent()) {
                    startIndex = indexRange.get().getMinimum();
                }
                axis.setIndex(startIndex);
            }

            if (axis.getValue() == null) {
                String value = null;
                try {
                    StringBuilder trace = new StringBuilder();
                    scriptProcessor.init(scriptObjectMap); // lazy
                    Map<String, String> scripts =
                            scriptProcessor.getScripts(dataDef, axisName);
                    appendQueryTrace(trace, "", scripts);

                    varSubstitutor.replaceVariables(scripts, item.getAxisMap());
                    appendQueryTrace(trace, "    >>>", scripts);

                    logQueryTrace(axisName, trace);

                    value = scriptProcessor.query(scripts);
                } catch (NoSuchElementException e) {
                }

                if (isNull(value)) {
                    try {
                        StringBuilder trace = new StringBuilder();
                        Map<String, String> queries =
                                queryProcessor.getQueries(dataDef, axisName);
                        appendQueryTrace(trace, "", queries);

                        if (!queries.get("field").equals("scoopi:none")) {
                            varSubstitutor.replaceVariables(queries,
                                    item.getAxisMap());
                            appendQueryTrace(trace, "    >>>", queries);

                            logQueryTrace(axisName, trace);

                            value = queryProcessor.query(queries, valueParser);
                        }
                    } catch (NoSuchElementException e) {
                    }
                }

                if (nonNull(value)) {
                    Optional<List<String>> prefixes =
                            prefixProcessor.getPrefixes(dataDef, axisName);
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

    private void logQueryTrace(final AxisName axisName,
            final StringBuilder trace) {
        LOGGER.trace(taskInfo.getMarker(), "[{}]:{} query{}{}",
                taskInfo.getLabel(), axisName, LINE, trace.toString());
    }

}
