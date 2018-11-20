package org.codetab.scoopi.defs.mig.yml.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.commons.lang3.Range;
import org.codetab.scoopi.defs.mig.yml.cache.AxisDefsCache;
import org.codetab.scoopi.defs.mig.yml.helper.AxisDefsHelper;
import org.codetab.scoopi.defs.yml.DataDefDef;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;
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

public class AxisDefsCacheTest {

    @Mock
    private AxisDefsHelper axisDefsHelper;
    @Mock
    private DataDefDef dataDefDef;

    @InjectMocks
    private AxisDefsCache axisDefsCache;

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
    public void testGetQuery() throws IllegalAccessException {
        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "def");
        String query = "test query";

        given(axisDefsHelper.getQuery(dataDef, AxisName.COL, "region"))
                .willReturn(query);

        // from def
        String actual = axisDefsCache.getQuery(dataDef, AxisName.COL, "region");
        assertThat(actual).isEqualTo(query);
    }

    @Test
    public void testGetQueryFromCache() throws IllegalAccessException {
        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "def");
        String query = "test query";

        given(axisDefsHelper.getQuery(dataDef, AxisName.COL, "region"))
                .willReturn(query);

        // from def
        String actual = axisDefsCache.getQuery(dataDef, AxisName.COL, "region");
        assertThat(actual).isEqualTo(query);

        // from cache
        actual = axisDefsCache.getQuery(dataDef, AxisName.COL, "region");
        assertThat(actual).isEqualTo(query);

        verify(axisDefsHelper, times(1)).getQuery(dataDef, AxisName.COL,
                "region");
    }

    @Test
    public void testGetQueryNotDefined() throws IllegalAccessException {
        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "def");

        given(axisDefsHelper.getQuery(dataDef, AxisName.COL, "region"))
                .willThrow(NoSuchElementException.class);

        // from def
        String actual = axisDefsCache.getQuery(dataDef, AxisName.COL, "region");

        assertThat(actual).isEqualTo("undefined");

        actual = axisDefsCache.getQuery(dataDef, AxisName.COL, "region");

        assertThat(actual).isEqualTo("undefined");
        verify(axisDefsHelper, times(1)).getQuery(dataDef, AxisName.COL,
                "region");
    }

    @Test
    public void testGetBreakAfters() throws IllegalAccessException {
        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "def");
        Axis col = factory.createAxis(AxisName.COL, "date");
        Optional<List<String>> breakAfters =
                Optional.of(Lists.newArrayList("x", "y"));

        given(axisDefsHelper.getBreakAfters(dataDef, col))
                .willReturn(breakAfters);

        // from def
        Optional<List<String>> actual =
                axisDefsCache.getBreakAfters(dataDef, col);

        assertThat(actual).isEqualTo(breakAfters);
    }

    @Test
    public void testGetBreakAftersFromCache() throws IllegalAccessException {
        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "def");
        Axis col = factory.createAxis(AxisName.COL, "date");
        Optional<List<String>> breakAfters =
                Optional.of(Lists.newArrayList("x", "y"));

        given(axisDefsHelper.getBreakAfters(dataDef, col))
                .willReturn(breakAfters);

        // from def
        Optional<List<String>> actual =
                axisDefsCache.getBreakAfters(dataDef, col);

        assertThat(actual).isEqualTo(breakAfters);

        // from cache
        actual = axisDefsCache.getBreakAfters(dataDef, col);
        assertThat(actual).isEqualTo(breakAfters);

        verify(axisDefsHelper, times(1)).getBreakAfters(dataDef, col);
    }

    @Test
    public void testGetBreakAftersNotDefined() throws IllegalAccessException {
        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "def");
        Axis col = factory.createAxis(AxisName.COL, "date");

        given(axisDefsHelper.getBreakAfters(dataDef, col))
                .willThrow(NoSuchElementException.class);

        Optional<List<String>> actual =
                axisDefsCache.getBreakAfters(dataDef, col);
        assertThat(actual).isEmpty();

        actual = axisDefsCache.getBreakAfters(dataDef, col);
        assertThat(actual).isEmpty();
        verify(axisDefsHelper, times(1)).getBreakAfters(dataDef, col);
    }

    @Test
    public void testGetIndexRange() throws IllegalAccessException {
        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "def");
        Axis col = factory.createAxis(AxisName.COL, "date");
        Optional<Range<Integer>> indexRange = Optional.of(Range.between(1, 3));

        given(axisDefsHelper.getIndexRange(dataDef, col))
                .willReturn(indexRange);

        // from def
        Optional<Range<Integer>> actual =
                axisDefsCache.getIndexRange(dataDef, col);

        assertThat(actual).isEqualTo(indexRange);
    }

    @Test
    public void testGetIndexRangeFromCache() throws IllegalAccessException {
        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "def");
        Axis col = factory.createAxis(AxisName.COL, "date");
        Optional<Range<Integer>> indexRange = Optional.of(Range.between(1, 3));

        given(axisDefsHelper.getIndexRange(dataDef, col))
                .willReturn(indexRange);

        // from def
        Optional<Range<Integer>> actual =
                axisDefsCache.getIndexRange(dataDef, col);

        assertThat(actual).isEqualTo(indexRange);

        // cache
        actual = axisDefsCache.getIndexRange(dataDef, col);

        assertThat(actual).isEqualTo(indexRange);

        verify(axisDefsHelper, times(1)).getIndexRange(dataDef, col);
    }

    @Test
    public void testGetIndexRangeNotDefined() throws IllegalAccessException {
        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "def");
        Axis col = factory.createAxis(AxisName.COL, "date");
        Optional<Range<Integer>> indexRange = Optional.empty();

        given(axisDefsHelper.getIndexRange(dataDef, col))
                .willThrow(NoSuchElementException.class);

        // from def
        Optional<Range<Integer>> actual =
                axisDefsCache.getIndexRange(dataDef, col);

        assertThat(actual).isEqualTo(indexRange);

        // cache
        actual = axisDefsCache.getIndexRange(dataDef, col);

        assertThat(actual).isEqualTo(indexRange);

        verify(axisDefsHelper, times(1)).getIndexRange(dataDef, col);
    }

    @Test
    public void testGetPrefixes() throws IllegalAccessException {
        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "def");
        Optional<List<String>> prefixes =
                Optional.of(Lists.newArrayList("p1", "p2"));

        given(axisDefsHelper.getPrefixes(dataDef, AxisName.COL))
                .willReturn(prefixes);

        // from def
        Optional<List<String>> actual =
                axisDefsCache.getPrefixes(dataDef, AxisName.COL);
        assertThat(actual).isEqualTo(prefixes);
    }

    @Test
    public void testGetPrefixesFromCache() throws IllegalAccessException {
        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "def");
        Optional<List<String>> prefixes =
                Optional.of(Lists.newArrayList("p1", "p2"));

        given(axisDefsHelper.getPrefixes(dataDef, AxisName.COL))
                .willReturn(prefixes);

        // from def
        Optional<List<String>> actual =
                axisDefsCache.getPrefixes(dataDef, AxisName.COL);
        assertThat(actual).isEqualTo(prefixes);

        // from cache
        actual = axisDefsCache.getPrefixes(dataDef, AxisName.COL);
        assertThat(actual).isEqualTo(prefixes);

        verify(axisDefsHelper, times(1)).getPrefixes(dataDef, AxisName.COL);
    }

    @Test
    public void testGetPrefixesNotDefined() throws IllegalAccessException {
        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "def");

        given(axisDefsHelper.getPrefixes(dataDef, AxisName.COL))
                .willThrow(NoSuchElementException.class);

        Optional<List<String>> actual =
                axisDefsCache.getPrefixes(dataDef, AxisName.COL);
        assertThat(actual).isEmpty();

        actual = axisDefsCache.getPrefixes(dataDef, AxisName.COL);
        assertThat(actual).isEmpty();

        verify(axisDefsHelper, times(1)).getPrefixes(dataDef, AxisName.COL);
    }
}
