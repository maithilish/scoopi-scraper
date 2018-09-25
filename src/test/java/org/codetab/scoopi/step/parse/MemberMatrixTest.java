package org.codetab.scoopi.step.parse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Set;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.Member;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.helper.MemberHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MemberMatrixTest {

    @Mock
    private MemberHelper memberHelper;

    @InjectMocks
    private MemberMatrix memberMatrix;

    private ObjectFactory factory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        factory = new ObjectFactory();
    }

    @Test
    public void testCreateAdjacentMember() throws IllegalAccessException {
        Axis col = factory.createAxis(AxisName.COL, "date");
        col.setValue("c");
        col.setIndex(1);
        col.setOrder(2);

        Axis row = factory.createAxis(AxisName.ROW, "price");
        row.setValue("r");
        row.setIndex(10);
        row.setOrder(11);

        Member member = factory.createMember();
        member.addAxis(col);
        member.addAxis(row);

        // value is null in expected axis
        Axis expectedCol = factory.createAxis(AxisName.COL, "date");
        expectedCol.setIndex(2);
        expectedCol.setOrder(3);

        Axis expectedRow = factory.createAxis(AxisName.ROW, "price");
        expectedRow.setIndex(10);
        expectedRow.setOrder(11);

        Member copy = member.copy();

        String nextMemberIndexesKey = "1020300";

        given(memberHelper.copy(member)).willReturn(copy);

        assertThat(memberMatrix.notYetCreated(nextMemberIndexesKey)).isTrue();

        Member actual = memberMatrix.createAdjacentMember(member, col,
                nextMemberIndexesKey);

        assertThat(actual).isSameAs(copy);
        assertThat(actual.getAxis(AxisName.COL)).isEqualTo(expectedCol);
        assertThat(actual.getAxis(AxisName.ROW)).isEqualTo(expectedRow);

        assertThat(memberMatrix.notYetCreated(nextMemberIndexesKey)).isFalse();
    }

    @Test
    public void testNotYetCreated() throws IllegalAccessException {

        String nextMemberIndexesKey = "1020300";

        @SuppressWarnings("unchecked")
        Set<String> createdSet = (Set<String>) FieldUtils
                .readField(memberMatrix, "createdSet", true);

        assertThat(memberMatrix.notYetCreated(nextMemberIndexesKey)).isTrue();

        createdSet.add(nextMemberIndexesKey);

        assertThat(memberMatrix.notYetCreated(nextMemberIndexesKey)).isFalse();
    }

}
