package org.codetab.scoopi.step.parse;

import static java.util.Objects.isNull;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.inject.Inject;
import javax.script.ScriptException;

import org.codetab.scoopi.defs.IItemDefs;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.Member;

public class ItemProcessor {

    @Inject
    private QueryProcessor queryProcessor;
    @Inject
    private QueryVarSubstitutor varSubstitutor;
    @Inject
    private IItemDefs itemDefs;

    public void setFieldsValue(final DataDef dataDef, final Member member,
            final IValueParser valueParser) throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, ScriptException {

        Axis row = member.getAxis(AxisName.ROW);
        String itemName = row.getMemberName();
        List<String> fieldNames = itemDefs.getFieldNames(dataDef, itemName);

        if (fieldNames.size() == 0) {
            return;
        }

        for (AxisName axisName : AxisName.getReverseValues()) {
            try {
                Axis axis = member.getAxis(axisName);
                if (isNull(axis.getValue())) {
                    axis.setValue("scoopi:item");
                }
            } catch (NoSuchElementException e) {
            }
        }

        for (String fieldName : fieldNames) {
            try {

                Map<String, String> queries =
                        itemDefs.getQueries(dataDef, itemName, fieldName);

                varSubstitutor.replaceVariables(queries, member.getAxisMap());

                String value = queryProcessor.query(queries, valueParser);
                member.addFields(fieldName, value);
                System.out.println(value);
            } catch (NoSuchElementException e) {
            }
        }
    }

}
