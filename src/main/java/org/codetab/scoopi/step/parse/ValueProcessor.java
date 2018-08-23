package org.codetab.scoopi.step.parse;

import static java.util.Objects.isNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.inject.Inject;
import javax.script.ScriptException;

import org.codetab.scoopi.defs.IAxisDefs;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.Member;

public class ValueProcessor {

    @Inject
    private IAxisDefs axisDefs;
    @Inject
    private ScriptProcessor scriptProcessor;
    @Inject
    private QueryProcessor queryProcessor;
    @Inject
    private QueryVarSubstitutor varSubstitutor;

    private Map<String, Object> scriptObjectMap = new HashMap<>();

    public void setAxisValues(final String dataDef, final Member member,
            final IValueParser valueParser) throws DataDefNotFoundException,
            ScriptException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        for (AxisName axisName : AxisName.values()) {
            Axis axis = null;
            try {
                axis = member.getAxis(axisName);
            } catch (NoSuchElementException e) {
                continue;
            }
            if (axis.getIndex() == null) {
                int startIndex = axisDefs.getStartIndex(dataDef, axis);
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

                // prefixProcessor
                axis.setValue(value);
            }
        }
    }

    public void addScriptObject(final String key, final Object value) {
        scriptObjectMap.put(key, value);
    }

}
