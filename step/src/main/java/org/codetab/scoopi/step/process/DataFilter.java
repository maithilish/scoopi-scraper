package org.codetab.scoopi.step.process;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.codetab.scoopi.model.DataComponent;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.step.base.BaseProcessor;

public class DataFilter extends BaseProcessor {

    @Inject
    private FilterHelper filterHelper;

    @Override
    public void process() {
        /*
         * data uses composite pattern and data.getItems() returns copy of
         * DataComponet list with only objects of Item type. The removeItem()
         * removes from original list and it is safe to use it in for loop
         * refactor: remove is expensive, instead add filtered items to new list
         * removing 60 items from 200 takes 300ms
         */
        List<DataComponent> items = new ArrayList<>();
        String dataDef = getPayload().getJobInfo().getDataDef();

        for (Item item : data.getItems()) {
            if (!filterHelper.filter(item, dataDef)) {
                items.add(item);
            }
        }
        data.setItems(items);
        setOutput(data);
    }
}
