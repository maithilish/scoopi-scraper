package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang3.Range;
import org.junit.Before;
import org.junit.Test;

public class IndexRangeFactoryTest {

    private IndexRangeFactory indexRange;

    @Before
    public void setUp() throws Exception {
        indexRange = new IndexRangeFactory();
    }

    @Test
    public void testCreateRangeNoDash() {
        Range<Integer> actual = indexRange.createRange("10");
        assertThat(actual.getMinimum()).isEqualTo(10);
        assertThat(actual.getMaximum()).isEqualTo(10);
    }

    @Test
    public void testCreateRange() {
        Range<Integer> actual = indexRange.createRange("5-20");
        assertThat(actual.getMinimum()).isEqualTo(5);
        assertThat(actual.getMaximum()).isEqualTo(20);
    }

    @Test
    public void testCreateRangeMaxValue() {
        Range<Integer> actual = indexRange.createRange("10-");
        assertThat(actual.getMinimum()).isEqualTo(10);
        assertThat(actual.getMaximum()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    public void testCreateRangeZero() {
        Range<Integer> actual = indexRange.createRange("-");
        assertThat(actual.getMinimum()).isEqualTo(0);
        assertThat(actual.getMaximum()).isEqualTo(0);
    }
}
