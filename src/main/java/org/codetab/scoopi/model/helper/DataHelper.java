package org.codetab.scoopi.model.helper;

import static java.util.Objects.isNull;

import java.util.List;

import javax.inject.Inject;

import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.DataComponent;
import org.codetab.scoopi.model.DataIterator;
import org.codetab.scoopi.util.Sequence;

public class DataHelper {

    @Inject
    private Sequence sequence;

    public void removeItems(final Data data, final List<DataComponent> items) {
        DataIterator it = data.iterator();
        while (it.hasNext()) {
            DataComponent dc = it.next();
            if (items.contains(dc)) {
                it.remove();
            }
        }
    }

    public void addItems(final Data data, final List<DataComponent> items) {
        for (DataComponent item : items) {
            data.addItem(item);
        }
    }

    public void addPageTag(final Data data) {
        String key = String.join("-", "page", data.getName());
        data.addTag("page", sequence.getSequence(key));
    }

    public void addItemTag(final Data data) {
        Integer page = (Integer) data.getTagValue("page");
        String key = String.join("-", "item", data.getName(), "page",
                String.valueOf(page));
        data.addTag("item", sequence.getSequence(key));
    }

    public void addAxisTags(final Data data, final Axis axis) {
        if (isNull(axis)) {
            data.addTag("index", 1);
            data.addTag("order", 1);
        } else {
            data.addTag("index", axis.getIndex());
            data.addTag("order", axis.getOrder());
        }
    }
}
