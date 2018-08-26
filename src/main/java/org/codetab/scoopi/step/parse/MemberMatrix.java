package org.codetab.scoopi.step.parse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.Member;
import org.codetab.scoopi.model.helper.MemberHelper;

public class MemberMatrix {

    @Inject
    private MemberHelper memberHelper;

    // set of hash - of member indexes
    private Set<Integer> createdSet = new HashSet<>();

    /**
     * Returns copy of indexes where index value of the axis is incremented by
     * one. For indexes {0,1,2,0}, for Axis.COL returns {0,2,2,0} and for
     * Axis.ROW returns {0,1,3,0}
     *
     * @param indexes
     * @param axis
     * @return copy of indexes
     */
    public Integer[] nextMemberIndexes(final Integer[] indexes,
            final Axis axis) {
        Integer[] copy = Arrays.copyOf(indexes, indexes.length);
        int i = axis.getName().ordinal();
        copy[i] = indexes[i] + 1;
        return copy;
    }

    /**
     * Create and return adjacent member. It deep copies the member and
     * increments the specified axis index and order of the copy by one. It also
     * nullifies the value field of all the axis.
     * <p>
     * For example, for Axis.COL it creates copies the member and increments its
     * col axis index and order by one and set null to all axis value field.
     * </p>
     * <p>
     * It saves the hash of new member which is used by notYetCreated() method
     * to determine whether an adjacent member is already created by some other
     * member.
     * </p>
     * @param member
     * @param axis
     * @return
     */
    public Member createAdjacentMember(final Member member, final Axis axis) {
        Member copy = memberHelper.copy(member);
        // reset all axis value
        copy.getAxes().stream().forEach(a -> a.setValue(null));

        Axis axisCopy = copy.getAxis(axis.getName());
        axisCopy.setIndex(axisCopy.getIndex() + 1);
        axisCopy.setOrder(axisCopy.getOrder() + 1);

        Integer[] indexes = memberHelper.getMemberIndexes(copy);
        createdSet.add(Arrays.hashCode(indexes));

        return copy;
    }

    /**
     * <p>
     * Is an adjacent member is already created or not.
     * </p>
     *
     * @param indexes
     *            of a member
     * @return true or false
     */
    public boolean notYetCreated(final Integer[] indexes) {
        int hash = Arrays.hashCode(indexes);
        return !createdSet.contains(hash);
    }
}
