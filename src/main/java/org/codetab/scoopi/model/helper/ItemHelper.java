package org.codetab.scoopi.model.helper;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.commons.lang3.Range;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper routines for Item.
 * @author Maithilish
 *
 */
public class ItemHelper {

    static final Logger LOGGER = LoggerFactory.getLogger(ItemHelper.class);

    public Item copy(final Item item) {
        return item.copy();
    }

    /**
     * Concat indexes of item axes by incrementing index value of the matching
     * axis. For item with indexes {0,1,38,0}
     * <ul>
     * <li>for Axis.COL returns 02380</li>
     * <li>for Axis.ROW returns 01390</li>
     * </ul>
     * <p>
     * index of the items' axis is not altered
     * </p>
     * <p>
     * key based on Integer[] results in collision instead use string key even
     * though slightly slower
     * </p>
     * @param item
     * @param axis
     *            to match and increment its index
     * @return string of concated indexes
     */
    public String getNextItemIndexesAsKey(final Item item, final Axis axis) {
        StringBuilder sb = new StringBuilder();
        for (AxisName axisName : AxisName.values()) {
            String index = "0";
            try {
                Axis mAxis = item.getAxis(axisName);
                if (nonNull(mAxis.getIndex())) {
                    int i = mAxis.getIndex();
                    if (mAxis.getName().equals(axis.getName())) {
                        i++;
                    }
                    index = String.valueOf(i);
                } else {
                    index = "1";
                }
            } catch (NoSuchElementException e) {
            }
            sb.append(index);
        }
        return sb.toString();
    }

    public boolean isAxisWithinRange(final Axis axis,
            final Optional<List<String>> breakAfters,
            final Optional<Range<Integer>> indexRange)
            throws NumberFormatException {

        // no indexRange or breakAfter
        if (!breakAfters.isPresent() && !indexRange.isPresent()) {
            return false;
        }

        String value = axis.getValue();
        if (isNull(value)) {
            String message =
                    "value is null, check breakAfter or query in datadef";
            throw new StepRunException(message);
        } else {
            if (breakAfters.isPresent()) {
                for (String breakAfter : breakAfters.get()) {
                    if (value.equals(breakAfter)) {
                        return false;
                    }
                }
            }
        }

        if (indexRange.isPresent()) {
            if (axis.getIndex() + 1 > indexRange.get().getMaximum()) {
                return false;
            }
        }

        return true;
    }
}
