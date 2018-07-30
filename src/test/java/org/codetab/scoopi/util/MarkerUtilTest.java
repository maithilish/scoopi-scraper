package org.codetab.scoopi.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;
import org.slf4j.Marker;

/**
 * <p>
 * MarkerUtil tests.
 * @author Maithilish
 *
 */
public class MarkerUtilTest {

    @Test
    public void testGetMarkerNameGroup() {

        Marker actual = MarkerUtil.getMarker("foo", "bar");

        assertThat(actual.getName()).isEqualTo("LOG_FOO_BAR");
    }

    @Test
    public void testGetMarkerNameGroupNullParams() {
        try {
            MarkerUtil.getMarker(null, "bar");
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("name must not be null");
        }

        try {
            MarkerUtil.getMarker("foo", null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("group must not be null");
        }
    }

    @Test
    public void testGetMarkerNameGroupDataDef() {

        Marker actual = MarkerUtil.getMarker("foo", "bar", "joe");

        assertThat(actual.getName()).isEqualTo("LOG_FOO_BAR_JOE");
    }

    @Test
    public void testGetMarkerNullDataDef() {

        Marker actual = MarkerUtil.getMarker("foo", "bar", null);

        assertThat(actual.getName()).isEqualTo("LOG_FOO_BAR");

    }

    @Test
    public void testGetMarkerNameGroupDataDefNullParams() {
        try {
            MarkerUtil.getMarker(null, "bar", "joe");
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("name must not be null");
        }

        try {
            MarkerUtil.getMarker("foo", null, "joe");
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("group must not be null");
        }
    }

    @Test
    public void testGetMarkerDataDef() {

        Marker actual = MarkerUtil.getMarker("foo");

        assertThat(actual.getName()).isEqualTo("LOG_FOO");
    }

    @Test
    public void testGetMarkerDataDefNullParams() {
        try {
            MarkerUtil.getMarker(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage())
                    .isEqualTo("dataDefName must not be null");
        }
    }

    @Test
    public void testWellDefinedUtilityClass()
            throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        TestUtils.assertUtilityClassWellDefined(MarkerUtil.class);
    }
}
