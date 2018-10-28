package org.codetab.scoopi.plugin.encoder;

import java.util.Collections;
import java.util.Comparator;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.codetab.scoopi.defs.IPluginDefs;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.model.Plugin;
import org.codetab.scoopi.system.ErrorLogger;

public class DataSorter {

    @Inject
    private IPluginDefs pluginDefs;
    @Inject
    private ErrorLogger errorLogger;

    public void sort(final Data data, final Plugin plugin) {
        String sortOrder = ""; //$NON-NLS-1$
        try {
            sortOrder = pluginDefs.getValue(plugin, "sortOrder");
        } catch (DefNotFoundException e) {
            sortOrder = "col,row"; // default //$NON-NLS-1$
        }

        if (StringUtils.isBlank(sortOrder)) {
            String message = String.join(" ",
                    "data not sorted, sortOrder not set in", plugin.toString());
            errorLogger.log(CAT.ERROR, message);
            return;
        }

        sortOrder = sortOrder.toUpperCase();
        /*
         * throws IllegalArgumentException if any axis names are valid
         */
        for (String axisName : sortOrder.split(",")) { //$NON-NLS-1$
            AxisName.valueOf(axisName.trim());
        }
        for (String axisName : sortOrder.split(",")) { //$NON-NLS-1$
            AxisName axis = AxisName.valueOf(axisName.trim());
            Comparator<Item> comparator = null;
            switch (axis) {
            case COL:
                comparator = new ColComparator();
                break;
            case ROW:
                comparator = new RowComparator();
                break;
            default:
                break;
            }
            if (comparator != null) {
                Collections.sort(data.getItems(), comparator);
            }
        }
    }
}
