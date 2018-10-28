package org.codetab.scoopi.step.parse;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.Range;
import org.codetab.scoopi.defs.yml.AxisDefs;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.helper.ItemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemStack {

    static final Logger LOGGER = LoggerFactory.getLogger(ItemStack.class);

    @Inject
    private AxisDefs axisDefs;
    @Inject
    private ItemHelper itemHelper;
    @Inject
    private ItemMatrix itemMatrix;

    private Deque<Item> mStack = new ArrayDeque<>();

    public void pushItems(final List<Item> items) {
        for (Item item : items) {
            mStack.addFirst(item);
        }
    }

    public Item popItem() {
        return mStack.removeFirst(); // pop
    }

    public boolean isEmpty() {
        return mStack.isEmpty();
    }

    public void pushAdjacentItems(final DataDef dataDef, final Item item) {

        for (AxisName axisName : AxisName.values()) {
            Axis axis = null;
            try {
                axis = item.getAxis(axisName);
            } catch (NoSuchElementException e) {
                continue;
            }
            if (axis.getName().equals(AxisName.FACT)) {
                continue;
            }

            Optional<List<String>> breakAfters =
                    axisDefs.getBreakAfters(dataDef, axis);
            Optional<Range<Integer>> indexRange =
                    axisDefs.getIndexRange(dataDef, axis);
            if (itemHelper.isAxisWithinRange(axis, breakAfters, indexRange)) {
                String nextItemIndexesKey =
                        itemHelper.getNextItemIndexesAsKey(item, axis);
                // if not already created, create and push adjacent item
                // for this axis
                if (itemMatrix.notYetCreated(nextItemIndexesKey)) {
                    Item newItem = itemMatrix.createAdjacentItem(item, axis,
                            nextItemIndexesKey);
                    mStack.addFirst(newItem); // push
                }
            }
        }
    }

}
