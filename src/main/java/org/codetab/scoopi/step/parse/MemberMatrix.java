package org.codetab.scoopi.step.parse;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.Member;
import org.codetab.scoopi.model.helper.MemberHelper;

public class MemberMatrix {

    @Inject
    private MemberHelper memberHelper;

    // set of nextMemberIndexesKey
    private Set<String> createdSet = new HashSet<>();

    /**
     * Create and return adjacent member. It deep copies the member and
     * increments the specified axis index and order of the copy by one. It also
     * nullifies the value field of all the axis.
     * <p>
     * For example, for Axis.COL it creates copies the member and increments its
     * col axis index and order by one and set null to all axis value field.
     * </p>
     * <p>
     * It saves the nextMemberIndexesKey in a set which is used by
     * notYetCreated() method to determine whether an adjacent member is already
     * created by some other member.
     * </p>
     * @param member
     * @param axis
     * @param nextMemberIndexesKey
     * @return
     */
    public Member createAdjacentMember(final Member member, final Axis axis,
            final String nextMemberIndexesKey) {
        Member copy = memberHelper.copy(member);
        // reset all axis value
        copy.getAxes().stream().forEach(a -> a.setValue(null));

        Axis axisCopy = copy.getAxis(axis.getName());
        axisCopy.setIndex(axisCopy.getIndex() + 1);
        axisCopy.setOrder(axisCopy.getOrder() + 1);

        createdSet.add(nextMemberIndexesKey);
        return copy;
    }

    /**
     * <p>
     * Return true if an adjacent member is not yet created else false
     * </p>
     *
     * @param indexes
     *            of a member
     * @return true or false
     */
    public boolean notYetCreated(final String indexesKey) {
        return !createdSet.contains(indexesKey);
    }

}
