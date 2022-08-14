package org.codetab.scoopi.step.parse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.InvalidDefException;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.TaskInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ValueProcessorTest {
    @InjectMocks
    private ValueProcessor valueProcessor;

    @Mock
    private ScriptProcessor scriptProcessor;
    @Mock
    private QueryProcessor queryProcessor;
    @Mock
    private PrefixProcessor prefixProcessor;
    @Mock
    private QueryVarSubstitutor varSubstitutor;
    @Mock
    private BreakAfter breakAfter;
    @Mock
    private TaskInfo taskInfo;
    @Mock
    private Configs configs;

    @Mock
    private Map<String, Object> scriptObjectMap;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSetAxisValuesIfPrefixesIsPresentIfAxisNameEqualsIfIsNull()
            throws Exception {
        String dataDef = "Foo";
        Item item = Mockito.mock(Item.class);
        Map<String, Integer> indexMap = new HashMap<>();
        Indexer indexer = Mockito.mock(Indexer.class);
        IValueParser valueParser = Mockito.mock(IValueParser.class);
        boolean replaceBlank = true;
        String blankReplaceWith = "Bar";
        String notFoundReplaceWith = "Baz";
        String apple = "Qux";

        Axis axis0 = Mockito.mock(Axis.class);
        List<Axis> list = new ArrayList<>();
        list.add(axis0);

        Axis axis = Mockito.mock(Axis.class);
        List<Axis> list2 = new ArrayList<>();
        list2.add(axis);

        String axisName = "fact"; // test fact axis
        String itemName = "Corge";
        String orange = null;
        String mango = "Garply";
        String apricot = "Waldo";
        String fig = null;

        List<Axis> list3 = new ArrayList<>();

        Map<String, String> varValues1 = new HashMap<>();

        Marker marker = Mockito.mock(Marker.class);
        String shade = "Xyzzy";

        Map<String, String> scripts = new HashMap<>();
        scripts.put("script", "s1");
        Map<String, String> queries = new HashMap<>();
        queries.put("q", "q1");

        List<Axis> list4 = new ArrayList<>();

        Map<String, String> varValues2 = new HashMap<>();

        Marker marker2 = Mockito.mock(Marker.class);
        String wolf = "Thud";

        String value = null;

        Optional<List<String>> breakAfters = Optional.empty();

        Optional<List<String>> prefixes = Optional.of(new ArrayList<>());

        Marker marker3 = Mockito.mock(Marker.class);

        Logger log = Mockito.mock(Logger.class);
        setFinalStaticField(ValueProcessor.class, "LOG", log);
        when(log.isTraceEnabled()).thenReturn(true);

        when(configs.getBoolean("scoopi.fact.blank.replace", true))
                .thenReturn(replaceBlank);
        when(configs.getConfig("scoopi.fact.blank.replaceWith", "-"))
                .thenReturn(blankReplaceWith);
        when(configs.getConfig("scoopi.fact.notFound.replaceWith", "not found"))
                .thenReturn(notFoundReplaceWith);

        when(item.getAxes()).thenReturn(list).thenReturn(list2)
                .thenReturn(list3).thenReturn(list4);

        when(axis0.getItemName()).thenReturn(apple);
        when(axis.getItemName()).thenReturn(itemName);
        when(axis.getAxisName()).thenReturn(axisName);
        when(axis.getValue()).thenReturn(orange).thenReturn(fig);
        when(axis.getMatch()).thenReturn(mango).thenReturn(apricot);
        when(scriptProcessor.getScripts(dataDef, itemName)).thenReturn(scripts);

        when(taskInfo.getMarker()).thenReturn(marker).thenReturn(marker3);
        when(taskInfo.getLabel()).thenReturn(shade);

        // to pass queryProcessor block
        when(scriptProcessor.query(scripts)).thenReturn(null);

        when(queryProcessor.getQueries(dataDef, axisName, itemName))
                .thenReturn(queries);

        when(varSubstitutor.getVarValueMap(scripts, list3, axis))
                .thenReturn(varValues1);
        when(varSubstitutor.getVarValueMap(queries, list4, axis))
                .thenReturn(varValues2);

        when(taskInfo.getMarker()).thenReturn(marker2);
        when(taskInfo.getLabel()).thenReturn(wolf);
        when(queryProcessor.query(queries, valueParser)).thenReturn(value);
        when(breakAfter.getBreakAfters(dataDef, itemName))
                .thenReturn(breakAfters);

        when(prefixProcessor.getPrefixes(dataDef, itemName))
                .thenReturn(prefixes);
        when(prefixProcessor.prefixValue(value, prefixes.get()))
                .thenReturn(value);

        valueProcessor.setAxisValues(dataDef, item, indexMap, indexer,
                valueParser);

        verify(axis0).setIndex(indexMap.get(apple));
        verify(axis).setValue(apricot.replaceAll("\\\\", ""));

        verify(varSubstitutor).replaceVariables(scripts, varValues1);
        verify(varSubstitutor).replaceVariables(queries, varValues2);

        verify(scriptProcessor).init(scriptObjectMap);

        verify(breakAfter, never()).check(breakAfters, value);
        verify(indexer, never()).markBreakAfter(itemName);

        // value is replaced as notFoundReplaceWith
        verify(axis).setValue(notFoundReplaceWith);
    }

    @Test
    public void testSetAxisValuesIfAxisNameEqualsElseIsNullIfValueTrimEqualsIgnoreCase()
            throws Exception {
        String dataDef = "Foo";
        Item item = Mockito.mock(Item.class);
        Map<String, Integer> indexMap = new HashMap<>();
        Indexer indexer = Mockito.mock(Indexer.class);
        IValueParser valueParser = Mockito.mock(IValueParser.class);
        boolean replaceBlank = true;
        String blankReplaceWith = "Bar";
        String notFoundReplaceWith = "Baz";

        String apple = "Qux";

        Axis axis0 = Mockito.mock(Axis.class);
        List<Axis> list = new ArrayList<>();
        list.add(axis0);

        Axis axis = Mockito.mock(Axis.class);
        List<Axis> list2 = new ArrayList<>();
        list2.add(axis);

        Map<String, String> scripts = new HashMap<>();
        scripts.put("script", "s1");
        Map<String, String> queries = new HashMap<>();
        queries.put("q", "q1");

        String orange = null;
        String fig = null;

        String axisName = "fact";
        String itemName = "Corge";
        String mango = "Garply";
        String apricot = "Waldo";

        List<Axis> list3 = new ArrayList<>();
        Map<String, String> varValues1 = new HashMap<>();
        Marker marker = Mockito.mock(Marker.class);
        String shade = "Xyzzy";

        List<Axis> list4 = new ArrayList<>();

        Map<String, String> varValues2 = new HashMap<>();
        Marker marker2 = Mockito.mock(Marker.class);
        String wolf = "Thud";

        String value = " null ";

        Optional<List<String>> breakAfters = Optional.empty();

        Optional<List<String>> prefixes = Optional.of(new ArrayList<>());

        Marker marker3 = Mockito.mock(Marker.class);

        Logger log = Mockito.mock(Logger.class);
        setFinalStaticField(ValueProcessor.class, "LOG", log);
        when(log.isTraceEnabled()).thenReturn(false);

        when(configs.getBoolean("scoopi.fact.blank.replace", true))
                .thenReturn(replaceBlank);
        when(configs.getConfig("scoopi.fact.blank.replaceWith", "-"))
                .thenReturn(blankReplaceWith);
        when(configs.getConfig("scoopi.fact.notFound.replaceWith", "not found"))
                .thenReturn(notFoundReplaceWith);
        when(item.getAxes()).thenReturn(list).thenReturn(list2)
                .thenReturn(list3).thenReturn(list4);

        when(axis0.getItemName()).thenReturn(apple);
        when(axis.getItemName()).thenReturn(itemName);

        when(axis.getAxisName()).thenReturn(axisName);
        when(axis.getValue()).thenReturn(orange).thenReturn(fig);
        when(axis.getMatch()).thenReturn(mango).thenReturn(apricot);
        when(scriptProcessor.getScripts(dataDef, itemName)).thenReturn(scripts);
        when(varSubstitutor.getVarValueMap(scripts, list3, axis))
                .thenReturn(varValues1);

        when(taskInfo.getMarker()).thenReturn(marker).thenReturn(marker3);
        when(taskInfo.getLabel()).thenReturn(shade);

        // to pass queryProcessor block
        when(scriptProcessor.query(scripts)).thenReturn(null);

        when(queryProcessor.getQueries(dataDef, axisName, itemName))
                .thenReturn(queries);
        when(varSubstitutor.getVarValueMap(queries, list4, axis))
                .thenReturn(varValues2);
        when(taskInfo.getMarker()).thenReturn(marker2);
        when(taskInfo.getLabel()).thenReturn(wolf);
        when(queryProcessor.query(queries, valueParser)).thenReturn(value);
        when(breakAfter.getBreakAfters(dataDef, itemName))
                .thenReturn(breakAfters);
        when(prefixProcessor.getPrefixes(dataDef, itemName))
                .thenReturn(prefixes);
        when(prefixProcessor.prefixValue(value, prefixes.get()))
                .thenReturn(value);

        valueProcessor.setAxisValues(dataDef, item, indexMap, indexer,
                valueParser);

        verify(axis0).setIndex(indexMap.get(apple));
        verify(axis).setValue(apricot.replaceAll("\\\\", ""));
        verify(varSubstitutor).replaceVariables(scripts, varValues1);
        verify(scriptProcessor).init(scriptObjectMap);
        verify(varSubstitutor).replaceVariables(queries, varValues2);
        verify(breakAfter, never()).check(breakAfters, value);
        verify(indexer, never()).markBreakAfter(itemName);

        // value modified to notFoundReplaceWith
        verify(axis).setValue(notFoundReplaceWith);
    }

    @Test
    public void testSetAxisValuesElseValueTrimEqualsIgnoreCaseIfelseIfReplaceBlank()
            throws Exception {
        String dataDef = "Foo";
        Item item = Mockito.mock(Item.class);
        Map<String, Integer> indexMap = new HashMap<>();
        Indexer indexer = Mockito.mock(Indexer.class);
        IValueParser valueParser = Mockito.mock(IValueParser.class);
        boolean replaceBlank = true;
        String blankReplaceWith = "Bar";
        String notFoundReplaceWith = "Baz";

        String apple = "Qux";

        Axis axis0 = Mockito.mock(Axis.class);
        List<Axis> list = new ArrayList<>();
        list.add(axis0);

        Axis axis = Mockito.mock(Axis.class);
        List<Axis> list2 = new ArrayList<>();
        list2.add(axis);

        Map<String, String> scripts = new HashMap<>();
        scripts.put("s", "s1");
        Map<String, String> queries = new HashMap<>();
        queries.put("q", "q1");

        String orange = null;
        String fig = null;

        String axisName = "fact";
        String itemName = "Corge";

        String mango = "Garply";
        String apricot = "Waldo";

        List<Axis> list3 = new ArrayList<>();
        Map<String, String> varValues1 = new HashMap<>();
        Marker marker = Mockito.mock(Marker.class);
        String shade = "Xyzzy";

        List<Axis> list4 = new ArrayList<>();
        Map<String, String> varValues2 = new HashMap<>();
        Marker marker2 = Mockito.mock(Marker.class);
        String wolf = "Thud";

        // value is blank
        String value = "   ";

        Optional<List<String>> breakAfters = Optional.empty();
        Optional<List<String>> prefixes = Optional.of(new ArrayList<>());

        Marker marker3 = Mockito.mock(Marker.class);

        when(configs.getBoolean("scoopi.fact.blank.replace", true))
                .thenReturn(replaceBlank);
        when(configs.getConfig("scoopi.fact.blank.replaceWith", "-"))
                .thenReturn(blankReplaceWith);
        when(configs.getConfig("scoopi.fact.notFound.replaceWith", "not found"))
                .thenReturn(notFoundReplaceWith);
        when(item.getAxes()).thenReturn(list).thenReturn(list2)
                .thenReturn(list3).thenReturn(list4);

        when(axis0.getItemName()).thenReturn(apple);
        when(axis.getItemName()).thenReturn(itemName);

        when(axis.getAxisName()).thenReturn(axisName);
        when(axis.getValue()).thenReturn(orange).thenReturn(fig);
        when(axis.getMatch()).thenReturn(mango).thenReturn(apricot);
        when(scriptProcessor.getScripts(dataDef, itemName)).thenReturn(scripts);
        when(varSubstitutor.getVarValueMap(scripts, list3, axis))
                .thenReturn(varValues1);
        when(taskInfo.getMarker()).thenReturn(marker).thenReturn(marker3);
        when(taskInfo.getLabel()).thenReturn(shade);

        // to pass queryProcessor block
        when(scriptProcessor.query(scripts)).thenReturn(null);

        when(queryProcessor.getQueries(dataDef, axisName, itemName))
                .thenReturn(queries);
        when(varSubstitutor.getVarValueMap(queries, list4, axis))
                .thenReturn(varValues2);
        when(taskInfo.getMarker()).thenReturn(marker2);
        when(taskInfo.getLabel()).thenReturn(wolf);
        when(queryProcessor.query(queries, valueParser)).thenReturn(value);
        when(breakAfter.getBreakAfters(dataDef, itemName))
                .thenReturn(breakAfters);
        when(prefixProcessor.getPrefixes(dataDef, itemName))
                .thenReturn(prefixes);
        when(prefixProcessor.prefixValue(value, prefixes.get()))
                .thenReturn(value);
        valueProcessor.setAxisValues(dataDef, item, indexMap, indexer,
                valueParser);

        verify(axis0).setIndex(indexMap.get(apple));
        verify(axis).setValue(apricot.replaceAll("\\\\", ""));
        verify(varSubstitutor).replaceVariables(scripts, varValues1);
        verify(scriptProcessor).init(scriptObjectMap);
        verify(varSubstitutor).replaceVariables(queries, varValues2);
        verify(breakAfter, never()).check(breakAfters, value);
        verify(indexer, never()).markBreakAfter(itemName);

        // replace with blankReplaceWith
        verify(axis).setValue(blankReplaceWith);
    }

    @Test
    public void testSetAxisValuesElseValueTrimEqualsIgnoreCaseIfelseElseReplaceBlank()
            throws Exception {
        String dataDef = "Foo";
        Item item = Mockito.mock(Item.class);
        Map<String, Integer> indexMap = new HashMap<>();
        Indexer indexer = Mockito.mock(Indexer.class);
        IValueParser valueParser = Mockito.mock(IValueParser.class);

        // replaceBlank else test
        boolean replaceBlank = false;

        String blankReplaceWith = "Bar";
        String notFoundReplaceWith = "Baz";

        String apple = "Qux";

        Axis axis0 = Mockito.mock(Axis.class);
        List<Axis> list = new ArrayList<>();
        list.add(axis0);

        Axis axis = Mockito.mock(Axis.class);
        List<Axis> list2 = new ArrayList<>();
        list2.add(axis);

        Map<String, String> scripts = new HashMap<>();
        scripts.put("s", "s1");
        Map<String, String> queries = new HashMap<>();
        queries.put("q", "q1");

        String orange = null;
        String fig = null;

        String axisName = "fact"; // fact axis test
        String itemName = "Corge";
        String mango = "Garply";
        String apricot = "Waldo";

        List<Axis> list3 = new ArrayList<>();
        Map<String, String> varValues1 = new HashMap<>();
        Marker marker = Mockito.mock(Marker.class);
        String shade = "Xyzzy";

        List<Axis> list4 = new ArrayList<>();
        Map<String, String> varValues2 = new HashMap<>();
        Marker marker2 = Mockito.mock(Marker.class);
        String wolf = "Thud";

        // value is blank but no replace
        String value = "    ";

        Optional<List<String>> breakAfters = Optional.empty();
        Optional<List<String>> prefixes = Optional.of(new ArrayList<>());

        Marker marker3 = Mockito.mock(Marker.class);

        when(configs.getBoolean("scoopi.fact.blank.replace", true))
                .thenReturn(replaceBlank);
        when(configs.getConfig("scoopi.fact.blank.replaceWith", "-"))
                .thenReturn(blankReplaceWith);
        when(configs.getConfig("scoopi.fact.notFound.replaceWith", "not found"))
                .thenReturn(notFoundReplaceWith);
        when(item.getAxes()).thenReturn(list).thenReturn(list2)
                .thenReturn(list3).thenReturn(list4);

        when(axis0.getItemName()).thenReturn(apple);
        when(axis.getItemName()).thenReturn(itemName);

        when(axis.getAxisName()).thenReturn(axisName);
        when(axis.getValue()).thenReturn(orange).thenReturn(fig);
        when(axis.getMatch()).thenReturn(mango).thenReturn(apricot);
        when(scriptProcessor.getScripts(dataDef, itemName)).thenReturn(scripts);
        when(varSubstitutor.getVarValueMap(scripts, list3, axis))
                .thenReturn(varValues1);
        when(taskInfo.getMarker()).thenReturn(marker).thenReturn(marker3);
        when(taskInfo.getLabel()).thenReturn(shade);

        // to pass queryProcessor block
        when(scriptProcessor.query(scripts)).thenReturn(null);

        when(queryProcessor.getQueries(dataDef, axisName, itemName))
                .thenReturn(queries);
        when(varSubstitutor.getVarValueMap(queries, list4, axis))
                .thenReturn(varValues2);
        when(taskInfo.getMarker()).thenReturn(marker2);
        when(taskInfo.getLabel()).thenReturn(wolf);
        when(queryProcessor.query(queries, valueParser)).thenReturn(value);
        when(breakAfter.getBreakAfters(dataDef, itemName))
                .thenReturn(breakAfters);
        when(prefixProcessor.getPrefixes(dataDef, itemName))
                .thenReturn(prefixes);
        when(prefixProcessor.prefixValue(value, prefixes.get()))
                .thenReturn(value);
        valueProcessor.setAxisValues(dataDef, item, indexMap, indexer,
                valueParser);

        verify(axis0).setIndex(indexMap.get(apple));
        verify(axis).setValue(apricot.replaceAll("\\\\", ""));
        verify(varSubstitutor).replaceVariables(scripts, varValues1);
        verify(scriptProcessor).init(scriptObjectMap);
        verify(varSubstitutor).replaceVariables(queries, varValues2);
        verify(breakAfter, never()).check(breakAfters, value);
        verify(indexer, never()).markBreakAfter(itemName);

        // verify unmodified blank value
        verify(axis).setValue(value);
    }

    @Test
    public void testSetAxisValuesElseIsNullElseValueTrimEqualsIgnoreCaseElseelse()
            throws Exception {
        String dataDef = "Foo";
        Item item = Mockito.mock(Item.class);
        Map<String, Integer> indexMap = new HashMap<>();
        Indexer indexer = Mockito.mock(Indexer.class);
        IValueParser valueParser = Mockito.mock(IValueParser.class);
        boolean replaceBlank = true;
        String blankReplaceWith = "Bar";
        String notFoundReplaceWith = "Baz";

        String apple = "Qux";

        Axis axis0 = Mockito.mock(Axis.class);
        List<Axis> list = new ArrayList<>();
        list.add(axis0);

        Axis axis = Mockito.mock(Axis.class);
        List<Axis> list2 = new ArrayList<>();
        list2.add(axis);

        Map<String, String> scripts = new HashMap<>();
        scripts.put("s", "s1");
        Map<String, String> queries = new HashMap<>();
        queries.put("q", "q1");

        String orange = null;
        String fig = null;

        String axisName = "fact";
        String itemName = "Corge";
        String mango = "Garply";
        String apricot = "Waldo";

        List<Axis> list3 = new ArrayList<>();
        Map<String, String> varValues1 = new HashMap<>();
        Marker marker = Mockito.mock(Marker.class);
        String shade = "Xyzzy";

        List<Axis> list4 = new ArrayList<>();

        Map<String, String> varValues2 = new HashMap<>();
        Marker marker2 = Mockito.mock(Marker.class);
        String wolf = "Thud";

        String value = " not null ";

        Optional<List<String>> breakAfters = Optional.empty();

        Optional<List<String>> prefixes = Optional.of(new ArrayList<>());

        Marker marker3 = Mockito.mock(Marker.class);

        when(configs.getBoolean("scoopi.fact.blank.replace", true))
                .thenReturn(replaceBlank);
        when(configs.getConfig("scoopi.fact.blank.replaceWith", "-"))
                .thenReturn(blankReplaceWith);
        when(configs.getConfig("scoopi.fact.notFound.replaceWith", "not found"))
                .thenReturn(notFoundReplaceWith);
        when(item.getAxes()).thenReturn(list).thenReturn(list2)
                .thenReturn(list3).thenReturn(list4);

        when(axis0.getItemName()).thenReturn(apple);
        when(axis.getItemName()).thenReturn(itemName);

        when(axis.getAxisName()).thenReturn(axisName);
        when(axis.getValue()).thenReturn(orange).thenReturn(fig);
        when(axis.getMatch()).thenReturn(mango).thenReturn(apricot);
        when(scriptProcessor.getScripts(dataDef, itemName)).thenReturn(scripts);
        when(varSubstitutor.getVarValueMap(scripts, list3, axis))
                .thenReturn(varValues1);

        when(taskInfo.getMarker()).thenReturn(marker).thenReturn(marker3);
        when(taskInfo.getLabel()).thenReturn(shade);

        // to pass queryProcessor block
        when(scriptProcessor.query(scripts)).thenReturn(null);

        when(queryProcessor.getQueries(dataDef, axisName, itemName))
                .thenReturn(queries);
        when(varSubstitutor.getVarValueMap(queries, list4, axis))
                .thenReturn(varValues2);
        when(taskInfo.getMarker()).thenReturn(marker2);
        when(taskInfo.getLabel()).thenReturn(wolf);
        when(queryProcessor.query(queries, valueParser)).thenReturn(value);
        when(breakAfter.getBreakAfters(dataDef, itemName))
                .thenReturn(breakAfters);
        when(prefixProcessor.getPrefixes(dataDef, itemName))
                .thenReturn(prefixes);
        when(prefixProcessor.prefixValue(value, prefixes.get()))
                .thenReturn(value);

        valueProcessor.setAxisValues(dataDef, item, indexMap, indexer,
                valueParser);

        verify(axis0).setIndex(indexMap.get(apple));
        verify(axis).setValue(apricot.replaceAll("\\\\", ""));
        verify(varSubstitutor).replaceVariables(scripts, varValues1);
        verify(scriptProcessor).init(scriptObjectMap);
        verify(varSubstitutor).replaceVariables(queries, varValues2);
        verify(breakAfter, never()).check(breakAfters, value);
        verify(indexer, never()).markBreakAfter(itemName);

        // value not modified or trimmed
        verify(axis).setValue(" not null ");
    }

    @Test
    public void testSetAxisValuesIfNonNullIfPrefixesIsPresentElseAxisNameEquals()
            throws Exception {
        String dataDef = "Foo";
        Item item = Mockito.mock(Item.class);
        Map<String, Integer> indexMap = new HashMap<>();
        Indexer indexer = Mockito.mock(Indexer.class);
        IValueParser valueParser = Mockito.mock(IValueParser.class);
        boolean replaceBlank = true;
        String blankReplaceWith = "Bar";
        String notFoundReplaceWith = "Baz";

        String apple = "Qux";

        Axis axis0 = Mockito.mock(Axis.class);
        List<Axis> list = new ArrayList<>();
        list.add(axis0);

        Axis axis = Mockito.mock(Axis.class);
        List<Axis> list2 = new ArrayList<>();
        list2.add(axis);

        Map<String, String> scripts = new HashMap<>();
        scripts.put("s", "s1");
        Map<String, String> queries = new HashMap<>();
        queries.put("q", "q1");

        String orange = null;
        String fig = null;

        String axisName = "Quux";
        String itemName = "Corge";

        String mango = "Garply";
        String apricot = "Waldo";

        List<Axis> list3 = new ArrayList<>();
        Map<String, String> varValues1 = new HashMap<>();
        Marker marker = Mockito.mock(Marker.class);
        String shade = "Xyzzy";

        List<Axis> list4 = new ArrayList<>();
        Map<String, String> varValues2 = new HashMap<>();
        Marker marker2 = Mockito.mock(Marker.class);
        String wolf = "Thud";
        String value = "Zoopy";

        Optional<List<String>> breakAfters = Optional.empty();
        Optional<List<String>> prefixes = Optional.of(new ArrayList<>());

        Marker marker3 = Mockito.mock(Marker.class);

        when(configs.getBoolean("scoopi.fact.blank.replace", true))
                .thenReturn(replaceBlank);
        when(configs.getConfig("scoopi.fact.blank.replaceWith", "-"))
                .thenReturn(blankReplaceWith);
        when(configs.getConfig("scoopi.fact.notFound.replaceWith", "not found"))
                .thenReturn(notFoundReplaceWith);
        when(item.getAxes()).thenReturn(list).thenReturn(list2)
                .thenReturn(list3).thenReturn(list4);

        when(axis0.getItemName()).thenReturn(apple);
        when(axis.getItemName()).thenReturn(itemName);

        when(axis.getAxisName()).thenReturn(axisName);
        when(axis.getValue()).thenReturn(orange).thenReturn(fig);
        when(axis.getMatch()).thenReturn(mango).thenReturn(apricot);
        when(scriptProcessor.getScripts(dataDef, itemName)).thenReturn(scripts);
        when(varSubstitutor.getVarValueMap(scripts, list3, axis))
                .thenReturn(varValues1);
        when(taskInfo.getMarker()).thenReturn(marker).thenReturn(marker3);
        when(taskInfo.getLabel()).thenReturn(shade);

        // to pass queryProcessor block
        when(scriptProcessor.query(scripts)).thenReturn(null);

        when(queryProcessor.getQueries(dataDef, axisName, itemName))
                .thenReturn(queries);
        when(varSubstitutor.getVarValueMap(queries, list4, axis))
                .thenReturn(varValues2);
        when(taskInfo.getMarker()).thenReturn(marker2);
        when(taskInfo.getLabel()).thenReturn(wolf);
        when(queryProcessor.query(queries, valueParser)).thenReturn(value);
        when(breakAfter.getBreakAfters(dataDef, itemName))
                .thenReturn(breakAfters);
        when(prefixProcessor.getPrefixes(dataDef, itemName))
                .thenReturn(prefixes);
        when(prefixProcessor.prefixValue(value, prefixes.get()))
                .thenReturn(value);
        valueProcessor.setAxisValues(dataDef, item, indexMap, indexer,
                valueParser);

        verify(axis0).setIndex(indexMap.get(apple));
        verify(axis).setValue(apricot.replaceAll("\\\\", ""));
        verify(varSubstitutor).replaceVariables(scripts, varValues1);
        verify(scriptProcessor).init(scriptObjectMap);
        verify(varSubstitutor).replaceVariables(queries, varValues2);
        verify(breakAfter, never()).check(breakAfters, value);
        verify(indexer, never()).markBreakAfter(itemName);
        verify(axis).setValue(value);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetAxisValuesIfIsNullIfNonNullElsePrefixesIsPresent()
            throws Exception {
        String dataDef = "Foo";
        Item item = Mockito.mock(Item.class);
        Map<String, Integer> indexMap = new HashMap<>();
        Indexer indexer = Mockito.mock(Indexer.class);
        IValueParser valueParser = Mockito.mock(IValueParser.class);
        boolean replaceBlank = true;
        String blankReplaceWith = "Bar";
        String notFoundReplaceWith = "Baz";

        String apple = "Qux";

        Axis axis0 = Mockito.mock(Axis.class);
        List<Axis> list = new ArrayList<>();
        list.add(axis0);

        Axis axis = Mockito.mock(Axis.class);
        List<Axis> list2 = new ArrayList<>();
        list2.add(axis);

        Map<String, String> scripts = new HashMap<>();
        scripts.put("s", "s1");
        Map<String, String> queries = new HashMap<>();
        queries.put("q", "q1");

        String orange = null;
        String fig = null;

        String axisName = "Quux";
        String itemName = "Corge";

        String mango = "Garply";
        String apricot = "Waldo";

        List<Axis> list3 = new ArrayList<>();
        Map<String, String> varValues1 = new HashMap<>();
        Marker marker = Mockito.mock(Marker.class);
        String shade = "Xyzzy";

        List<Axis> list4 = new ArrayList<>();
        Map<String, String> varValues2 = new HashMap<>();
        Marker marker2 = Mockito.mock(Marker.class);
        String wolf = "Thud";
        String value = "Zoopy";
        Optional<List<String>> breakAfters = Optional.empty();
        Optional<List<String>> prefixes = Optional.empty();

        when(configs.getBoolean("scoopi.fact.blank.replace", true))
                .thenReturn(replaceBlank);
        when(configs.getConfig("scoopi.fact.blank.replaceWith", "-"))
                .thenReturn(blankReplaceWith);
        when(configs.getConfig("scoopi.fact.notFound.replaceWith", "not found"))
                .thenReturn(notFoundReplaceWith);
        when(item.getAxes()).thenReturn(list).thenReturn(list2)
                .thenReturn(list3).thenReturn(list4);

        when(axis0.getItemName()).thenReturn(apple);
        when(axis.getItemName()).thenReturn(itemName);

        when(axis.getAxisName()).thenReturn(axisName);
        when(axis.getValue()).thenReturn(orange).thenReturn(fig);
        when(axis.getMatch()).thenReturn(mango).thenReturn(apricot);
        when(scriptProcessor.getScripts(dataDef, itemName)).thenReturn(scripts);
        when(varSubstitutor.getVarValueMap(scripts, list3, axis))
                .thenReturn(varValues1);
        when(taskInfo.getMarker()).thenReturn(marker);
        when(taskInfo.getLabel()).thenReturn(shade);

        // to pass queryProcessor block
        when(scriptProcessor.query(scripts)).thenReturn(null);

        when(queryProcessor.getQueries(dataDef, axisName, itemName))
                .thenReturn(queries);
        when(varSubstitutor.getVarValueMap(queries, list4, axis))
                .thenReturn(varValues2);
        when(taskInfo.getMarker()).thenReturn(marker2);
        when(taskInfo.getLabel()).thenReturn(wolf);
        when(queryProcessor.query(queries, valueParser)).thenReturn(value);
        when(breakAfter.getBreakAfters(dataDef, itemName))
                .thenReturn(breakAfters);
        when(prefixProcessor.getPrefixes(dataDef, itemName))
                .thenReturn(prefixes);

        valueProcessor.setAxisValues(dataDef, item, indexMap, indexer,
                valueParser);

        verify(axis0).setIndex(indexMap.get(apple));
        verify(axis).setValue(apricot.replaceAll("\\\\", ""));
        verify(varSubstitutor).replaceVariables(scripts, varValues1);
        verify(scriptProcessor).init(scriptObjectMap);
        verify(varSubstitutor).replaceVariables(queries, varValues2);
        verify(breakAfter, never()).check(breakAfters, value);
        verify(indexer, never()).markBreakAfter(itemName);
        verify(prefixProcessor, never()).prefixValue(eq(value),
                any(ArrayList.class));

        verify(taskInfo, times(2)).getMarker();
        verify(axis).setValue(value);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetAxisValuesIfBreakAftersIsPresentIfIsNullElseNonNull()
            throws Exception {
        String dataDef = "Foo";
        Item item = Mockito.mock(Item.class);
        Map<String, Integer> indexMap = new HashMap<>();
        Indexer indexer = Mockito.mock(Indexer.class);
        IValueParser valueParser = Mockito.mock(IValueParser.class);
        boolean replaceBlank = true;
        String blankReplaceWith = "Bar";
        String notFoundReplaceWith = "Baz";

        Axis axis0 = Mockito.mock(Axis.class);
        List<Axis> list = new ArrayList<>();
        list.add(axis0);

        Axis axis = Mockito.mock(Axis.class);
        List<Axis> list2 = new ArrayList<>();
        list2.add(axis);

        Map<String, String> scripts = new HashMap<>();
        scripts.put("s", "s1");
        Map<String, String> queries = new HashMap<>();
        queries.put("q", "q1");

        String orange = null;
        String fig = null;

        String apple = "Qux";

        String axisName = "Quux";
        String itemName = "Corge";

        String mango = "Garply";
        String apricot = "Waldo";

        List<Axis> list3 = new ArrayList<>();
        Map<String, String> varValues1 = new HashMap<>();
        Marker marker = Mockito.mock(Marker.class);
        String shade = "Xyzzy";

        List<Axis> list4 = new ArrayList<>();
        Map<String, String> varValues2 = new HashMap<>();
        Marker marker2 = Mockito.mock(Marker.class);
        String wolf = "Thud";

        String value = null; // isNull(value)

        Optional<List<String>> breakAfters = Optional.of(new ArrayList<>());

        when(configs.getBoolean("scoopi.fact.blank.replace", true))
                .thenReturn(replaceBlank);
        when(configs.getConfig("scoopi.fact.blank.replaceWith", "-"))
                .thenReturn(blankReplaceWith);
        when(configs.getConfig("scoopi.fact.notFound.replaceWith", "not found"))
                .thenReturn(notFoundReplaceWith);
        when(item.getAxes()).thenReturn(list).thenReturn(list2)
                .thenReturn(list3).thenReturn(list4);

        when(axis0.getItemName()).thenReturn(apple);
        when(axis.getItemName()).thenReturn(itemName);

        when(axis.getAxisName()).thenReturn(axisName);
        when(axis.getValue()).thenReturn(orange).thenReturn(fig);
        when(axis.getMatch()).thenReturn(mango).thenReturn(apricot);
        when(scriptProcessor.getScripts(dataDef, itemName)).thenReturn(scripts);
        when(varSubstitutor.getVarValueMap(scripts, list3, axis))
                .thenReturn(varValues1);
        when(taskInfo.getMarker()).thenReturn(marker);
        when(taskInfo.getLabel()).thenReturn(shade);

        // to pass queryProcessor block
        when(scriptProcessor.query(scripts)).thenReturn(null);

        when(queryProcessor.getQueries(dataDef, axisName, itemName))
                .thenReturn(queries);
        when(varSubstitutor.getVarValueMap(queries, list4, axis))
                .thenReturn(varValues2);
        when(taskInfo.getMarker()).thenReturn(marker2);
        when(taskInfo.getLabel()).thenReturn(wolf);
        when(queryProcessor.query(queries, valueParser)).thenReturn(value);
        when(breakAfter.getBreakAfters(dataDef, itemName))
                .thenReturn(breakAfters);

        assertThrows(InvalidDefException.class, () -> valueProcessor
                .setAxisValues(dataDef, item, indexMap, indexer, valueParser));

        verify(axis0).setIndex(indexMap.get(apple));
        verify(axis).setValue(apricot.replaceAll("\\\\", ""));
        verify(varSubstitutor).replaceVariables(scripts, varValues1);
        verify(scriptProcessor).init(scriptObjectMap);
        verify(varSubstitutor).replaceVariables(queries, varValues2);
        verify(breakAfter, never()).check(breakAfters, value);
        verify(indexer, never()).markBreakAfter(itemName);
        verify(prefixProcessor, never()).getPrefixes(dataDef, itemName);

        verify(prefixProcessor, never()).prefixValue(eq(value),
                any(ArrayList.class));

        verify(taskInfo, times(2)).getMarker();
        verify(axis, never()).setValue(value);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetAxisValuesIfBreakAftersIsPresentElseIsNullIfBreakAfterCheck()
            throws Exception {
        String dataDef = "Foo";
        Item item = Mockito.mock(Item.class);
        Map<String, Integer> indexMap = new HashMap<>();
        Indexer indexer = Mockito.mock(Indexer.class);
        IValueParser valueParser = Mockito.mock(IValueParser.class);
        boolean replaceBlank = true;
        String blankReplaceWith = "Bar";
        String notFoundReplaceWith = "Baz";

        Axis axis0 = Mockito.mock(Axis.class);
        List<Axis> list = new ArrayList<>();
        list.add(axis0);

        Axis axis = Mockito.mock(Axis.class);
        List<Axis> list2 = new ArrayList<>();
        list2.add(axis);

        Map<String, String> scripts = new HashMap<>();
        scripts.put("s", "s1");
        Map<String, String> queries = new HashMap<>();
        queries.put("q", "q1");

        String orange = null;
        String fig = null;

        String apple = "Qux";

        String axisName = "Quux";
        String itemName = "Corge";

        String mango = "Garply";
        String apricot = "Waldo";

        List<Axis> list3 = new ArrayList<>();
        Map<String, String> varValues1 = new HashMap<>();
        Marker marker = Mockito.mock(Marker.class);
        String shade = "Xyzzy";
        List<Axis> list4 = new ArrayList<>();
        Map<String, String> varValues2 = new HashMap<>();
        Marker marker2 = Mockito.mock(Marker.class);
        String wolf = "Thud";
        String value = "Zoopy";
        Optional<List<String>> breakAfters = Optional.of(new ArrayList<>());
        boolean avalanche = true;
        Optional<List<String>> prefixes = Optional.empty();

        Marker marker3 = Mockito.mock(Marker.class);

        when(configs.getBoolean("scoopi.fact.blank.replace", true))
                .thenReturn(replaceBlank);
        when(configs.getConfig("scoopi.fact.blank.replaceWith", "-"))
                .thenReturn(blankReplaceWith);
        when(configs.getConfig("scoopi.fact.notFound.replaceWith", "not found"))
                .thenReturn(notFoundReplaceWith);
        when(item.getAxes()).thenReturn(list).thenReturn(list2)
                .thenReturn(list3).thenReturn(list4);

        when(axis0.getItemName()).thenReturn(apple);
        when(axis.getItemName()).thenReturn(itemName);

        when(axis.getAxisName()).thenReturn(axisName);
        when(axis.getValue()).thenReturn(orange).thenReturn(fig);
        when(axis.getMatch()).thenReturn(mango).thenReturn(apricot);
        when(scriptProcessor.getScripts(dataDef, itemName)).thenReturn(scripts);
        when(varSubstitutor.getVarValueMap(scripts, list3, axis))
                .thenReturn(varValues1);
        when(taskInfo.getMarker()).thenReturn(marker).thenReturn(marker3);
        when(taskInfo.getLabel()).thenReturn(shade);
        // to pass queryProcessor block
        when(scriptProcessor.query(scripts)).thenReturn(null);
        when(queryProcessor.getQueries(dataDef, axisName, itemName))
                .thenReturn(queries);
        when(varSubstitutor.getVarValueMap(queries, list4, axis))
                .thenReturn(varValues2);
        when(taskInfo.getMarker()).thenReturn(marker2);
        when(taskInfo.getLabel()).thenReturn(wolf);
        when(queryProcessor.query(queries, valueParser)).thenReturn(value);
        when(breakAfter.getBreakAfters(dataDef, itemName))
                .thenReturn(breakAfters);
        when(breakAfter.check(breakAfters, value)).thenReturn(avalanche);
        when(prefixProcessor.getPrefixes(dataDef, itemName))
                .thenReturn(prefixes);

        valueProcessor.setAxisValues(dataDef, item, indexMap, indexer,
                valueParser);

        verify(axis0).setIndex(indexMap.get(apple));
        verify(axis).setValue(apricot.replaceAll("\\\\", ""));
        verify(varSubstitutor).replaceVariables(scripts, varValues1);
        verify(scriptProcessor).init(scriptObjectMap);
        verify(varSubstitutor).replaceVariables(queries, varValues2);
        verify(indexer).markBreakAfter(itemName);
        verify(prefixProcessor, never()).prefixValue(eq(value),
                any(ArrayList.class));
        verify(axis).setValue(value);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetAxisValuesIfBreakAftersIsPresentElseIsNullElseBreakAfterCheck()
            throws Exception {
        String dataDef = "Foo";
        Item item = Mockito.mock(Item.class);
        Map<String, Integer> indexMap = new HashMap<>();
        Indexer indexer = Mockito.mock(Indexer.class);
        IValueParser valueParser = Mockito.mock(IValueParser.class);
        boolean replaceBlank = true;
        String blankReplaceWith = "Bar";
        String notFoundReplaceWith = "Baz";

        String apple = "Qux";

        Axis axis0 = Mockito.mock(Axis.class);
        List<Axis> list = new ArrayList<>();
        list.add(axis0);

        Axis axis = Mockito.mock(Axis.class);
        List<Axis> list2 = new ArrayList<>();
        list2.add(axis);

        Map<String, String> scripts = new HashMap<>();
        scripts.put("s", "s1");
        Map<String, String> queries = new HashMap<>();
        queries.put("q", "q1");

        String orange = null;
        String fig = null;

        String axisName = "Quux";
        String itemName = "Corge";

        String mango = "Garply";
        String apricot = "Waldo";

        List<Axis> list3 = new ArrayList<>();
        Map<String, String> varValues1 = new HashMap<>();
        Marker marker = Mockito.mock(Marker.class);
        String shade = "Xyzzy";

        List<Axis> list4 = new ArrayList<>();
        Map<String, String> varValues2 = new HashMap<>();
        Marker marker2 = Mockito.mock(Marker.class);
        String wolf = "Thud";
        String value = "Zoopy";
        Optional<List<String>> breakAfters = Optional.of(new ArrayList<>());
        boolean avalanche = false; // breakAfter.check() false
        Optional<List<String>> prefixes = Optional.empty();

        Marker marker3 = Mockito.mock(Marker.class);

        when(configs.getBoolean("scoopi.fact.blank.replace", true))
                .thenReturn(replaceBlank);
        when(configs.getConfig("scoopi.fact.blank.replaceWith", "-"))
                .thenReturn(blankReplaceWith);
        when(configs.getConfig("scoopi.fact.notFound.replaceWith", "not found"))
                .thenReturn(notFoundReplaceWith);
        when(item.getAxes()).thenReturn(list).thenReturn(list2)
                .thenReturn(list3).thenReturn(list4);
        when(axis0.getItemName()).thenReturn(apple);
        when(axis.getItemName()).thenReturn(itemName);
        when(axis.getAxisName()).thenReturn(axisName);
        when(axis.getValue()).thenReturn(orange).thenReturn(fig);
        when(axis.getMatch()).thenReturn(mango).thenReturn(apricot);
        when(scriptProcessor.getScripts(dataDef, itemName)).thenReturn(scripts);
        when(varSubstitutor.getVarValueMap(scripts, list3, axis))
                .thenReturn(varValues1);
        when(taskInfo.getMarker()).thenReturn(marker).thenReturn(marker3);
        when(taskInfo.getLabel()).thenReturn(shade);
        // to pass queryProcessor block
        when(scriptProcessor.query(scripts)).thenReturn(null);
        when(queryProcessor.getQueries(dataDef, axisName, itemName))
                .thenReturn(queries);
        when(varSubstitutor.getVarValueMap(queries, list4, axis))
                .thenReturn(varValues2);
        when(taskInfo.getMarker()).thenReturn(marker2);
        when(taskInfo.getLabel()).thenReturn(wolf);
        when(queryProcessor.query(queries, valueParser)).thenReturn(value);
        when(breakAfter.getBreakAfters(dataDef, itemName))
                .thenReturn(breakAfters);
        when(breakAfter.check(breakAfters, value)).thenReturn(avalanche);
        when(prefixProcessor.getPrefixes(dataDef, itemName))
                .thenReturn(prefixes);
        verify(prefixProcessor, never()).prefixValue(eq(value),
                any(ArrayList.class));
        valueProcessor.setAxisValues(dataDef, item, indexMap, indexer,
                valueParser);

        verify(axis0).setIndex(indexMap.get(apple));
        verify(axis).setValue(apricot.replaceAll("\\\\", ""));
        verify(varSubstitutor).replaceVariables(scripts, varValues1);
        verify(scriptProcessor).init(scriptObjectMap);
        verify(varSubstitutor).replaceVariables(queries, varValues2);
        verify(indexer, never()).markBreakAfter(itemName);
        verify(prefixProcessor, never()).prefixValue(eq(value),
                any(ArrayList.class));
        verify(axis).setValue(value);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetAxisValuesIfIsNullTryElseBreakAftersIsPresent()
            throws Exception {
        String dataDef = "Foo";
        Item item = Mockito.mock(Item.class);
        Map<String, Integer> indexMap = new HashMap<>();
        Indexer indexer = Mockito.mock(Indexer.class);
        IValueParser valueParser = Mockito.mock(IValueParser.class);
        boolean replaceBlank = true;
        String blankReplaceWith = "Bar";
        String notFoundReplaceWith = "Baz";

        String apple = "Qux";

        Axis axis0 = Mockito.mock(Axis.class);
        List<Axis> list = new ArrayList<>();
        list.add(axis0);

        Axis axis = Mockito.mock(Axis.class);
        List<Axis> list2 = new ArrayList<>();
        list2.add(axis);

        Map<String, String> scripts = new HashMap<>();
        scripts.put("s", "s1");
        Map<String, String> queries = new HashMap<>();
        queries.put("q", "q1");

        String orange = null;
        String fig = null;

        String axisName = "Quux";
        String itemName = "Corge";

        String mango = "Garply";
        String apricot = "Waldo";

        List<Axis> list3 = new ArrayList<>();
        Map<String, String> varValues1 = new HashMap<>();
        Marker marker = Mockito.mock(Marker.class);
        String shade = "Xyzzy";

        List<Axis> list4 = new ArrayList<>();
        Map<String, String> varValues2 = new HashMap<>();
        Marker marker2 = Mockito.mock(Marker.class);
        String wolf = "Thud";
        String value = "Zoopy";
        Optional<List<String>> breakAfters = Optional.empty();
        Optional<List<String>> prefixes = Optional.empty();

        Marker marker3 = Mockito.mock(Marker.class);

        when(configs.getBoolean("scoopi.fact.blank.replace", true))
                .thenReturn(replaceBlank);
        when(configs.getConfig("scoopi.fact.blank.replaceWith", "-"))
                .thenReturn(blankReplaceWith);
        when(configs.getConfig("scoopi.fact.notFound.replaceWith", "not found"))
                .thenReturn(notFoundReplaceWith);
        when(item.getAxes()).thenReturn(list).thenReturn(list2)
                .thenReturn(list3).thenReturn(list4);
        when(axis0.getItemName()).thenReturn(apple);
        when(axis.getItemName()).thenReturn(itemName);
        when(axis.getAxisName()).thenReturn(axisName);
        when(axis.getValue()).thenReturn(orange).thenReturn(fig);
        when(axis.getMatch()).thenReturn(mango).thenReturn(apricot);
        when(scriptProcessor.getScripts(dataDef, itemName)).thenReturn(scripts);
        when(varSubstitutor.getVarValueMap(scripts, list3, axis))
                .thenReturn(varValues1);
        when(taskInfo.getMarker()).thenReturn(marker).thenReturn(marker3);
        when(taskInfo.getLabel()).thenReturn(shade);
        // to pass queryProcessor block
        when(scriptProcessor.query(scripts)).thenReturn(null);
        when(queryProcessor.getQueries(dataDef, axisName, itemName))
                .thenReturn(queries);
        when(varSubstitutor.getVarValueMap(queries, list4, axis))
                .thenReturn(varValues2);
        when(taskInfo.getMarker()).thenReturn(marker2);
        when(taskInfo.getLabel()).thenReturn(wolf);
        when(queryProcessor.query(queries, valueParser)).thenReturn(value);
        when(breakAfter.getBreakAfters(dataDef, itemName))
                .thenReturn(breakAfters);
        when(prefixProcessor.getPrefixes(dataDef, itemName))
                .thenReturn(prefixes);

        valueProcessor.setAxisValues(dataDef, item, indexMap, indexer,
                valueParser);

        verify(axis0).setIndex(indexMap.get(apple));
        verify(axis).setValue(apricot.replaceAll("\\\\", ""));
        verify(varSubstitutor).replaceVariables(scripts, varValues1);
        verify(scriptProcessor).init(scriptObjectMap);
        verify(varSubstitutor).replaceVariables(queries, varValues2);
        verify(breakAfter, never()).check(breakAfters, value);
        verify(indexer, never()).markBreakAfter(itemName);
        verify(prefixProcessor, never()).prefixValue(eq(value),
                any(ArrayList.class));
        verify(axis).setValue(value);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetAxisValuesIfIsNullTryCatchNoSuchElementException()
            throws Exception {
        String dataDef = "Foo";
        Item item = Mockito.mock(Item.class);
        Map<String, Integer> indexMap = new HashMap<>();
        Indexer indexer = Mockito.mock(Indexer.class);
        IValueParser valueParser = Mockito.mock(IValueParser.class);
        boolean replaceBlank = true;
        String blankReplaceWith = "Bar";
        String notFoundReplaceWith = "Baz";

        String apple = "Qux";

        Axis axis0 = Mockito.mock(Axis.class);
        List<Axis> list = new ArrayList<>();
        list.add(axis0);

        Axis axis = Mockito.mock(Axis.class);
        List<Axis> list2 = new ArrayList<>();
        list2.add(axis);

        Map<String, String> scripts = new HashMap<>();
        scripts.put("s", "s1");
        Map<String, String> queries = new HashMap<>();
        queries.put("q", "q1");

        String orange = null;
        String fig = null;

        String axisName = "Quux";
        String itemName = "Corge";

        String mango = "Garply";
        String apricot = "Waldo";

        List<Axis> list3 = new ArrayList<>();
        Map<String, String> varValues1 = new HashMap<>();
        Marker marker = Mockito.mock(Marker.class);
        String shade = "Xyzzy";

        List<Axis> list4 = new ArrayList<>();
        Map<String, String> varValues2 = new HashMap<>();
        Marker marker2 = Mockito.mock(Marker.class);
        String wolf = "Thud";
        String value = null;
        Optional<List<String>> breakAfters = Optional.empty();
        Optional<List<String>> prefixes = Optional.empty();

        Marker marker3 = Mockito.mock(Marker.class);

        when(configs.getBoolean("scoopi.fact.blank.replace", true))
                .thenReturn(replaceBlank);
        when(configs.getConfig("scoopi.fact.blank.replaceWith", "-"))
                .thenReturn(blankReplaceWith);
        when(configs.getConfig("scoopi.fact.notFound.replaceWith", "not found"))
                .thenReturn(notFoundReplaceWith);
        when(item.getAxes()).thenReturn(list).thenReturn(list2)
                .thenReturn(list3).thenReturn(list4);

        when(axis0.getItemName()).thenReturn(apple);
        when(axis.getItemName()).thenReturn(itemName);

        when(axis.getAxisName()).thenReturn(axisName);
        when(axis.getValue()).thenReturn(orange).thenReturn(fig);
        when(axis.getMatch()).thenReturn(mango).thenReturn(apricot);
        when(scriptProcessor.getScripts(dataDef, itemName)).thenReturn(scripts);
        when(varSubstitutor.getVarValueMap(scripts, list3, axis))
                .thenReturn(varValues1);
        when(taskInfo.getMarker()).thenReturn(marker).thenReturn(marker3);
        when(taskInfo.getLabel()).thenReturn(shade);
        // to pass queryProcessor block
        when(scriptProcessor.query(scripts)).thenReturn(null);
        when(queryProcessor.getQueries(dataDef, axisName, itemName))
                .thenThrow(NoSuchElementException.class);
        when(varSubstitutor.getVarValueMap(queries, list4, axis))
                .thenReturn(varValues2);
        when(taskInfo.getMarker()).thenReturn(marker2);
        when(taskInfo.getLabel()).thenReturn(wolf);
        when(queryProcessor.query(queries, valueParser)).thenReturn(value);
        when(breakAfter.getBreakAfters(dataDef, itemName))
                .thenReturn(breakAfters);
        when(prefixProcessor.getPrefixes(dataDef, itemName))
                .thenReturn(prefixes);

        valueProcessor.setAxisValues(dataDef, item, indexMap, indexer,
                valueParser);

        verify(axis0).setIndex(indexMap.get(apple));
        verify(axis).setValue(apricot.replaceAll("\\\\", ""));
        verify(varSubstitutor).replaceVariables(scripts, varValues1);
        verify(scriptProcessor).init(scriptObjectMap);
        verify(varSubstitutor, never()).replaceVariables(queries, varValues2);
        verify(breakAfter, never()).check(breakAfters, value);
        verify(indexer, never()).markBreakAfter(itemName);
        verify(prefixProcessor, never()).prefixValue(eq(value),
                any(ArrayList.class));
        verify(axis).setValue(value);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetAxisValuesIfIsNullTryElseIsNull() throws Exception {
        String dataDef = "Foo";
        Item item = Mockito.mock(Item.class);
        Map<String, Integer> indexMap = new HashMap<>();
        Indexer indexer = Mockito.mock(Indexer.class);
        IValueParser valueParser = Mockito.mock(IValueParser.class);
        boolean replaceBlank = true;
        String blankReplaceWith = "Bar";
        String notFoundReplaceWith = "Baz";
        Axis axis = Mockito.mock(Axis.class);

        String apple = "Qux";

        Axis axis0 = Mockito.mock(Axis.class);
        List<Axis> list = new ArrayList<>();
        list.add(axis0);

        List<Axis> list2 = new ArrayList<>();
        list2.add(axis);

        Map<String, String> scripts = new HashMap<>();
        scripts.put("s", "s1");
        Map<String, String> queries = new HashMap<>();
        queries.put("q", "q1");

        String orange = null;
        String fig = null;

        String axisName = "Quux";
        String itemName = "Corge";

        String mango = "Garply";
        String apricot = "Waldo";

        List<Axis> list3 = new ArrayList<>();
        Map<String, String> varValues1 = new HashMap<>();
        Marker marker = Mockito.mock(Marker.class);
        String shade = "Xyzzy";

        List<Axis> list4 = new ArrayList<>();
        Map<String, String> varValues2 = new HashMap<>();
        Marker marker2 = Mockito.mock(Marker.class);
        String wolf = "Thud";
        String value = "Zoopy";
        Optional<List<String>> breakAfters = Optional.empty();
        Optional<List<String>> prefixes = Optional.empty();

        Marker marker3 = Mockito.mock(Marker.class);

        when(configs.getBoolean("scoopi.fact.blank.replace", true))
                .thenReturn(replaceBlank);
        when(configs.getConfig("scoopi.fact.blank.replaceWith", "-"))
                .thenReturn(blankReplaceWith);
        when(configs.getConfig("scoopi.fact.notFound.replaceWith", "not found"))
                .thenReturn(notFoundReplaceWith);
        when(item.getAxes()).thenReturn(list).thenReturn(list2)
                .thenReturn(list3);
        when(axis0.getItemName()).thenReturn(apple);
        when(axis.getItemName()).thenReturn(itemName);
        when(axis.getAxisName()).thenReturn(axisName);
        when(axis.getValue()).thenReturn(orange).thenReturn(fig);
        when(axis.getMatch()).thenReturn(mango).thenReturn(apricot);
        when(scriptProcessor.getScripts(dataDef, itemName)).thenReturn(scripts);
        when(varSubstitutor.getVarValueMap(scripts, list3, axis))
                .thenReturn(varValues1);
        when(taskInfo.getMarker()).thenReturn(marker).thenReturn(marker3);
        when(taskInfo.getLabel()).thenReturn(shade);
        when(scriptProcessor.query(scripts)).thenReturn(value);
        when(taskInfo.getMarker()).thenReturn(marker2);
        when(taskInfo.getLabel()).thenReturn(wolf);
        when(breakAfter.getBreakAfters(dataDef, itemName))
                .thenReturn(breakAfters);
        when(prefixProcessor.getPrefixes(dataDef, itemName))
                .thenReturn(prefixes);

        valueProcessor.setAxisValues(dataDef, item, indexMap, indexer,
                valueParser);

        verify(axis0).setIndex(indexMap.get(apple));
        verify(axis).setValue(apricot.replaceAll("\\\\", ""));
        verify(varSubstitutor).replaceVariables(scripts, varValues1);
        verify(scriptProcessor).init(scriptObjectMap);
        verify(queryProcessor, never()).getQueries(dataDef, axisName, itemName);
        verify(item, times(3)).getAxes();
        verify(varSubstitutor, never()).getVarValueMap(queries, list4, axis);
        verify(varSubstitutor, never()).replaceVariables(queries, varValues2);
        verify(queryProcessor, never()).query(queries, valueParser);
        verify(breakAfter, never()).check(breakAfters, value);
        verify(indexer, never()).markBreakAfter(itemName);
        verify(prefixProcessor, never()).prefixValue(eq(value),
                any(ArrayList.class));
        verify(axis).setValue(value);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetAxisValuesIfIsNullTryCatchNoSuchElementException2()
            throws Exception {
        String dataDef = "Foo";
        Item item = Mockito.mock(Item.class);
        Map<String, Integer> indexMap = new HashMap<>();
        Indexer indexer = Mockito.mock(Indexer.class);
        IValueParser valueParser = Mockito.mock(IValueParser.class);
        boolean replaceBlank = true;
        String blankReplaceWith = "Bar";
        String notFoundReplaceWith = "Baz";

        String apple = "Qux";

        Axis axis0 = Mockito.mock(Axis.class);
        List<Axis> list = new ArrayList<>();
        list.add(axis0);

        Axis axis = Mockito.mock(Axis.class);
        List<Axis> list2 = new ArrayList<>();
        list2.add(axis);

        Map<String, String> scripts = new HashMap<>();
        scripts.put("s", "s1");
        Map<String, String> queries = new HashMap<>();
        queries.put("q", "q1");

        String orange = null;
        String fig = null;

        String axisName = "Quux";
        String itemName = "Corge";

        String mango = "Garply";
        String apricot = "Waldo";

        List<Axis> list3 = new ArrayList<>();
        Map<String, String> varValues1 = new HashMap<>();
        Marker marker = Mockito.mock(Marker.class);
        String shade = "Xyzzy";

        List<Axis> list4 = new ArrayList<>();
        Map<String, String> varValues2 = new HashMap<>();
        Marker marker2 = Mockito.mock(Marker.class);
        String wolf = "Thud";
        String value = "Zoopy";
        Optional<List<String>> breakAfters = Optional.empty();
        Optional<List<String>> prefixes = Optional.empty();

        Marker marker3 = Mockito.mock(Marker.class);

        when(configs.getBoolean("scoopi.fact.blank.replace", true))
                .thenReturn(replaceBlank);
        when(configs.getConfig("scoopi.fact.blank.replaceWith", "-"))
                .thenReturn(blankReplaceWith);
        when(configs.getConfig("scoopi.fact.notFound.replaceWith", "not found"))
                .thenReturn(notFoundReplaceWith);
        when(item.getAxes()).thenReturn(list).thenReturn(list2)
                .thenReturn(list3).thenReturn(list4);
        when(axis0.getItemName()).thenReturn(apple);
        when(axis.getItemName()).thenReturn(itemName);
        when(axis.getAxisName()).thenReturn(axisName);
        when(axis.getValue()).thenReturn(orange).thenReturn(fig);
        when(axis.getMatch()).thenReturn(mango).thenReturn(apricot);
        when(scriptProcessor.getScripts(dataDef, itemName))
                .thenThrow(NoSuchElementException.class);
        when(varSubstitutor.getVarValueMap(scripts, list3, axis))
                .thenReturn(varValues1);
        when(taskInfo.getMarker()).thenReturn(marker).thenReturn(marker3);
        when(taskInfo.getLabel()).thenReturn(shade);
        // to pass queryProcessor block
        when(scriptProcessor.query(scripts)).thenReturn(null);
        when(queryProcessor.getQueries(dataDef, axisName, itemName))
                .thenReturn(queries);
        when(varSubstitutor.getVarValueMap(queries, list4, axis))
                .thenReturn(varValues2);
        when(taskInfo.getMarker()).thenReturn(marker2);
        when(taskInfo.getLabel()).thenReturn(wolf);
        when(queryProcessor.query(queries, valueParser)).thenReturn(value);
        when(breakAfter.getBreakAfters(dataDef, itemName))
                .thenReturn(breakAfters);
        when(prefixProcessor.getPrefixes(dataDef, itemName))
                .thenReturn(prefixes);

        valueProcessor.setAxisValues(dataDef, item, indexMap, indexer,
                valueParser);

        verify(axis0).setIndex(indexMap.get(apple));
        verify(axis).setValue(apricot.replaceAll("\\\\", ""));
        verify(varSubstitutor, never()).replaceVariables(scripts, varValues1);
        verify(scriptProcessor, never()).init(scriptObjectMap);
        verify(varSubstitutor).replaceVariables(queries, varValues2);
        verify(breakAfter, never()).check(breakAfters, value);
        verify(indexer, never()).markBreakAfter(itemName);
        verify(prefixProcessor, never()).prefixValue(eq(value),
                any(ArrayList.class));
        verify(axis).setValue(value);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetAxisValuesIfIsNullElseIsNull() throws Exception {
        String dataDef = "Foo";
        Item item = Mockito.mock(Item.class);
        Map<String, Integer> indexMap = new HashMap<>();
        Indexer indexer = Mockito.mock(Indexer.class);
        IValueParser valueParser = Mockito.mock(IValueParser.class);
        boolean replaceBlank = true;
        String blankReplaceWith = "Bar";
        String notFoundReplaceWith = "Baz";

        String apple = "Qux";
        Axis axis0 = Mockito.mock(Axis.class);
        List<Axis> list = new ArrayList<>();
        list.add(axis0);

        Axis axis = Mockito.mock(Axis.class);
        List<Axis> list2 = new ArrayList<>();
        list2.add(axis);

        Map<String, String> scripts = new HashMap<>();
        scripts.put("s", "s1");
        Map<String, String> queries = new HashMap<>();
        queries.put("q", "q1");

        String orange = null;
        // getValue not null for else
        String fig = "Not null";

        String axisName = "Quux";
        String itemName = "Corge";

        String mango = "Garply";
        String apricot = "Waldo";

        List<Axis> list3 = new ArrayList<>();
        Map<String, String> varValues1 = new HashMap<>();
        Marker marker = Mockito.mock(Marker.class);
        String shade = "Xyzzy";

        List<Axis> list4 = new ArrayList<>();
        Map<String, String> varValues2 = new HashMap<>();
        Marker marker2 = Mockito.mock(Marker.class);
        String wolf = "Thud";
        String value = "Zoopy";
        Optional<List<String>> breakAfters = Optional.empty();

        when(configs.getBoolean("scoopi.fact.blank.replace", true))
                .thenReturn(replaceBlank);
        when(configs.getConfig("scoopi.fact.blank.replaceWith", "-"))
                .thenReturn(blankReplaceWith);
        when(configs.getConfig("scoopi.fact.notFound.replaceWith", "not found"))
                .thenReturn(notFoundReplaceWith);
        when(item.getAxes()).thenReturn(list).thenReturn(list2);
        when(axis0.getItemName()).thenReturn(apple);
        when(axis.getItemName()).thenReturn(itemName);
        when(axis.getAxisName()).thenReturn(axisName);
        when(axis.getValue()).thenReturn(orange).thenReturn(fig);
        when(axis.getMatch()).thenReturn(mango).thenReturn(apricot);
        when(taskInfo.getMarker()).thenReturn(marker);
        when(taskInfo.getLabel()).thenReturn(shade);
        when(taskInfo.getMarker()).thenReturn(marker2);
        when(taskInfo.getLabel()).thenReturn(wolf);
        valueProcessor.setAxisValues(dataDef, item, indexMap, indexer,
                valueParser);

        verify(axis0).setIndex(indexMap.get(apple));
        verify(axis).setValue(apricot.replaceAll("\\\\", ""));
        verify(scriptProcessor, never()).getScripts(dataDef, itemName);
        verify(item, times(2)).getAxes();
        verify(varSubstitutor, never()).getVarValueMap(scripts, list3, axis);
        verify(varSubstitutor, never()).replaceVariables(scripts, varValues1);
        verify(scriptProcessor, never()).init(scriptObjectMap);
        verify(scriptProcessor, never()).query(scripts);
        verify(queryProcessor, never()).getQueries(dataDef, axisName, itemName);
        verify(varSubstitutor, never()).getVarValueMap(queries, list4, axis);
        verify(varSubstitutor, never()).replaceVariables(queries, varValues2);
        verify(queryProcessor, never()).query(queries, valueParser);
        verify(breakAfter, never()).getBreakAfters(dataDef, itemName);
        verify(breakAfter, never()).check(breakAfters, value);
        verify(indexer, never()).markBreakAfter(itemName);
        verify(prefixProcessor, never()).getPrefixes(dataDef, itemName);

        verify(prefixProcessor, never()).prefixValue(eq(value),
                any(ArrayList.class));
        verify(taskInfo, never()).getMarker();
        verify(axis, never()).setValue(value);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetAxisValuesElseIsNull() throws Exception {
        String dataDef = "Foo";
        Item item = Mockito.mock(Item.class);
        Map<String, Integer> indexMap = new HashMap<>();
        Indexer indexer = Mockito.mock(Indexer.class);
        IValueParser valueParser = Mockito.mock(IValueParser.class);
        boolean replaceBlank = true;
        String blankReplaceWith = "Bar";
        String notFoundReplaceWith = "Baz";
        String apricot = "Waldo";
        String apple = "Qux";

        Axis axis0 = Mockito.mock(Axis.class);
        List<Axis> list = new ArrayList<>();
        list.add(axis0);

        Axis axis = Mockito.mock(Axis.class);
        List<Axis> list2 = new ArrayList<>();
        list2.add(axis);

        Map<String, String> scripts = new HashMap<>();
        scripts.put("s", "s1");
        Map<String, String> queries = new HashMap<>();
        queries.put("q", "q1");

        // first axis.getValue() is null
        String orange = "not null";
        String fig = null;

        String axisName = "Quux";
        String itemName = "Corge";

        String mango = "Garply";

        List<Axis> list3 = new ArrayList<>();
        Map<String, String> varValues1 = new HashMap<>();
        Marker marker = Mockito.mock(Marker.class);
        String shade = "Plugh";

        List<Axis> list4 = new ArrayList<>();
        Map<String, String> varValues2 = new HashMap<>();
        Marker marker2 = Mockito.mock(Marker.class);
        String wolf = "Xyzzy";
        String value = "Thud";
        Optional<List<String>> breakAfters = Optional.empty();
        Optional<List<String>> prefixes = Optional.empty();

        Marker marker3 = Mockito.mock(Marker.class);

        when(configs.getBoolean("scoopi.fact.blank.replace", true))
                .thenReturn(replaceBlank);
        when(configs.getConfig("scoopi.fact.blank.replaceWith", "-"))
                .thenReturn(blankReplaceWith);
        when(configs.getConfig("scoopi.fact.notFound.replaceWith", "not found"))
                .thenReturn(notFoundReplaceWith);
        when(item.getAxes()).thenReturn(list).thenReturn(list2)
                .thenReturn(list3).thenReturn(list4);
        when(axis0.getItemName()).thenReturn(apple);
        when(axis.getItemName()).thenReturn(itemName);
        when(axis.getAxisName()).thenReturn(axisName);
        when(axis.getValue()).thenReturn(orange).thenReturn(fig);
        when(axis.getMatch()).thenReturn(mango);
        when(scriptProcessor.getScripts(dataDef, itemName)).thenReturn(scripts);
        when(varSubstitutor.getVarValueMap(scripts, list3, axis))
                .thenReturn(varValues1);
        when(taskInfo.getMarker()).thenReturn(marker).thenReturn(marker3);
        when(taskInfo.getLabel()).thenReturn(shade);
        // to pass queryProcessor block
        when(scriptProcessor.query(scripts)).thenReturn(null);
        when(queryProcessor.getQueries(dataDef, axisName, itemName))
                .thenReturn(queries);
        when(varSubstitutor.getVarValueMap(queries, list4, axis))
                .thenReturn(varValues2);
        when(taskInfo.getMarker()).thenReturn(marker2);
        when(taskInfo.getLabel()).thenReturn(wolf);
        when(queryProcessor.query(queries, valueParser)).thenReturn(value);
        when(breakAfter.getBreakAfters(dataDef, itemName))
                .thenReturn(breakAfters);
        when(prefixProcessor.getPrefixes(dataDef, itemName))
                .thenReturn(prefixes);

        valueProcessor.setAxisValues(dataDef, item, indexMap, indexer,
                valueParser);

        verify(axis0).setIndex(indexMap.get(apple));
        verify(axis, never()).getMatch();
        verify(axis, never()).setValue(apricot.replaceAll("\\\\", ""));
        verify(varSubstitutor).replaceVariables(scripts, varValues1);
        verify(scriptProcessor).init(scriptObjectMap);
        verify(varSubstitutor).replaceVariables(queries, varValues2);
        verify(breakAfter, never()).check(breakAfters, value);
        verify(indexer, never()).markBreakAfter(itemName);
        verify(prefixProcessor, never()).prefixValue(eq(value),
                any(ArrayList.class));
        verify(axis).setValue(value);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetAxisValuesElseIsNull2() throws Exception {
        String dataDef = "Foo";
        Item item = Mockito.mock(Item.class);
        Map<String, Integer> indexMap = new HashMap<>();
        Indexer indexer = Mockito.mock(Indexer.class);
        IValueParser valueParser = Mockito.mock(IValueParser.class);
        boolean replaceBlank = true;
        String blankReplaceWith = "Bar";
        String notFoundReplaceWith = "Baz";
        String apricot = "Waldo";
        String apple = "Qux";

        Axis axis0 = Mockito.mock(Axis.class);
        List<Axis> list = new ArrayList<>();
        list.add(axis0);

        Axis axis = Mockito.mock(Axis.class);
        List<Axis> list2 = new ArrayList<>();
        list2.add(axis);

        Map<String, String> scripts = new HashMap<>();
        scripts.put("s", "s1");
        Map<String, String> queries = new HashMap<>();
        queries.put("q", "q1");

        // first axis.getValue() is null
        String orange = "not null";
        String fig = null;

        String axisName = "Quux";
        String itemName = "Corge";

        String mango = null;

        List<Axis> list3 = new ArrayList<>();
        Map<String, String> varValues1 = new HashMap<>();
        Marker marker = Mockito.mock(Marker.class);
        String shade = "Plugh";

        List<Axis> list4 = new ArrayList<>();
        Map<String, String> varValues2 = new HashMap<>();
        Marker marker2 = Mockito.mock(Marker.class);
        String wolf = "Xyzzy";
        String value = "Thud";
        Optional<List<String>> breakAfters = Optional.empty();
        Optional<List<String>> prefixes = Optional.empty();

        Marker marker3 = Mockito.mock(Marker.class);

        when(configs.getBoolean("scoopi.fact.blank.replace", true))
                .thenReturn(replaceBlank);
        when(configs.getConfig("scoopi.fact.blank.replaceWith", "-"))
                .thenReturn(blankReplaceWith);
        when(configs.getConfig("scoopi.fact.notFound.replaceWith", "not found"))
                .thenReturn(notFoundReplaceWith);
        when(item.getAxes()).thenReturn(list).thenReturn(list2)
                .thenReturn(list3).thenReturn(list4);
        when(axis0.getItemName()).thenReturn(apple);
        when(axis.getItemName()).thenReturn(itemName);
        when(axis.getAxisName()).thenReturn(axisName);
        when(axis.getValue()).thenReturn(orange).thenReturn(fig);
        when(axis.getMatch()).thenReturn(mango);
        when(scriptProcessor.getScripts(dataDef, itemName)).thenReturn(scripts);
        when(varSubstitutor.getVarValueMap(scripts, list3, axis))
                .thenReturn(varValues1);
        when(taskInfo.getMarker()).thenReturn(marker).thenReturn(marker3);
        when(taskInfo.getLabel()).thenReturn(shade);
        // to pass queryProcessor block
        when(scriptProcessor.query(scripts)).thenReturn(null);
        when(queryProcessor.getQueries(dataDef, axisName, itemName))
                .thenReturn(queries);
        when(varSubstitutor.getVarValueMap(queries, list4, axis))
                .thenReturn(varValues2);
        when(taskInfo.getMarker()).thenReturn(marker2);
        when(taskInfo.getLabel()).thenReturn(wolf);
        when(queryProcessor.query(queries, valueParser)).thenReturn(value);
        when(breakAfter.getBreakAfters(dataDef, itemName))
                .thenReturn(breakAfters);
        when(prefixProcessor.getPrefixes(dataDef, itemName))
                .thenReturn(prefixes);

        valueProcessor.setAxisValues(dataDef, item, indexMap, indexer,
                valueParser);

        verify(axis0).setIndex(indexMap.get(apple));
        verify(axis, never()).getMatch();
        verify(axis, never()).setValue(apricot.replaceAll("\\\\", ""));
        verify(varSubstitutor).replaceVariables(scripts, varValues1);
        verify(scriptProcessor).init(scriptObjectMap);
        verify(varSubstitutor).replaceVariables(queries, varValues2);
        verify(breakAfter, never()).check(breakAfters, value);
        verify(indexer, never()).markBreakAfter(itemName);
        verify(prefixProcessor, never()).prefixValue(eq(value),
                any(ArrayList.class));
        verify(axis).setValue(value);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetAxisValuesElseIsNull3() throws Exception {
        String dataDef = "Foo";
        Item item = Mockito.mock(Item.class);
        Map<String, Integer> indexMap = new HashMap<>();
        Indexer indexer = Mockito.mock(Indexer.class);
        IValueParser valueParser = Mockito.mock(IValueParser.class);
        boolean replaceBlank = true;
        String blankReplaceWith = "Bar";
        String notFoundReplaceWith = "Baz";
        String apricot = "Waldo";
        String apple = "Qux";

        Axis axis0 = Mockito.mock(Axis.class);
        List<Axis> list = new ArrayList<>();
        list.add(axis0);

        Axis axis = Mockito.mock(Axis.class);
        List<Axis> list2 = new ArrayList<>();
        list2.add(axis);

        Map<String, String> scripts = new HashMap<>();
        scripts.put("script", "s1");
        Map<String, String> queries = new HashMap<>();
        queries.put("q", "q1");

        // first axis.getValue() is null
        String orange = null;
        String fig = null;

        String axisName = "Quux";
        String itemName = "Corge";

        String mango = null;

        List<Axis> list3 = new ArrayList<>();
        Map<String, String> varValues1 = new HashMap<>();
        Marker marker = Mockito.mock(Marker.class);
        String shade = "Plugh";

        List<Axis> list4 = new ArrayList<>();
        Map<String, String> varValues2 = new HashMap<>();
        Marker marker2 = Mockito.mock(Marker.class);
        String wolf = "Xyzzy";
        String value = "Thud";
        Optional<List<String>> breakAfters = Optional.empty();
        Optional<List<String>> prefixes = Optional.empty();

        Marker marker3 = Mockito.mock(Marker.class);

        when(configs.getBoolean("scoopi.fact.blank.replace", true))
                .thenReturn(replaceBlank);
        when(configs.getConfig("scoopi.fact.blank.replaceWith", "-"))
                .thenReturn(blankReplaceWith);
        when(configs.getConfig("scoopi.fact.notFound.replaceWith", "not found"))
                .thenReturn(notFoundReplaceWith);
        when(item.getAxes()).thenReturn(list).thenReturn(list2)
                .thenReturn(list3).thenReturn(list4);
        when(axis0.getItemName()).thenReturn(apple);
        when(axis.getItemName()).thenReturn(itemName);
        when(axis.getAxisName()).thenReturn(axisName);
        when(axis.getValue()).thenReturn(orange).thenReturn(fig);
        when(axis.getMatch()).thenReturn(mango);
        when(scriptProcessor.getScripts(dataDef, itemName)).thenReturn(scripts);
        when(varSubstitutor.getVarValueMap(scripts, list3, axis))
                .thenReturn(varValues1);
        when(taskInfo.getMarker()).thenReturn(marker).thenReturn(marker3);
        when(taskInfo.getLabel()).thenReturn(shade);
        // to pass queryProcessor block
        when(scriptProcessor.query(scripts)).thenReturn(null);
        when(queryProcessor.getQueries(dataDef, axisName, itemName))
                .thenReturn(queries);
        when(varSubstitutor.getVarValueMap(queries, list4, axis))
                .thenReturn(varValues2);
        when(taskInfo.getMarker()).thenReturn(marker2);
        when(taskInfo.getLabel()).thenReturn(wolf);
        when(queryProcessor.query(queries, valueParser)).thenReturn(value);
        when(breakAfter.getBreakAfters(dataDef, itemName))
                .thenReturn(breakAfters);
        when(prefixProcessor.getPrefixes(dataDef, itemName))
                .thenReturn(prefixes);

        valueProcessor.setAxisValues(dataDef, item, indexMap, indexer,
                valueParser);

        verify(axis0).setIndex(indexMap.get(apple));
        verify(axis, times(1)).getMatch();
        verify(axis, never()).setValue(apricot.replaceAll("\\\\", ""));
        verify(varSubstitutor).replaceVariables(scripts, varValues1);
        verify(scriptProcessor).init(scriptObjectMap);
        verify(varSubstitutor).replaceVariables(queries, varValues2);
        verify(breakAfter, never()).check(breakAfters, value);
        verify(indexer, never()).markBreakAfter(itemName);
        verify(prefixProcessor, never()).prefixValue(eq(value),
                any(ArrayList.class));
        verify(axis).setValue(value);
    }

    @Test
    public void testAddScriptObjectIfIsNull() throws Exception {
        String key = "Foo";
        Object value = Mockito.mock(Object.class);

        FieldUtils.writeDeclaredField(valueProcessor, "scriptObjectMap", null,
                true);

        valueProcessor.addScriptObject(key, value);

        @SuppressWarnings("unchecked")
        Map<String, Object> actual = (Map<String, Object>) FieldUtils
                .readDeclaredField(valueProcessor, "scriptObjectMap", true);

        assertEquals(1, actual.size());
        assertSame(value, actual.get(key));
    }

    @Test
    public void testAddScriptObjectElseIsNull() throws Exception {
        String key = "Foo";
        Object value = Mockito.mock(Object.class);

        Map<String, Object> scriptObjectMap1 = new HashMap<>();
        FieldUtils.writeDeclaredField(valueProcessor, "scriptObjectMap",
                scriptObjectMap1, true);

        valueProcessor.addScriptObject(key, value);

        assertEquals(1, scriptObjectMap1.size());
        assertSame(value, scriptObjectMap1.get(key));
    }

    private static void setFinalStaticField(final Class<?> clazz,
            final String fieldName, final Object value)
            throws ReflectiveOperationException {

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);

        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, value);
    }
}
