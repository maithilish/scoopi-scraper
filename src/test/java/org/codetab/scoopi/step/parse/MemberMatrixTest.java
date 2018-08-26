package org.codetab.scoopi.step.parse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;
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
    public void testNextMemberIndexes() {
        Axis col = factory.createAxis(AxisName.COL, "date");
        Integer[] indexes = new Integer[] {0, 1, 2, 0};
        Integer[] actual = memberMatrix.nextMemberIndexes(indexes, col);

        Integer[] expected = new Integer[] {0, 2, 2, 0};
        assertThat(actual).isNotSameAs(indexes);
        assertThat(actual).isNotEqualTo(indexes);
        assertThat(actual).isEqualTo(expected);

        Axis row = factory.createAxis(AxisName.ROW, "item");
        indexes = new Integer[] {0, 1, 2, 0};
        actual = memberMatrix.nextMemberIndexes(indexes, row);

        expected = new Integer[] {0, 1, 3, 0};
        assertThat(actual).isNotSameAs(indexes);
        assertThat(actual).isNotEqualTo(indexes);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testCreateMember() {
        Axis col = factory.createAxis(AxisName.COL, "date");
        col.setValue("c");
        col.setIndex(1);
        col.setOrder(1);

        Axis row = factory.createAxis(AxisName.ROW, "price");
        row.setValue("r");
        row.setIndex(10);
        row.setOrder(10);

        Member member = factory.createMember();
        member.addAxis(col);
        member.addAxis(row);

        Axis expectedCol = factory.createAxis(AxisName.COL, "date");
        expectedCol.setIndex(2);
        expectedCol.setOrder(2);

        Axis expectedRow = factory.createAxis(AxisName.ROW, "price");
        expectedRow.setIndex(10);
        expectedRow.setOrder(10);

        Member copy = member.copy();

        Integer[] indexes = new Integer[] {0, 2, 0, 0};

        given(memberHelper.copy(member)).willReturn(copy);
        given(memberHelper.getMemberIndexes(copy)).willReturn(indexes);

        Member actual = memberMatrix.createAdjacentMember(member, col);

        assertThat(memberMatrix.notYetCreated(indexes)).isFalse();
        assertThat(actual).isSameAs(copy);
        assertThat(actual.getAxis(AxisName.COL)).isEqualTo(expectedCol);
        assertThat(actual.getAxis(AxisName.ROW)).isEqualTo(expectedRow);
    }

    @Test
    public void testNotYetCreated() throws IllegalAccessException {
        Integer[] indexes = new Integer[] {1, 2, 3};

        boolean actual = memberMatrix.notYetCreated(indexes);

        assertThat(actual).isTrue();

        @SuppressWarnings("unchecked")
        Set<Integer> createdSet = (Set<Integer>) FieldUtils
                .readField(memberMatrix, "createdSet", true);
        int hash = Arrays.hashCode(indexes);
        createdSet.add(hash);

        actual = memberMatrix.notYetCreated(indexes);

        assertThat(actual).isFalse();
    }

}
