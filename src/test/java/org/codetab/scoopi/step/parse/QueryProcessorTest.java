package org.codetab.scoopi.step.parse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.script.ScriptException;

import org.codetab.scoopi.defs.mig.IAxisDefs;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.step.parse.cache.ParserCache;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class QueryProcessorTest {

    @Mock
    private IAxisDefs axisDefs;
    @Mock
    private ParserCache parserCache;

    @InjectMocks
    private QueryProcessor queryProcessor;

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
    public void testGetQueries() {
        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "def");
        AxisName axisName = AxisName.COL;
        String region = "region";
        String field = "field";
        String attribute = "attribute";

        given(axisDefs.getQuery(dataDef, axisName, "region"))
                .willReturn(region);
        given(axisDefs.getQuery(dataDef, axisName, "field")).willReturn(field);
        given(axisDefs.getQuery(dataDef, axisName, "attribute"))
                .willReturn(attribute);

        Map<String, String> actual =
                queryProcessor.getQueries(dataDef, axisName);

        assertThat(actual.size()).isEqualTo(3);
        assertThat(actual.get("region")).isEqualTo(region);
        assertThat(actual.get("field")).isEqualTo(field);
        assertThat(actual.get("attribute")).isEqualTo(attribute);
    }

    @Test
    public void testGetQueriesNoAttribute() {
        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "def");
        AxisName axisName = AxisName.COL;
        String region = "region";
        String field = "field";

        String attribute = "undefined";

        given(axisDefs.getQuery(dataDef, axisName, "region"))
                .willReturn(region);
        given(axisDefs.getQuery(dataDef, axisName, "field")).willReturn(field);
        given(axisDefs.getQuery(dataDef, axisName, "attribute"))
                .willReturn(attribute);

        Map<String, String> actual =
                queryProcessor.getQueries(dataDef, axisName);

        assertThat(actual.size()).isEqualTo(3);
        assertThat(actual.get("region")).isEqualTo(region);
        assertThat(actual.get("field")).isEqualTo(field);
        assertThat(actual.get("attribute")).isBlank();
    }

    @Test
    public void testGetQueriesShouldThrowException() {
        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "def");
        AxisName axisName = AxisName.COL;

        given(axisDefs.getQuery(dataDef, axisName, "region"))
                .willReturn("undefined", "region");
        given(axisDefs.getQuery(dataDef, axisName, "field")).willReturn("field",
                "undefined");

        try {
            queryProcessor.getQueries(dataDef, axisName);
            fail("should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            assertThat(e.getMessage()).isEqualTo("query not defined");
        }

        try {
            queryProcessor.getQueries(dataDef, axisName);
            fail("should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            assertThat(e.getMessage()).isEqualTo("query not defined");
        }
    }

    @Test
    public void testQuery() throws ScriptException {
        Map<String, String> queries = new HashMap<>();
        int key = 5;
        String value = "test";
        IValueParser valueParser = Mockito.mock(ValueParser.class);

        given(parserCache.getKey(queries)).willReturn(key);
        given(parserCache.get(key)).willReturn(null);
        given(valueParser.parseValue(queries)).willReturn(value);

        String actual = queryProcessor.query(queries, valueParser);

        assertThat(actual).isEqualTo(value);

        verify(parserCache).put(key, value);
    }

    @Test
    public void testQueryFromCache() throws ScriptException {
        Map<String, String> queries = new HashMap<>();
        int key = 5;
        String value = "test";
        IValueParser valueParser = Mockito.mock(ValueParser.class);

        given(parserCache.getKey(queries)).willReturn(key);
        given(parserCache.get(key)).willReturn(value);

        String actual = queryProcessor.query(queries, valueParser);

        assertThat(actual).isEqualTo(value);
    }
}
