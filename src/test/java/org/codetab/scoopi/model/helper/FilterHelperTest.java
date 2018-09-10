package org.codetab.scoopi.model.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codetab.scoopi.defs.IAxisDefs;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.Filter;
import org.codetab.scoopi.model.Member;
import org.codetab.scoopi.model.ObjectFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

public class FilterHelperTest {

    @Mock
    private IAxisDefs axisDefs;

    @InjectMocks
    private FilterHelper filterHelper;

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
    public void testGetFilterMap() {
        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "defJson");
        Map<AxisName, List<Filter>> filterMap = new HashMap<>();

        given(axisDefs.getFilterMap(dataDef)).willReturn(filterMap);

        Map<AxisName, List<Filter>> actual = filterHelper.getFilterMap(dataDef);

        assertThat(actual).isSameAs(filterMap);
    }

    @Test
    public void testGetFilterMembersByValue() {
        Filter filter1 = factory.createFilter("value", "r1");
        Filter filter2 = factory.createFilter("value", "r3");
        List<Filter> filters = Lists.newArrayList(filter1, filter2);
        Map<AxisName, List<Filter>> filterMap = new HashMap<>();
        filterMap.put(AxisName.ROW, filters);

        List<Member> members = createTestMembers();

        List<Member> actual = filterHelper.getFilterMembers(members, filterMap);

        assertThat(actual.size()).isEqualTo(2);
        assertThat(actual.get(0)).isSameAs(members.get(0));
        assertThat(actual.get(1)).isSameAs(members.get(2));

        filter1 = factory.createFilter("value", "c1");
        filter2 = factory.createFilter("value", "c2");
        Filter filter3 = factory.createFilter("value", "c10");
        filters = Lists.newArrayList(filter1, filter2, filter3);

        filterMap.put(AxisName.COL, filters);

        actual = filterHelper.getFilterMembers(members, filterMap);

        assertThat(actual.size()).isEqualTo(3);
        assertThat(actual.get(0)).isSameAs(members.get(0));
        assertThat(actual.get(1)).isSameAs(members.get(1));
        assertThat(actual.get(2)).isSameAs(members.get(2));
    }

    @Test
    public void testGetFilterMembersPattern() {
        Filter filter1 = factory.createFilter("value", "r[1-2]");
        List<Filter> filters = Lists.newArrayList(filter1);
        Map<AxisName, List<Filter>> filterMap = new HashMap<>();
        filterMap.put(AxisName.ROW, filters);

        List<Member> members = createTestMembers();

        List<Member> actual = filterHelper.getFilterMembers(members, filterMap);

        assertThat(actual.size()).isEqualTo(2);
        assertThat(actual.get(0)).isSameAs(members.get(0));
        assertThat(actual.get(1)).isSameAs(members.get(1));
    }

    @Test
    public void testGetFilterMembersPatternShouldThrowException() {
        Filter filter1 = factory.createFilter("value", "r][");
        List<Filter> filters = Lists.newArrayList(filter1);
        Map<AxisName, List<Filter>> filterMap = new HashMap<>();
        filterMap.put(AxisName.ROW, filters);

        List<Member> members = createTestMembers();

        testRule.expect(StepRunException.class);
        filterHelper.getFilterMembers(members, filterMap);
    }

    @Test
    public void testGetFilterMembersByMatch() {
        Filter filter1 = factory.createFilter("match", "r4");
        Filter filter2 = factory.createFilter("match", "r6");
        List<Filter> filters = Lists.newArrayList(filter1, filter2);
        Map<AxisName, List<Filter>> filterMap = new HashMap<>();
        filterMap.put(AxisName.ROW, filters);

        List<Member> members = createTestMembers();

        List<Member> actual = filterHelper.getFilterMembers(members, filterMap);

        assertThat(actual.size()).isEqualTo(2);
        assertThat(actual.get(0)).isSameAs(members.get(3));
        assertThat(actual.get(1)).isSameAs(members.get(5));

        filter1 = factory.createFilter("match", "c4");
        filter2 = factory.createFilter("match", "c5");
        Filter filter3 = factory.createFilter("match", "c10");
        filters = Lists.newArrayList(filter1, filter2, filter3);

        filterMap.put(AxisName.COL, filters);

        actual = filterHelper.getFilterMembers(members, filterMap);

        assertThat(actual.size()).isEqualTo(3);
        assertThat(actual.get(0)).isSameAs(members.get(3));
        assertThat(actual.get(1)).isSameAs(members.get(4));
        assertThat(actual.get(2)).isSameAs(members.get(5));
    }

    @Test
    public void testFilter() {
        List<Member> members = createTestMembers();

        Member m1 = members.get(0);
        Member m2 = members.get(1);
        Member m3 = members.get(2);
        Member m4 = members.get(3);
        Member m5 = members.get(4);
        Member m6 = members.get(5);

        ArrayList<Member> filterMembers = Lists.newArrayList(m1, m2, m5);

        filterHelper.filter(members, filterMembers);

        assertThat(members.size()).isEqualTo(3);
        assertThat(members).doesNotContain(m1, m2, m5);
        assertThat(members).containsExactly(m3, m4, m6);
    }

    private List<Member> createTestMembers() {
        List<Member> members = new ArrayList<>();

        members.add(createTestMember("value", "1"));
        members.add(createTestMember("value", "2"));
        members.add(createTestMember("value", "3"));

        members.add(createTestMember("match", "4"));
        members.add(createTestMember("match", "5"));
        members.add(createTestMember("match", "6"));
        return members;
    }

    private Member createTestMember(final String type, final String index) {
        Member member = factory.createMember();
        String mName = "m" + index;
        String colValue = "c" + index;
        String colMatch = null;
        String rowValue = "r" + index;
        String rowMatch = null;
        String factValue = "f" + index;
        String factMatch = null;
        if (type.equals("match")) {
            colValue = null;
            colMatch = "c" + index;
            rowValue = null;
            rowMatch = "r" + index;
            factValue = null;
            factMatch = "f" + index;
        }

        Axis col = factory.createAxis(AxisName.COL, mName, colValue, colMatch,
                0, 0);
        Axis row = factory.createAxis(AxisName.ROW, mName, rowValue, rowMatch,
                0, 0);
        Axis fact = factory.createAxis(AxisName.FACT, mName, factValue,
                factMatch, 0, 0);

        member.addAxis(col);
        member.addAxis(row);
        member.addAxis(fact);
        return member;
    }

}
