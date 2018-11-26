package org.codetab.scoopi.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ItemTest {

    private Item item;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        item = new Item();
    }

    @Test
    public void testHashCode() {
        List<Item> testObjects = createTestObjects();
        Item t1 = testObjects.get(0);
        Item t2 = testObjects.get(1);

        String[] excludes = {};
        int expectedHashT1 = HashCodeBuilder.reflectionHashCode(t1, excludes);
        int expectedHashT2 = HashCodeBuilder.reflectionHashCode(t2, excludes);

        assertThat(t1.hashCode()).isEqualTo(expectedHashT1);
        assertThat(t2.hashCode()).isEqualTo(expectedHashT2);
        assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
    }

    @Test
    public void testEqualsObject() {
        List<Item> testObjects = createTestObjects();
        Item t1 = testObjects.get(0);
        Item t2 = testObjects.get(1);

        String[] excludes = {};
        assertThat(EqualsBuilder.reflectionEquals(t1, t2, excludes)).isTrue();

        assertThat(t1).isEqualTo(t2);
        assertThat(t2).isEqualTo(t1);
    }

    @Test
    public void testToString() {
        List<Item> testObjects = createTestObjects();
        Item t1 = testObjects.get(0);

        String expected =
                new ToStringBuilder(t1, ToStringStyle.MULTI_LINE_STYLE)
                        .append("axes", t1.getAxes()).toString();
        assertThat(t1.toString()).isEqualTo(expected);
    }

    @Test
    public void testGetGroup() {
        item.setGroup("x");
        assertThat(item.getGroup()).isEqualTo("x");
    }

    @Test
    public void testGetAxes() {
        List<Axis> axis = item.getAxes();
        assertThat(axis).isNotNull();
    }

    @Test
    public void testGetAxis() {
        Axis col = new Axis("col", "date");
        item.addAxis(col);

        Axis row = new Axis("row", "Price");
        item.addAxis(row);

        assertThat(item.getAxis("col")).isSameAs(col);
        assertThat(item.getAxis("row")).isSameAs(row);
    }

    @Test
    public void testGetAxisThrowException() {
        Axis row = new Axis("row", "Price");
        item.addAxis(row);

        exceptionRule.expect(NoSuchElementException.class);
        item.getAxis("col");
    }

    @Test
    public void testGetAxisMap() {
        Axis col = new Axis("col", "date");
        item.addAxis(col);

        Axis row = new Axis("row", "Price");
        item.addAxis(row);

        Map<String, Axis> axisMap = item.getAxisMap();

        assertThat(axisMap.size()).isEqualTo(2);
        assertThat(axisMap.get("col")).isSameAs(col);
        assertThat(axisMap.get("row")).isSameAs(row);
    }

    @Test
    public void testAddAxis() {
        Axis col = new Axis("col", "date");
        item.addAxis(col);

        Axis row = new Axis("row", "Price");
        item.addAxis(row);

        assertThat(item.getAxis("col")).isSameAs(col);
        assertThat(item.getAxis("row")).isSameAs(row);
    }

    @Test
    public void testGetValue() {
        Axis col = new Axis("col", "date");
        item.addAxis(col);

        Axis row = new Axis("row", "Price");
        item.addAxis(row);

        item.setValue("col", "x");
        item.setValue("row", "y");

        assertThat(item.getValue("col")).isSameAs("x");
        assertThat(item.getValue("row")).isSameAs("y");
    }

    @Test
    public void testGetId() {
        item.setId(10L);
        assertThat(item.getId()).isEqualTo(10L);
    }

    @Test
    public void testTraceItem() {
        Axis col = new Axis("col", "date");
        col.setValue("x");
        item.addAxis(col);

        Axis row = new Axis("row", "Price");
        row.setValue("y");
        item.addAxis(row);

        assertThat(item.traceItem().toString()).isEqualTo(traceString(item));
    }

    private List<Item> createTestObjects() {
        List<Axis> axes = new ArrayList<>();

        Item t1 = new Item();
        t1.setId(1L);
        t1.setName("x");
        t1.setGroup("g");
        t1.setAxes(axes);

        Item t2 = new Item();
        t2.setId(1L);
        t2.setName("x");
        t2.setGroup("g");
        t2.setAxes(axes);

        List<Item> testObjects = new ArrayList<>();
        testObjects.add(t1);
        testObjects.add(t2);
        return testObjects;
    }

    private String traceString(final Item testItem) {
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
