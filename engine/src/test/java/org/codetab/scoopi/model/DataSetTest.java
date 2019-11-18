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

public class DataSetTest {

    class Enhanced extends DataSet {
        @SuppressWarnings("unused")
        private int dnDetachedState = 1;
        @SuppressWarnings("unused")
        private int dnFlags = 2;
        @SuppressWarnings("unused")
        private int dnStateManager = 3;

        Enhanced(final String name, final String group, final String col,
                final String row, final String fact) {
            super(name, group, col, row, fact);
        }
    }

    private DataSet dataSet;

    @Before
    public void setUp() throws Exception {
        dataSet = new DataSet("name", "group", "col", "row", "fact");
        dataSet.setId(1L);
    }

    @Test
    public void testGetId() {
        assertThat(dataSet.getId()).isEqualTo(1L);
    }

    @Test
    public void testGetName() {
        assertThat(dataSet.getName()).isEqualTo("name");
    }

    @Test
    public void testGetGroup() {
        assertThat(dataSet.getGroup()).isEqualTo("group");
    }

    @Test
    public void testGetCol() {
        assertThat(dataSet.getCol()).isEqualTo("col");
    }

    @Test
    public void testGetRow() {
        assertThat(dataSet.getRow()).isEqualTo("row");
    }

    @Test
    public void testGetFact() {
        assertThat(dataSet.getFact()).isEqualTo("fact");
    }

    @Test
    public void testHashCode() {
        List<Enhanced> testObjects = createTestObjects();
        Enhanced t1 = testObjects.get(0);
        Enhanced t2 = testObjects.get(1);

        String[] excludes = {"id", "fromDate", "toDate", "dnDetachedState",
                "dnFlags", "dnStateManager"};
        int expectedHashT1 = HashCodeBuilder.reflectionHashCode(t1, excludes);
        int expectedHashT2 = HashCodeBuilder.reflectionHashCode(t2, excludes);

        assertThat(t1.hashCode()).isEqualTo(expectedHashT1);
        assertThat(t2.hashCode()).isEqualTo(expectedHashT2);
        assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
    }

    @Test
    public void testEqualsObject() {
        List<Enhanced> testObjects = createTestObjects();
        Enhanced t1 = testObjects.get(0);
        Enhanced t2 = testObjects.get(1);

        String[] excludes = {"id", "fromDate", "toDate", "dnDetachedState",
                "dnFlags", "dnStateManager"};
        assertThat(EqualsBuilder.reflectionEquals(t1, t2, excludes)).isTrue();

        assertThat(t1).isEqualTo(t2);
        assertThat(t2).isEqualTo(t1);
    }

    @Test
    public void testToString() {
        List<Enhanced> testObjects = createTestObjects();
        Enhanced t1 = testObjects.get(0);

        String expected =
                new ToStringBuilder(t1, ToStringStyle.MULTI_LINE_STYLE)
                        .append("id", t1.getId()).append("name", t1.getName())
                        .append("group", t1.getGroup())
                        .append("col", t1.getCol()).append("row", t1.getRow())
                        .append("fact", t1.getFact()).toString();

        assertThat(t1.toString()).isEqualTo(expected);
    }

    private List<Enhanced> createTestObjects() {

        Enhanced t1 = new Enhanced("name", "group", "col", "row", "fact");
        Enhanced t2 = new Enhanced("name", "group", "col", "row", "fact");
        t1.setId(1L);
        t2.setId(2L);
        t2.dnDetachedState = 11;
        t2.dnFlags = 12;
        t2.dnStateManager = 13;

        List<Enhanced> testObjects = new ArrayList<>();
        testObjects.add(t1);
        testObjects.add(t2);
        return testObjects;
    }
}
