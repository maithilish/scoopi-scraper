package org.codetab.scoopi.step.parse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.script.ScriptException;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.assertj.core.util.Lists;
import org.codetab.scoopi.defs.mig.yml.AxisDefs;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.ItemMig;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.TaskInfo;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ValueProcessorTest {

    @Mock
    private ScriptProcessor scriptProcessor;
    @Mock
    private QueryProcessor queryProcessor;
    @Mock
    private PrefixProcessor prefixProcessor;
    @Mock
    private QueryVarSubstitutor varSubstitutor;
    @Mock
    private AxisDefs axisDefs;
    @Mock
    private TaskInfo taskInfo;

    @InjectMocks
    private ValueProcessor valueProcessor;

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
    public void testAddScriptObject() throws IllegalAccessException {
        valueProcessor.addScriptObject("tkey", "tval");

        @SuppressWarnings("unchecked")
        Map<String, Object> actual = (Map<String, Object>) FieldUtils
                .readField(valueProcessor, "scriptObjectMap", true);

        assertThat(actual.size()).isEqualTo(1);
        assertThat(actual.get("tkey")).isEqualTo("tval");
    }

    @Test
    public void testSetAxisValuesScript()
            throws ScriptException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "def");

        Axis row = factory.createAxis(AxisName.ROW, "item", null, null, 2, 2);
        ItemMig itemMig = factory.createItemMig();
        itemMig.addAxis(row);

        IValueParser valueParser = null;

        Map<String, String> scripts = new HashMap<>();
        @SuppressWarnings("unchecked")
        Map<String, Object> scriptObjectMap = (Map<String, Object>) FieldUtils
                .readField(valueProcessor, "scriptObjectMap", true);

        String value = "test";

        given(scriptProcessor.getScripts(dataDef, row.getAxisName()))
                .willReturn(scripts);
        given(scriptProcessor.query(scripts)).willReturn(value);

        valueProcessor.setAxisValues(dataDef, itemMig, valueParser);

        assertThat(row.getValue()).isEqualTo(value);
        verify(scriptProcessor).init(scriptObjectMap);
        verify(varSubstitutor).replaceVariables(scripts, itemMig.getAxisMap());
    }

    @Test
    public void testSetAxisValuesQuery()
            throws ScriptException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "def");

        Axis row = factory.createAxis(AxisName.ROW, "item", null, null, 2, 2);
        ItemMig itemMig = factory.createItemMig();
        itemMig.addAxis(row);

        IValueParser valueParser = Mockito.mock(ValueParser.class);
        Map<String, String> queries = new HashMap<>();
        queries.put("region", "region query");
        queries.put("field", "field query");

        String value = "test";

        given(scriptProcessor.getScripts(dataDef, row.getAxisName()))
                .willThrow(NoSuchElementException.class);
        given(queryProcessor.getQueries(dataDef, row.getAxisName()))
                .willReturn(queries);
        given(queryProcessor.query(queries, valueParser)).willReturn(value);

        valueProcessor.setAxisValues(dataDef, itemMig, valueParser);

        assertThat(row.getValue()).isEqualTo(value);
        verify(varSubstitutor).replaceVariables(queries, itemMig.getAxisMap());
    }

    @Test
    public void testSetAxisValuesIndexNotSet() throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, ScriptException {
        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "def");

        Axis row = factory.createAxis(AxisName.ROW, "item");
        row.setValue("xyz"); // value is set to skip script and query block

        ItemMig itemMig = factory.createItemMig();
        itemMig.addAxis(row);

        IValueParser valueParser = null;

        // indexRange empty
        Optional<Range<Integer>> indexRange = Optional.empty();
        given(axisDefs.getIndexRange(dataDef, row)).willReturn(indexRange);
        row.setIndex(null);
        valueProcessor.setAxisValues(dataDef, itemMig, valueParser);
        assertThat(row.getIndex()).isEqualTo(1);

        // indexRange 22-24
        indexRange = Optional.of(Range.between(22, 24));
        given(axisDefs.getIndexRange(dataDef, row)).willReturn(indexRange);
        row.setIndex(null);
        valueProcessor.setAxisValues(dataDef, itemMig, valueParser);
        assertThat(row.getIndex()).isEqualTo(22);
    }

    @Test
    public void testSetAxisValuesPrefix()
            throws ScriptException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "def");

        Axis row = factory.createAxis(AxisName.ROW, "item", null, null, 2, 2);
        ItemMig itemMig = factory.createItemMig();
        itemMig.addAxis(row);

        IValueParser valueParser = Mockito.mock(ValueParser.class);
        Map<String, String> queries = new HashMap<>();
        queries.put("region", "region query");
        queries.put("field", "field query");

        String value = "test";
        String expectedValue = "p1test";

        Optional<List<String>> prefixes = Optional.of(Lists.newArrayList("p1"));

        given(scriptProcessor.getScripts(dataDef, row.getAxisName()))
                .willThrow(NoSuchElementException.class);
        given(queryProcessor.getQueries(dataDef, row.getAxisName()))
                .willReturn(queries);
        given(queryProcessor.query(queries, valueParser)).willReturn(value);
        given(prefixProcessor.getPrefixes(dataDef, AxisName.ROW))
                .willReturn(prefixes);
        given(prefixProcessor.prefixValue(value, prefixes.get()))
                .willReturn(expectedValue);

        valueProcessor.setAxisValues(dataDef, itemMig, valueParser);

        assertThat(row.getValue()).isEqualTo(expectedValue);
    }

    @Test
    public void testSetAxisValuesPrefixValueIsNull()
            throws ScriptException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "def");

        Axis row = factory.createAxis(AxisName.ROW, "item", null, null, 2, 2);
        ItemMig itemMig = factory.createItemMig();
        itemMig.addAxis(row);

        IValueParser valueParser = Mockito.mock(ValueParser.class);

        given(scriptProcessor.getScripts(dataDef, row.getAxisName()))
                .willThrow(NoSuchElementException.class);
        given(queryProcessor.getQueries(dataDef, row.getAxisName()))
                .willThrow(NoSuchElementException.class);

        valueProcessor.setAxisValues(dataDef, itemMig, valueParser);

        assertThat(row.getValue()).isNull();
        verifyZeroInteractions(varSubstitutor, prefixProcessor);
    }
}
