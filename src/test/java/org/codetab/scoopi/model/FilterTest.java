package org.codetab.scoopi.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Before;
import org.junit.Test;

public class FilterTest {

    private Filter filter;

    @Before
    public void setUp() throws Exception {
        filter = new Filter();
    }

    @Test
    public void testGetType() {
        String type = "value";
        filter.setType(type);
        assertThat(filter.getType()).isEqualTo(type);
    }

    @Test
    public void testGetPattern() {
        String pattern = "x";
        filter.setPattern(pattern);
        assertThat(filter.getPattern()).isEqualTo(pattern);
    }

    @Test
    public void testEqualsObject() {
        String type = "v";
        String pattern = "x";
        filter.setType(type);
        filter.setPattern(pattern);

        Filter other = new Filter();
        other.setType(type);
        other.setPattern(pattern);
        assertThat(filter).isEqualTo(other);
    }

    @Test
    public void testHashCode() {
        String type = "v";
        String pattern = "x";
        filter.setType(type);
        filter.setPattern(pattern);

        int expected = HashCodeBuilder.reflectionHashCode(filter);
        assertThat(filter.hashCode()).isEqualTo(expected);
    }

    @Test
    public void testToString() {
        String type = "v";
        String pattern = "x";
        filter.setType(type);
        filter.setPattern(pattern);

        assertThat(filter.toString()).isEqualTo("Filter[type=v,pattern=x]");
    }

}
