package org.codetab.scoopi.step.parse;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.helper.ItemHelper;

public class ItemMatrix {

    @Inject
    private ItemHelper itemHelper;

    // set of nextItemIndexesKey
    private Set<String> createdSet = new HashSet<>();

    /**
     * Create and return adjacent item. It deep copies the item and increments
     * the specified axis index and order of the copy by one. It also nullifies
     * the value field of all the axis.
     * <p>
     * For example, for Axis.COL it creates copies the item and increments its
     * col axis index and order by one and set null to all axis value field.
     * </p>
     * <p>
     * It saves the nextItemIndexesKey in a set which is used by notYetCreated()
     * method to determine whether an adjacent item is already created by some
     * other item.
     * </p>
     * @param item
     * @param axis
     * @param nextItemIndexesKey
     * @return
     */
    public Item createAdjacentItem(final Item item, final Axis axis,
            final String nextItemIndexesKey) {
        Item copy = itemHelper.copy(item);
        // reset all axis value
        copy.getAxes().stream().forEach(a -> a.setValue(null));

        Axis axisCopy = copy.getAxis(axis.getName());
        axisCopy.setIndex(axisCopy.getIndex() + 1);
        axisCopy.setOrder(axisCopy.getOrder() + 1);

        createdSet.add(nextItemIndexesKey);
        return copy;
    }

    /**
     * <p>
     * Return true if an adjacent item is not yet created else false
     * </p>
     *
     * @param indexes
     *            of a item
     * @return true or false
     */
    public boolean notYetCreated(final String indexesKey) {
        return !createdSet.contains(indexesKey);
    }

}
