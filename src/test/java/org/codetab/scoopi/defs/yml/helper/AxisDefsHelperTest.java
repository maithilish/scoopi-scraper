package org.codetab.scoopi.defs.yml.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;
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

        List<String> actual = axisDefsHelper.getBreakAfters(dataDef, axis);

        assertThat(actual).containsExactly("break1", "break2");
    }

    @Test
    public void testGetBreakAftersNotDefined() throws IOException {
        DataDef dataDef = getTestDataDef();
        Axis axis = factory.createAxis(AxisName.ROW, "Xyz");

        List<String> actual = axisDefsHelper.getBreakAfters(dataDef, axis);
        assertThat(actual).isEmpty();

        axis = factory.createAxis(AxisName.ROW, "Price");

        actual = axisDefsHelper.getBreakAfters(dataDef, axis);
        assertThat(actual).isEmpty();
    }

    @Test
    public void testGetIndexRange() throws IOException {
        DataDef dataDef = getTestDataDef();
        Axis axis = factory.createAxis(AxisName.COL, "item");

        Range<Integer> actual = axisDefsHelper.getIndexRange(dataDef, axis);

        assertThat(actual.getMinimum()).isEqualTo(2);
        assertThat(actual.getMaximum()).isEqualTo(5);
    }

    @Test
    public void testGetIndexRangeNotDefined() throws IOException {
        DataDef dataDef = getTestDataDef();
        Axis axis = factory.createAxis(AxisName.ROW, "Xyz");

        try {
            axisDefsHelper.getIndexRange(dataDef, axis);
            fail("should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            assertThat(e.getMessage()).isEqualTo("indexRange not defined");
        }

        axis = factory.createAxis(AxisName.ROW, "Price");

        try {
            axisDefsHelper.getIndexRange(dataDef, axis);
            fail("should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            assertThat(e.getMessage()).isEqualTo("indexRange not defined");
        }
    }

    private DataDef getTestDataDef() throws IOException {
        Date runDate = new Date();
        Date fromDate = DateUtils.addDays(runDate, -1);
        Date toDate = DateUtils.addDays(runDate, 1);
        String defJson = new StringBuilder().append("{price:{axis:")
                .append("{fact:{query:{region:queryregion,field:queryfield},")
                .append("members:[{member:{name:fact,index:0,order:10}}]},")
                .append("col:{query:{script:colscript},")
                .append("members:[{member:{name: item, breakAfter: ['break1','break2'], ")
                .append("indexRange: 2-5, order:11}}]},")
                .append("row:{query:{noQuery: ignored},")
                .append("members:[{member:{name:Price,value:Price,index:2,order:12}},")
                .append("{member:{name:High,match:High}}").append("]} }}}")
                .toString();
        JsonNode def = mapper.readTree(TestUtils.parseJson(defJson));
        DataDef dataDef =
                factory.createDataDef("price", fromDate, toDate, defJson);
        dataDef.setDef(def);
        return dataDef;
    }
}
