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

public class ItemTest {

    private ItemMig itemMig;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        itemMig = new ItemMig();
    }

    @Test
    public void testHashCode() {
        List<ItemMig> testObjects = createTestObjects();
        ItemMig t1 = testObjects.get(0);
        ItemMig t2 = testObjects.get(1);

        String[] excludes = {};
        int expectedHashT1 = HashCodeBuilder.reflectionHashCode(t1, excludes);
        int expectedHashT2 = HashCodeBuilder.reflectionHashCode(t2, excludes);

        assertThat(t1.hashCode()).isEqualTo(expectedHashT1);
        assertThat(t2.hashCode()).isEqualTo(expectedHashT2);
        assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
    }

    @Test
    public void testEqualsObject() {
        List<ItemMig> testObjects = createTestObjects();
        ItemMig t1 = testObjects.get(0);
        ItemMig t2 = testObjects.get(1);

        String[] excludes = {};
        assertThat(EqualsBuilder.reflectionEquals(t1, t2, excludes)).isTrue();

        assertThat(t1).isEqualTo(t2);
        assertThat(t2).isEqualTo(t1);
    }

    @Test
    public void testToString() {
        List<ItemMig> testObjects = createTestObjects();
        ItemMig t1 = testObjects.get(0);

        String expected =
                new ToStringBuilder(t1, ToStringStyle.MULTI_LINE_STYLE)
                        .append("axes", t1.getAxes()).toString();
        assertThat(t1.toString()).isEqualTo(expected);
    }

    @Test
    public void testGetGroup() {
        itemMig.setGroup("x");
        assertThat(itemMig.getGroup()).isEqualTo("x");
    }

    @Test
    public void testGetAxes() {
        Set<Axis> axis = itemMig.getAxes();
        assertThat(axis).isNotNull();
    }

    @Test
    public void testGetAxis() {
        Axis col = new Axis(AxisName.COL, "date");
        itemMig.addAxis(col);

        Axis row = new Axis(AxisName.ROW, "Price");
        itemMig.addAxis(row);

        assertThat(itemMig.getAxis(AxisName.COL)).isSameAs(col);
        assertThat(itemMig.getAxis(AxisName.ROW)).isSameAs(row);
    }

    @Test
    public void testGetAxisThrowException() {
        exceptionRule.expect(NoSuchElementException.class);
        itemMig.getAxis(AxisName.COL);
    }

    @Test
    public void testGetAxisMap() {
        Axis col = new Axis(AxisName.COL, "date");
        itemMig.addAxis(col);

        Axis row = new Axis(AxisName.ROW, "Price");
        itemMig.addAxis(row);

        Map<String, Axis> axisMap = itemMig.getAxisMap();

        assertThat(axisMap.size()).isEqualTo(2);
        assertThat(axisMap.get("COL")).isSameAs(col);
        assertThat(axisMap.get("ROW")).isSameAs(row);
    }

    @Test
    public void testAddAxis() {
        Axis col = new Axis(AxisName.COL, "date");
        itemMig.addAxis(col);

        Axis row = new Axis(AxisName.ROW, "Price");
        itemMig.addAxis(row);

        assertThat(itemMig.getAxis(AxisName.COL)).isSameAs(col);
        assertThat(itemMig.getAxis(AxisName.ROW)).isSameAs(row);
    }

    @Test
    public void testGetValue() {
        Axis col = new Axis(AxisName.COL, "date");
        itemMig.addAxis(col);

        Axis row = new Axis(AxisName.ROW, "Price");
        itemMig.addAxis(row);

        itemMig.setValue(AxisName.COL, "x");
        itemMig.setValue(AxisName.ROW, "y");

        assertThat(itemMig.getValue(AxisName.COL)).isSameAs("x");
        assertThat(itemMig.getValue(AxisName.ROW)).isSameAs("y");
    }

    @Test
    public void testGetId() {
        itemMig.setId(10L);
        assertThat(itemMig.getId()).isEqualTo(10L);
    }

    @Test
    public void testTraceItem() {
        Axis col = new Axis(AxisName.COL, "date");
        col.setValue("x");
        itemMig.addAxis(col);

        Axis row = new Axis(AxisName.ROW, "Price");
        row.setValue("y");
        itemMig.addAxis(row);

        assertThat(itemMig.traceItem().toString())
                .isEqualTo(traceString(itemMig));
    }

    private List<ItemMig> createTestObjects() {
        Set<Axis> axes = new HashSet<>();

        ItemMig t1 = new ItemMig();
        t1.setId(1L);
        t1.setName("x");
        t1.setGroup("g");
        t1.setAxes(axes);

        ItemMig t2 = new ItemMig();
        t2.setId(1L);
        t2.setName("x");
        t2.setGroup("g");
        t2.setAxes(axes);

        List<ItemMig> testObjects = new ArrayList<>();
        testObjects.add(t1);
        testObjects.add(t2);
        return testObjects;
    }

    private String traceString(final ItemMig testItem) {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append("Item=[name=");
        sb.append(testItem.getName());
        sb.append(",group=");
        sb.append(testItem.getGroup());
        sb.append("]");
        sb.append(nl);
        testItem.getAxes().stream().forEach(sb::append);
        return sb.toString();
    }
}
