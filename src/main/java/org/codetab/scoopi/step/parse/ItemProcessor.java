package org.codetab.scoopi.step.parse;

import static java.util.Objects.isNull;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.inject.Inject;
import javax.script.ScriptException;

import org.codetab.scoopi.defs.IItemDefs;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.helper.DataHelper;

public class ItemProcessor {

    @Inject
    private QueryProcessor queryProcessor;
    @Inject
    private QueryVarSubstitutor varSubstitutor;
    @Inject
    private ObjectFactory objectFactory;
    @Inject
    private IItemDefs itemDefs;
    @Inject
    private DataHelper dataHelper;

    public Optional<Data> createItems(final DataDef dataDef,
            final Data parentData, final Item item,
            final IValueParser valueParser) throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, ScriptException {

        Data data = null;

        Axis row = item.getAxis(AxisName.ROW);
        String itemName = row.getItemName();
        List<String> fieldNames = itemDefs.getItemNames(dataDef, itemName);

        if (fieldNames.size() == 0) {
            return Optional.ofNullable(data);
        } else {
            data = objectFactory.createData(dataDef.getName());
            parentData.copyTags(data);
            dataHelper.addItemTag(data);
            dataHelper.addAxisTags(data, row);
        }

        for (AxisName axisName : AxisName.getReverseValues()) {
            try {
                Axis axis = item.getAxis(axisName);
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

                varSubstitutor.replaceVariables(queries, item.getAxisMap());

                String value = queryProcessor.query(queries, valueParser);

                Item newItem = item.copy();
                row = newItem.getAxis(AxisName.ROW);
                Axis fact = newItem.getAxis(AxisName.FACT);
                row.setValue(fieldName);
                fact.setValue(value);

                newItem.setParent(data);
                data.addItem(newItem);
            } catch (NoSuchElementException e) {
            }
        }
        return Optional.ofNullable(data);
    }

}
