package org.codetab.scoopi.step.parse;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

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
import org.codetab.scoopi.model.Member;

public class ValueProcessor {

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

    private Map<String, Object> scriptObjectMap = new HashMap<>();

    public void setAxisValues(final DataDef dataDef, final Member member,
            final IValueParser valueParser)
            throws ScriptException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        // as index of AxisName.FACT is zero, process in reverse so that
        // all other axis are processed before the fact
        for (AxisName axisName : AxisName.getReverseValues()) {
            Axis axis = null;
            try {
                axis = member.getAxis(axisName);
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
                    scriptProcessor.init(scriptObjectMap); // lazy
                    Map<String, String> scripts =
                            scriptProcessor.getScripts(dataDef, axisName);
                    varSubstitutor.replaceVariables(scripts,
                            member.getAxisMap());
                    value = scriptProcessor.query(scripts);
                } catch (NoSuchElementException e) {
                }

                if (isNull(value)) {
                    try {
                        Map<String, String> queries =
                                queryProcessor.getQueries(dataDef, axisName);
                        varSubstitutor.replaceVariables(queries,
                                member.getAxisMap());
                        value = queryProcessor.query(queries, valueParser);
                    } catch (NoSuchElementException e) {
                    }
                }

                if (nonNull(value)) {
                    Optional<List<String>> prefixes =
                            prefixProcessor.getPrefixes(dataDef, axisName);
                    if (prefixes.isPresent()) {
                        prefixProcessor.prefixValue(value, prefixes.get());
                    }
                }

                axis.setValue(value);
            }
        }
    }

    public void addScriptObject(final String key, final Object value) {
        scriptObjectMap.put(key, value);
    }
}
