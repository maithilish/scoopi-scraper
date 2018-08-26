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
import org.codetab.scoopi.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper routines for Member.
 * @author Maithilish
 *
 */
public class MemberHelper {

    static final Logger LOGGER = LoggerFactory.getLogger(MemberHelper.class);

    public Member copy(final Member member) {
        return member.copy();
    }

    public Integer[] getMemberIndexes(final Member member) {
        Integer[] memberIndexes = new Integer[AxisName.values().length];
        for (AxisName axisName : AxisName.values()) {
            int index = 0;
            try {
                Axis axis = member.getAxis(axisName);
                if (nonNull(axis.getIndex())) {
                    index = new Integer(axis.getIndex());
                }
            } catch (NoSuchElementException e) {
            }

            memberIndexes[axisName.ordinal()] = index;
        }
        return memberIndexes;
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
