package org.codetab.scoopi.step.process;

import javax.inject.Inject;

import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.helper.FilterHelper;
import org.codetab.scoopi.step.base.BaseProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataFilter extends BaseProcessor {

    static final Logger LOGGER = LoggerFactory.getLogger(DataFilter.class);

    @Inject
    private FilterHelper filterHelper;

    @Override
    public boolean process() {
        String dataDef = getPayload().getJobInfo().getDataDef();
        /*
         * data uses composite pattern and data.getItems() returns copy of
         * DataComponet list with only objects of Item type. The removeItem()
         * removes from original list and it is safe to use it in for loop
         */
        for (Item item : data.getItems()) {
            if (filterHelper.filter(item, dataDef)) {
                data.removeItem(item);
            }
        }
        setOutput(data);
        setConsistent(true);

        System.out.println(data.getItems());

        return true;

    }
}
