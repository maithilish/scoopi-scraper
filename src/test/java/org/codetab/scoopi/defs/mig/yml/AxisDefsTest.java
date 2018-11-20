package org.codetab.scoopi.defs.mig.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.Range;
import org.codetab.scoopi.defs.mig.yml.AxisDefs;
import org.codetab.scoopi.defs.mig.yml.cache.AxisDefsCache;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.ObjectFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

public class AxisDefsTest {

    @Mock
    private AxisDefsCache axisDefsCache;

    @InjectMocks
    private AxisDefs axisDefs;

    private static ObjectFactory factory;

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
        String query = "test query";
        DataDef dataDef =
                factory.createDataDef("price", new Date(), new Date(), "def");

        given(axisDefsCache.getQuery(dataDef, AxisName.COL, "region"))
                .willReturn(query);

        String actual = axisDefs.getQuery(dataDef, AxisName.COL, "region");

        assertThat(actual).isEqualTo(query);
    }

    @Test
    public void testGetBreakAfters() throws IllegalAccessException {
        Axis col = factory.createAxis(AxisName.COL, "date");
        DataDef dataDef =
                factory.createDataDef("price", new Date(), new Date(), "def");
        Optional<List<String>> breakAfters =
                Optional.of(Lists.newArrayList("ba1", "ba2"));

        given(axisDefsCache.getBreakAfters(dataDef, col))
                .willReturn(breakAfters);

        Optional<List<String>> actual = axisDefs.getBreakAfters(dataDef, col);

        assertThat(actual).isEqualTo(breakAfters);
    }

    @Test
    public void testGetIndexRange() throws IllegalAccessException {
        Axis col = factory.createAxis(AxisName.COL, "date");
        DataDef dataDef =
                factory.createDataDef("price", new Date(), new Date(), "def");
        Optional<Range<Integer>> indexRange =
                Optional.of(Range.between(10, 20));

        given(axisDefsCache.getIndexRange(dataDef, col)).willReturn(indexRange);

        Optional<Range<Integer>> actual = axisDefs.getIndexRange(dataDef, col);

        assertThat(actual).isEqualTo(indexRange);
    }

    @Test
    public void testGetPrefixes() throws IllegalAccessException {
        DataDef dataDef =
                factory.createDataDef("price", new Date(), new Date(), "def");
        Optional<List<String>> prefixes =
                Optional.of(Lists.newArrayList("p1", "p2"));

        given(axisDefsCache.getPrefixes(dataDef, AxisName.COL))
                .willReturn(prefixes);

        Optional<List<String>> actual =
                axisDefs.getPrefixes(dataDef, AxisName.COL);

        assertThat(actual).isEqualTo(prefixes);
    }
}
