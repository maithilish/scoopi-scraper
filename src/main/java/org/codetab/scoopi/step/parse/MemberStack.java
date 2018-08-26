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
import org.codetab.scoopi.model.Member;
import org.codetab.scoopi.model.helper.MemberHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemberStack {

    static final Logger LOGGER = LoggerFactory.getLogger(MemberStack.class);

    @Inject
    private AxisDefs axisDefs;
    @Inject
    private MemberHelper memberHelper;
    @Inject
    private MemberMatrix memberMatrix;

    private Deque<Member> mStack = new ArrayDeque<>();

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

    public void pushAdjacentMembers(final DataDef dataDef,
            final Member member) {
        Integer[] indexes = memberHelper.getMemberIndexes(member);

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

            Optional<List<String>> breakAfters =
                    axisDefs.getBreakAfters(dataDef, axis);
            Optional<Range<Integer>> indexRange =
                    axisDefs.getIndexRange(dataDef, axis);
            if (memberHelper.isAxisWithinRange(axis, breakAfters, indexRange)) {
                Integer[] nextMemberIndexes =
                        memberMatrix.nextMemberIndexes(indexes, axis);
                // if not already created, create and push adjacent member
                // for this axis
                if (memberMatrix.notYetCreated(nextMemberIndexes)) {
                    Member newMember =
                            memberMatrix.createAdjacentMember(member, axis);
                    mStack.addFirst(newMember); // push
                }
            }
        }
    }

}
