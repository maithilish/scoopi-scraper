package org.codetab.scoopi.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Before;
import org.junit.Test;

public class AxisTest {

    private Axis axis;

    @Before
    public void setUp() throws Exception {
        axis = new Axis(AxisName.COL, "date");
    }

    @Test
    public void testGetName() {
        assertThat(axis.getName()).isEqualTo(AxisName.COL);
    }

    @Test
    public void testGetValue() {
        axis.setValue("x");
        assertThat(axis.getValue()).isEqualTo("x");
    }

    @Test
    public void testGetMatch() {
        axis.setMatch("x");
        assertThat(axis.getMatch()).isEqualTo("x");
    }

    @Test
    public void testGetIndex() {
        axis.setIndex(1);
        assertThat(axis.getIndex()).isEqualTo(1);
    }

    @Test
    public void testGetOrder() {
        axis.setOrder(2);
        assertThat(axis.getOrder()).isEqualTo(2);
    }

    @Test
    public void testCompareTo() {
        Axis a1 = new Axis(AxisName.COL, "date");
        Axis a2 = new Axis(AxisName.COL, "date");

        assertThat(a1.compareTo(a2)).isEqualTo(0);

        a1 = new Axis(AxisName.COL, "date");
        a2 = new Axis(AxisName.ROW, "Price");
        assertThat(a1.compareTo(a2)).isEqualTo(-1);

        a1 = new Axis(AxisName.ROW, "Price");
        a2 = new Axis(AxisName.COL, "date");
        assertThat(a1.compareTo(a2)).isEqualTo(1);
    }

    @Test
    public void testHashCode() {
        List<Axis> testObjects = createTestObjects();
        Axis t1 = testObjects.get(0);
        Axis t2 = testObjects.get(1);

        String[] excludes = {};
        int expectedHashT1 = HashCodeBuilder.reflectionHashCode(t1, excludes);
        int expectedHashT2 = HashCodeBuilder.reflectionHashCode(t2, excludes);

        assertThat(t1.hashCode()).isEqualTo(expectedHashT1);
        assertThat(t2.hashCode()).isEqualTo(expectedHashT2);
        assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
    }

    @Test
    public void testEqualsObject() {
        List<Axis> testObjects = createTestObjects();
        Axis t1 = testObjects.get(0);
        Axis t2 = testObjects.get(1);

        String[] excludes = {};
        assertThat(EqualsBuilder.reflectionEquals(t1, t2, excludes)).isTrue();

        assertThat(t1).isEqualTo(t2);
        assertThat(t2).isEqualTo(t1);
    }

    @Test
    public void testToString() {
        List<Axis> testObjects = createTestObjects();
        Axis t1 = testObjects.get(0);

        String expected = expectedString(t1);
        assertThat(t1.toString()).isEqualTo(expected);
    }

    private List<Axis> createTestObjects() {
        Axis t1 = new Axis(AxisName.COL, "date");
        t1.setMatch("m");
        t1.setValue("v");
        t1.setIndex(1);
        t1.setOrder(2);

        Axis t2 = new Axis(AxisName.COL, "date");
        t2.setMatch("m");
        t2.setValue("v");
        t2.setIndex(1);
        t2.setOrder(2);

        List<Axis> testObjects = new ArrayList<>();
        testObjects.add(t1);
        testObjects.add(t2);
        return testObjects;
    }

    private String expectedString(final Axis testAxis) {
        String str =
                new ToStringBuilder(testAxis, ToStringStyle.SHORT_PREFIX_STYLE)
                        .append("name", testAxis.getName())
                        .append("memberName", testAxis.getMemberName())
                        .append("value", testAxis.getValue())
                        .append("match", testAxis.getMatch())
                        .append("index", testAxis.getIndex())
                        .append("order", testAxis.getOrder()).toString();
        return System.lineSeparator() + "  " + str;

    }
}
