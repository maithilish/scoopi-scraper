package org.codetab.scoopi.plugin.encoder;

import java.util.Comparator;

import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.Member;

public final class ColComparator implements Comparator<Member> {

    @Override
    public int compare(final Member m1, final Member m2) {
        Axis m1Col = m1.getAxis(AxisName.COL);
        Axis m2Col = m2.getAxis(AxisName.COL);
        return m1Col.getOrder() - m2Col.getOrder();
    }

}
