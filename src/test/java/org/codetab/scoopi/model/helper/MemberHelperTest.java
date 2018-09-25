package org.codetab.scoopi.model.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.commons.lang3.Range;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.Member;
import org.codetab.scoopi.model.ObjectFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

public class MemberHelperTest {

    @InjectMocks
    private MemberHelper memberHelper;

    private static ObjectFactory factory;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass() {
        factory = new ObjectFactory();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCopy() {
        Axis fact = factory.createAxis(AxisName.FACT, "fact");
        Axis col = factory.createAxis(AxisName.COL, "date");
        Axis row = factory.createAxis(AxisName.ROW, "item");

        Member member = createTestMember();
        member.addAxis(fact);
        member.addAxis(col);
        member.addAxis(row);

        Member actual = memberHelper.copy(member);

        assertThat(actual).isNotSameAs(member);
        assertThat(actual).isEqualTo(member);
    }

    @Test
    public void testGetNextMemberIndexesKey() {
        Member member = createTestMember();

        // indexes: fact 10, col 20, row 30, page 0

        String actual = memberHelper.getNextMemberIndexesAsKey(member,
                member.getAxis(AxisName.FACT));

        assertThat(actual).isEqualTo("1120300");

        actual = memberHelper.getNextMemberIndexesAsKey(member,
                member.getAxis(AxisName.COL));

        assertThat(actual).isEqualTo("1021300");

        actual = memberHelper.getNextMemberIndexesAsKey(member,
                member.getAxis(AxisName.ROW));

        assertThat(actual).isEqualTo("1020310");
    }

    @Test
    public void testGetNextMemberIndexesKeyShouldThrowException() {
        Member member = createTestMember();

        // indexes: fact 10, col 20, row 30, page 0
        testRule.expect(NoSuchElementException.class);
        memberHelper.getNextMemberIndexesAsKey(member,
                member.getAxis(AxisName.PAGE));
    }

    @Test
    public void testGetMemberIndexesNullIndex() {
        Member member = createTestMember();
        member.getAxis(AxisName.COL).setIndex(null);

        // indexes: fact 10, col null, row 30, page 0
        String actual = memberHelper.getNextMemberIndexesAsKey(member,
                member.getAxis(AxisName.COL));
        assertThat(actual).isEqualTo("101300");
    }

    @Test
    public void testIsAxisWithinRangeNotRangeAxis()
            throws NumberFormatException {
        Axis fact = factory.createAxis(AxisName.FACT, "factItem");
        Optional<Range<Integer>> indexRange = Optional.empty();
        Optional<List<String>> breakAfters = Optional.empty();
        boolean actual =
                memberHelper.isAxisWithinRange(fact, breakAfters, indexRange);
        assertThat(actual).isFalse();
    }

    @Test
    public void testIsAxisWithinRange() throws NumberFormatException {
        Axis row = factory.createAxis(AxisName.ROW, "item");

        Optional<List<String>> breakAfters = Optional.empty();
        Optional<Range<Integer>> indexRange = Optional.empty();

        // not range axis
        row.setValue("z");
        boolean actual =
                memberHelper.isAxisWithinRange(row, breakAfters, indexRange);
        assertThat(actual).isFalse();

        // breakAfters exists but value not matches
        breakAfters = Optional.of(Lists.newArrayList("x", "y"));
        indexRange = Optional.empty();
        row.setValue("z");
        actual = memberHelper.isAxisWithinRange(row, breakAfters, indexRange);
        assertThat(actual).isTrue();

        // breakAfters exists and value matches
        breakAfters = Optional.of(Lists.newArrayList("x", "y"));
        indexRange = Optional.empty();
        row.setValue("x");
        actual = memberHelper.isAxisWithinRange(row, breakAfters, indexRange);
        assertThat(actual).isFalse();

        // indexRange exists and index within range
        breakAfters = Optional.empty();
        indexRange = Optional.of(Range.between(1, 3));
        row.setIndex(2);
        actual = memberHelper.isAxisWithinRange(row, breakAfters, indexRange);
        assertThat(actual).isTrue();

        // indexRange exists but index outside the range
        breakAfters = Optional.empty();
        indexRange = Optional.of(Range.between(1, 3));
        row.setIndex(3);
        actual = memberHelper.isAxisWithinRange(row, breakAfters, indexRange);
        assertThat(actual).isFalse();
    }

    @Test
    public void testIsAxisWithinRangeShouldThrowException()
            throws NumberFormatException {
        Axis row = factory.createAxis(AxisName.ROW, "item");

        Optional<List<String>> breakAfters =
                Optional.of(Lists.newArrayList("x", "y"));
        Optional<Range<Integer>> indexRange = Optional.of(Range.between(1, 3));

        row.setValue(null);

        testRule.expect(StepRunException.class);
        memberHelper.isAxisWithinRange(row, breakAfters, indexRange);
    }

    public Member createTestMember() {
        Member member = factory.createMember();

        Axis fact = factory.createAxis(AxisName.FACT, "factItem");
        fact.setIndex(10);
        member.addAxis(fact);

        Axis col = factory.createAxis(AxisName.COL, "colItem");
        col.setIndex(20);
        member.addAxis(col);

        Axis row = factory.createAxis(AxisName.ROW, "rowItem");
        row.setIndex(30);
        member.addAxis(row);
        return member;
    }

}
