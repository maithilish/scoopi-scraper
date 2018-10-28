package org.codetab.scoopi.defs.yml.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.Filter;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.util.TestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AxisDefsHelperTest {

    @Spy
    private JsonNodeHelper jsonNodeHelper;
    @Spy
    private ObjectFactory objectFactory;

    @InjectMocks
    private AxisDefsHelper axisDefsHelper;

    private static ObjectFactory factory;
    private static ObjectMapper mapper;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass() {
        mapper = new ObjectMapper();
        factory = new ObjectFactory();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetQuery() throws IOException {
        DataDef dataDef = getTestDataDef();

        assertThat(axisDefsHelper.getQuery(dataDef, AxisName.FACT, "region"))
                .isEqualTo("queryregion");
        assertThat(axisDefsHelper.getQuery(dataDef, AxisName.FACT, "field"))
                .isEqualTo("queryfield");
        assertThat(axisDefsHelper.getQuery(dataDef, AxisName.COL, "script"))
                .isEqualTo("colscript");
    }

    @Test
    public void testGetQueryNoQuery() throws IOException {
        DataDef dataDef = getTestDataDef();

        testRule.expect(NoSuchElementException.class);
        axisDefsHelper.getQuery(dataDef, AxisName.ROW, "region");
    }

    @Test
    public void testGetBreakAfters() throws IOException {
        DataDef dataDef = getTestDataDef();
        Axis axis = factory.createAxis(AxisName.COL, "item");

        Optional<List<String>> actual =
                axisDefsHelper.getBreakAfters(dataDef, axis);

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).containsExactly("break1", "break2");
    }

    @Test
    public void testGetBreakAftersNotDefined() throws IOException {
        DataDef dataDef = getTestDataDef();
        Axis axis = factory.createAxis(AxisName.ROW, "Xyz");

        Optional<List<String>> actual =
                axisDefsHelper.getBreakAfters(dataDef, axis);
        assertThat(actual.isPresent()).isFalse();

        axis = factory.createAxis(AxisName.ROW, "Price");

        actual = axisDefsHelper.getBreakAfters(dataDef, axis);
        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void testGetIndexRange() throws IOException {
        DataDef dataDef = getTestDataDef();
        Axis axis = factory.createAxis(AxisName.COL, "item");

        Optional<Range<Integer>> actual =
                axisDefsHelper.getIndexRange(dataDef, axis);

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get().getMinimum()).isEqualTo(2);
        assertThat(actual.get().getMaximum()).isEqualTo(5);
    }

    @Test
    public void testGetIndexRangeNotDefined() throws IOException {
        DataDef dataDef = getTestDataDef();
        Axis axis = factory.createAxis(AxisName.ROW, "Xyz");

        Optional<Range<Integer>> actual =
                axisDefsHelper.getIndexRange(dataDef, axis);
        assertThat(actual.isPresent()).isFalse();

        axis = factory.createAxis(AxisName.ROW, "Price");

        actual = axisDefsHelper.getIndexRange(dataDef, axis);
        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void testGetPrefixes() throws IOException {
        DataDef dataDef = getTestDataDef();

        Optional<List<String>> actual =
                axisDefsHelper.getPrefixes(dataDef, AxisName.ROW);

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).containsExactly("p1", "p2");
    }

    @Test
    public void testGetFilters() throws IOException {
        DataDef dataDef = getTestDataDef();

        Filter expected1 = factory.createFilter("value", "f1");
        Filter expected2 = factory.createFilter("match", "f2");

        Map<AxisName, List<Filter>> actual = axisDefsHelper.getFilters(dataDef);

        assertThat(actual.size()).isEqualTo(3);

        assertThat(actual.get(AxisName.FACT).size()).isEqualTo(0);
        assertThat(actual.get(AxisName.COL).size()).isEqualTo(0);
        assertThat(actual.get(AxisName.ROW).size()).isEqualTo(2);

        assertThat(actual.get(AxisName.ROW)).containsExactly(expected1,
                expected2);
    }

    private DataDef getTestDataDef() throws IOException {
        Date runDate = new Date();
        Date fromDate = DateUtils.addDays(runDate, -1);
        Date toDate = DateUtils.addDays(runDate, 1);
        String defJson = new StringBuilder().append("{axis:")
                .append("{fact:{query:{region:queryregion,field:queryfield},")
                .append("items:[{item:{name:fact,index:0,order:10}}]},")
                .append("col:{query:{script:colscript},")
                .append("items:[{item:{name: item, breakAfter: ['break1','break2'], ")
                .append("indexRange: 2-5, order:11}}]},")
                .append("row:{query:{noQuery: ignored},")
                .append("items:[{item:{name:Price,value:Price,index:2,order:12}},")
                .append("{item:{name:High,match:High}}],")
                .append("prefix: [ p1, p2 ], ")
                .append("filters: [{filter: {type: value, pattern: f1 }}, ")
                .append("{filter: {type: match, pattern: f2} }]").append("} }}")
                .toString();
        JsonNode def = mapper.readTree(TestUtils.parseJson(defJson));
        DataDef dataDef =
                factory.createDataDef("price", fromDate, toDate, defJson);
        dataDef.setDef(def);
        return dataDef;
    }
}
