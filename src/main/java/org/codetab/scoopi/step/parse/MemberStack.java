package org.codetab.scoopi.step.parse;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IAxisDefs;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.Member;
import org.codetab.scoopi.model.helper.MemberHelper;

public class MemberStack {

    @Inject
    private MemberHelper memberHelper;
    @Inject
    private IAxisDefs axisDefs;

    private Deque<Member> mStack = new ArrayDeque<>();
    private Set<Integer[]> memberIndexSet = new HashSet<>();

    public void pushMembers(final List<Member> members) {
        for (Member member : members) {
            mStack.addFirst(member);
        }
    }

    public Member popMember() {
        return mStack.removeFirst(); // pop
    }

    public boolean isEmpty() {
        return mStack.isEmpty();
    }

    public void pushNewMember(final String dataDef, final Member member)
            throws ClassNotFoundException, NumberFormatException {
        for (AxisName axisName : AxisName.values()) {
            Axis axis = null;
            try {
                axis = member.getAxis(axisName);
            } catch (NoSuchElementException e) {
                continue;
            }
            if (axis.getName().equals(AxisName.FACT)) {
                continue;
            }

            int endIndex;
            try {
                endIndex = axisDefs.getEndIndex(dataDef, axis);
            } catch (DataDefNotFoundException e) {
                endIndex = -1;
            }

            if (!memberHelper.hasFinished(dataDef, axis, endIndex)) {
                Integer[] nextMemberIndexes =
                        memberHelper.nextMemberIndexes(member, axisName);
                if (!memberHelper.alreadyProcessed(memberIndexSet,
                        nextMemberIndexes)) {
                    // Member newMember = Util.deepClone(Member.class, member);
                    Member newMember = memberHelper.createMember(member);
                    Axis newAxis = newMember.getAxis(axisName);
                    newAxis.setIndex(newAxis.getIndex() + 1);
                    newAxis.setOrder(newAxis.getOrder() + 1);
                    // nullify all axis value
                    for (Axis na : newMember.getAxes()) {
                        na.setValue(null);
                    }
                    mStack.addFirst(newMember); // push
                    memberIndexSet.add(nextMemberIndexes);
                }
            }
        }
    }

}
