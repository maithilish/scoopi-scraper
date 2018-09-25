package org.codetab.scoopi.step.parse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.Range;
import org.codetab.scoopi.defs.yml.AxisDefs;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.Member;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.helper.MemberHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

public class MemberStackTest {

    @Mock
    private MemberHelper memberHelper;
    @Mock
    private AxisDefs axisDefs;
    @Mock
    private MemberMatrix memberMatrix;

    @InjectMocks
    private MemberStack stack;

    private ObjectFactory factory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        factory = new ObjectFactory();
    }

    @Test
    public void testPushAndPopMembers() {
        Member member1 = factory.createMember();
        Member member2 = factory.createMember();
        List<Member> members = Lists.newArrayList(member1, member2);

        stack.pushMembers(members);

        assertThat(stack.popMember()).isEqualTo(member2);
        assertThat(stack.popMember()).isEqualTo(member1);
        assertThat(stack.isEmpty()).isTrue();
    }

    @Test
    public void testPushAdjacentMembersNextMemberNotYetCreated()
            throws DataDefNotFoundException, IllegalAccessException {

        // push some member - to test whether PushAdjacentMembers uses
        // addFirst()
        stack.pushMembers(Lists.newArrayList(factory.createMember()));

        Axis col = factory.createAxis(AxisName.COL, "member1");
        Member member = factory.createMember();
        member.addAxis(col);

        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "defJson");

        Optional<List<String>> breakAfters =
                Optional.of(Lists.newArrayList("x", "y"));
        Optional<Range<Integer>> indexRange = Optional.of(Range.between(1, 3));

        // Integer[] indexes = new Integer[] {1, 2};
        // Integer[] nextMemberIndexes = new Integer[] {3, 4, 5};
        String nextMemberIndexesKey = "345";
        Member memberCopy = Mockito.mock(Member.class);

        // given(memberHelper.getMemberIndexesKey(member)).willReturn(indexes);
        given(axisDefs.getBreakAfters(dataDef, col)).willReturn(breakAfters);
        given(axisDefs.getIndexRange(dataDef, col)).willReturn(indexRange);
        given(memberHelper.isAxisWithinRange(col, breakAfters, indexRange))
                .willReturn(true);
        // given(memberMatrix.nextMemberIndexes(indexes, col))
        // .willReturn(nextMemberIndexes);
        given(memberHelper.getNextMemberIndexesAsKey(member, col))
                .willReturn(nextMemberIndexesKey);
        given(memberMatrix.notYetCreated(nextMemberIndexesKey))
                .willReturn(true);
        given(memberMatrix.createAdjacentMember(member, col,
                nextMemberIndexesKey)).willReturn(memberCopy);

        stack.pushAdjacentMembers(dataDef, member);

        Member actual = stack.popMember();

        assertThat(actual).isSameAs(memberCopy);
    }

    @Test
    public void testPushAdjacentMembersNextMemberAlreadyCreated()
            throws DataDefNotFoundException, IllegalAccessException {

        // push some member - to test whether PushAdjacentMembers uses
        // addFirst()
        Member dummyMember = factory.createMember();
        stack.pushMembers(Lists.newArrayList(dummyMember));

        Axis col = factory.createAxis(AxisName.COL, "member1");
        Member member = factory.createMember();
        member.addAxis(col);

        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "defJson");

        Optional<List<String>> breakAfters =
                Optional.of(Lists.newArrayList("x", "y"));
        Optional<Range<Integer>> indexRange = Optional.of(Range.between(1, 3));

        // Integer[] indexes = new Integer[] {1, 2};
        // Integer[] nextMemberIndexes = new Integer[] {3, 4, 5};
        String nextMemberIndexesKey = "345";

        // given(memberHelper.getMemberIndexesKey(member)).willReturn(indexes);
        given(axisDefs.getBreakAfters(dataDef, col)).willReturn(breakAfters);
        given(axisDefs.getIndexRange(dataDef, col)).willReturn(indexRange);
        given(memberHelper.isAxisWithinRange(col, breakAfters, indexRange))
                .willReturn(true);
        // given(memberMatrix.nextMemberIndexes(indexes, col))
        // .willReturn(nextMemberIndexes);
        given(memberHelper.getNextMemberIndexesAsKey(member, col))
                .willReturn(nextMemberIndexesKey);
        given(memberMatrix.notYetCreated(nextMemberIndexesKey))
                .willReturn(false);

        stack.pushAdjacentMembers(dataDef, member);

        Member actual = stack.popMember();

        assertThat(actual).isSameAs(dummyMember);

        verify(memberMatrix, never()).createAdjacentMember(member, col,
                nextMemberIndexesKey);
    }

    @Test
    public void testPushAdjacentMembersAxisNotWithinRange()
            throws DataDefNotFoundException, IllegalAccessException {

        // push some member - to test whether PushAdjacentMembers uses
        // addFirst()
        Member dummyMember = factory.createMember();
        stack.pushMembers(Lists.newArrayList(dummyMember));

        Axis col = factory.createAxis(AxisName.COL, "member1");
        Axis fact = factory.createAxis(AxisName.FACT, "fact");
        Member member = factory.createMember();
        member.addAxis(col);
        member.addAxis(fact);

        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "defJson");

        Optional<List<String>> breakAfters =
                Optional.of(Lists.newArrayList("x", "y"));
        Optional<Range<Integer>> indexRange = Optional.of(Range.between(1, 3));

        // Integer[] indexes = new Integer[] {1, 2};

        // given(memberHelper.getMemberIndexesKey(member)).willReturn(indexes);
        given(axisDefs.getBreakAfters(dataDef, col)).willReturn(breakAfters);
        given(axisDefs.getIndexRange(dataDef, col)).willReturn(indexRange);
        given(memberHelper.isAxisWithinRange(col, breakAfters, indexRange))
                .willReturn(false);

        stack.pushAdjacentMembers(dataDef, member);

        Member actual = stack.popMember();

        assertThat(actual).isSameAs(dummyMember);

        verifyZeroInteractions(memberMatrix);
    }

}
