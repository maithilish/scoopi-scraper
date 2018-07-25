package org.codetab.scoopi.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MemberTest {

    private Member member;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        member = new Member();
    }

    @Test
    public void testHashCode() {
        List<Member> testObjects = createTestObjects();
        Member t1 = testObjects.get(0);
        Member t2 = testObjects.get(1);

        String[] excludes = {};
        int expectedHashT1 = HashCodeBuilder.reflectionHashCode(t1, excludes);
        int expectedHashT2 = HashCodeBuilder.reflectionHashCode(t2, excludes);

        assertThat(t1.hashCode()).isEqualTo(expectedHashT1);
        assertThat(t2.hashCode()).isEqualTo(expectedHashT2);
        assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
    }

    @Test
    public void testEqualsObject() {
        List<Member> testObjects = createTestObjects();
        Member t1 = testObjects.get(0);
        Member t2 = testObjects.get(1);

        String[] excludes = {};
        assertThat(EqualsBuilder.reflectionEquals(t1, t2, excludes)).isTrue();

        assertThat(t1).isEqualTo(t2);
        assertThat(t2).isEqualTo(t1);
    }

    @Test
    public void testToString() {
        List<Member> testObjects = createTestObjects();
        Member t1 = testObjects.get(0);

        String expected =
                new ToStringBuilder(t1, ToStringStyle.MULTI_LINE_STYLE)
                        .append("axes", t1.getAxes()).toString();
        assertThat(t1.toString()).isEqualTo(expected);
    }

    @Test
    public void testGetGroup() {
        member.setGroup("x");
        assertThat(member.getGroup()).isEqualTo("x");
    }

    @Test
    public void testGetAxes() {
        Set<Axis> axis = member.getAxes();
        assertThat(axis).isNotNull();
    }

    @Test
    public void testGetAxis() {
        Axis col = new Axis();
        col.setName(AxisName.COL);
        member.addAxis(col);

        Axis row = new Axis();
        row.setName(AxisName.ROW);
        member.addAxis(row);

        assertThat(member.getAxis(AxisName.COL)).isSameAs(col);
        assertThat(member.getAxis(AxisName.ROW)).isSameAs(row);
    }

    @Test
    public void testGetAxisThrowException() {
        exceptionRule.expect(NoSuchElementException.class);
        member.getAxis(AxisName.COL);
    }

    @Test
    public void testGetAxisMap() {
        Axis col = new Axis();
        col.setName(AxisName.COL);
        member.addAxis(col);

        Axis row = new Axis();
        row.setName(AxisName.ROW);
        member.addAxis(row);

        Map<String, Axis> axisMap = member.getAxisMap();

        assertThat(axisMap.size()).isEqualTo(2);
        assertThat(axisMap.get("COL")).isSameAs(col);
        assertThat(axisMap.get("ROW")).isSameAs(row);
    }

    @Test
    public void testAddAxis() {
        Axis col = new Axis();
        col.setName(AxisName.COL);
        member.addAxis(col);

        Axis row = new Axis();
        row.setName(AxisName.ROW);
        member.addAxis(row);

        assertThat(member.getAxis(AxisName.COL)).isSameAs(col);
        assertThat(member.getAxis(AxisName.ROW)).isSameAs(row);
    }

    @Test
    public void testGetValue() {
        Axis col = new Axis();
        col.setName(AxisName.COL);
        member.addAxis(col);

        Axis row = new Axis();
        row.setName(AxisName.ROW);
        member.addAxis(row);

        member.setValue(AxisName.COL, "x");
        member.setValue(AxisName.ROW, "y");

        assertThat(member.getValue(AxisName.COL)).isSameAs("x");
        assertThat(member.getValue(AxisName.ROW)).isSameAs("y");
    }

    @Test
    public void testGetId() {
        member.setId(10L);
        assertThat(member.getId()).isEqualTo(10L);
    }

    @Test
    public void testTraceMember() {
        Axis col = new Axis();
        col.setName(AxisName.COL);
        col.setValue("x");
        member.addAxis(col);

        Axis row = new Axis();
        row.setName(AxisName.ROW);
        row.setValue("y");
        member.addAxis(row);

        assertThat(member.traceMember().toString())
                .isEqualTo(traceString(member));
    }

    private List<Member> createTestObjects() {
        Set<Axis> axes = new HashSet<>();

        Member t1 = new Member();
        t1.setId(1L);
        t1.setName("x");
        t1.setGroup("g");
        t1.setAxes(axes);

        Member t2 = new Member();
        t2.setId(1L);
        t2.setName("x");
        t2.setGroup("g");
        t2.setAxes(axes);

        List<Member> testObjects = new ArrayList<>();
        testObjects.add(t1);
        testObjects.add(t2);
        return testObjects;
    }

    private String traceString(final Member testMember) {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append("Member=[name=");
        sb.append(testMember.getName());
        sb.append(",group=");
        sb.append(testMember.getGroup());
        sb.append("]");
        sb.append(nl);
        testMember.getAxes().stream().forEach(sb::append);
        return sb.toString();
    }
}
