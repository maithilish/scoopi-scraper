package org.codetab.scoopi.model.helper;

import java.util.List;

import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.DataComponent;
import org.codetab.scoopi.model.DataIterator;

public class DataHelper {

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
            data.addMember(item);
        }
    }

}
