package org.codetab.scoopi.model.helper;

import static java.util.Objects.isNull;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IAxisDefs;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.model.Member;
import org.codetab.scoopi.shared.StatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper routines for Member.
 * @author Maithilish
 *
 */
public class MemberHelper {

    static final Logger LOGGER = LoggerFactory.getLogger(MemberHelper.class);

    @Inject
    private IAxisDefs axiDefs;
    @Inject
    private StatService statService;

    @Inject
    private MemberHelper() {
    }

    /**
     * create deep copy of member
     * @param member
     * @return
     */
    public Member createMember(final Member member) {
        return member.copy();
    }

    public Integer[] nextMemberIndexes(final Member member,
            final AxisName axisName) {
        Integer[] indexes = getMemberIndexes(member);
        indexes[axisName.ordinal()] = indexes[axisName.ordinal()] + 1;
        return indexes;
    }

    public boolean alreadyProcessed(final Set<Integer[]> memberIndexSet,
            final Integer[] memberIndexes) {
        for (Integer[] indexes : memberIndexSet) {
            boolean processed = true;
            for (int i = 0; i < AxisName.values().length; i++) {
                int index = indexes[i];
                int memberIndex = memberIndexes[i];
                if (index != memberIndex) {
                    processed = false;
                }
            }
            if (processed) {
                return processed;
            }
        }
        return false;
    }

    public boolean hasFinished(final String dataDef, final Axis axis,
            final int endIndex) throws NumberFormatException {
        boolean noField = true;
        try {
            List<String> breakAfters = axiDefs.getBreakAfters(dataDef, axis);
            noField = false;
            String value = axis.getValue();
            if (isNull(value)) {
                String message =
                        "value is null, check breakAfter or query in datadef";
                throw new StepRunException(message);
            } else {
                for (String breakAfter : breakAfters) {
                    if (value.equals(breakAfter)) {
                        return true;
                    }
                }
            }
        } catch (NoSuchElementException e) {
        } catch (DataDefNotFoundException e) {
            String message = String.join(" ", "unable to get breakAfter");
            LOGGER.error("{} {}", message, e.getMessage());
            LOGGER.debug("{} {}", message, e);
            statService.log(CAT.INTERNAL, message, e);
        }

        if (endIndex >= 0) {
            noField = false;
            if (axis.getIndex() + 1 > endIndex) {
                return true;
            }
        }

        if (noField) {
            String message = "breakAfter or indexRange undefined";
            throw new NoSuchElementException(message);
        }
        return false;
    }

    public Integer[] getMemberIndexes(final Member member) {
        Integer[] memberIndexes = new Integer[AxisName.values().length];
        for (AxisName axisName : AxisName.values()) {
            Axis axis = null;
            int index = 0;
            try {
                axis = member.getAxis(axisName);
                index = new Integer(axis.getIndex());
            } catch (NoSuchElementException e) {
            }
            memberIndexes[axisName.ordinal()] = index;
        }
        return memberIndexes;
    }
}
