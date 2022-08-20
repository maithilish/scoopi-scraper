package org.codetab.scoopi.step.parse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.IItemDef;
import org.codetab.scoopi.model.Query;
import org.codetab.scoopi.model.TaskInfo;
import org.codetab.scoopi.step.parse.cache.ParserCache;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ScriptProcessorTest {
    @InjectMocks
    private ScriptProcessor scriptProcessor;

    @Mock
    private IItemDef itemDef;
    @Mock
    private ScriptParser scriptParser;
    @Mock
    private ParserCache parserCache;
    @Mock
    private TaskInfo taskInfo;
    @Mock
    private Configs configs;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInit() {
        Map<String, Object> scriptObjectMap = new HashMap<>();
        scriptProcessor.init(scriptObjectMap);

        verify(scriptParser).initScriptEngine(scriptObjectMap);
    }

    @Test
    public void testGetScripts() {
        String dataDef = "Foo";
        String itemName = "Bar";
        Map<String, String> scripts = new HashMap<>();
        Query query2 = Mockito.mock(Query.class);
        String script = "Baz";
        Optional<Query> query = Optional.of(query2);
        scripts.put("script", script);

        when(itemDef.getItemQuery(dataDef, itemName)).thenReturn(query);
        when(query2.getQuery("script")).thenReturn(script);

        Map<String, String> actual =
                scriptProcessor.getScripts(dataDef, itemName);

        assertEquals(scripts, actual);
    }

    @Test
    public void testGetScriptsException() {
        String dataDef = "Foo";
        String itemName = "Bar";
        Query query2 = Mockito.mock(Query.class);
        String script = "Baz";
        Optional<Query> query = Optional.empty();

        when(itemDef.getItemQuery(dataDef, itemName)).thenReturn(query);
        when(query2.getQuery("script")).thenReturn(script);

        assertThrows(NoSuchElementException.class,
                () -> scriptProcessor.getScripts(dataDef, itemName));
        verify(query2, never()).getQuery("script");
    }

    @Test
    public void testGetScriptsException2() {
        String dataDef = "Foo";
        String itemName = "Bar";
        Query query2 = Mockito.mock(Query.class);
        Optional<Query> query = Optional.of(query2);

        when(itemDef.getItemQuery(dataDef, itemName)).thenReturn(query);
        when(query2.getQuery("script")).thenThrow(NoSuchElementException.class);

        assertThrows(NoSuchElementException.class,
                () -> scriptProcessor.getScripts(dataDef, itemName));
        verify(query2).getQuery("script");
    }

    @Test
    public void testQueryIf() throws Exception {
        Map<String, String> scripts = new HashMap<>();
        int key = 1;
        ZonedDateTime val = Mockito.mock(ZonedDateTime.class);
        ZonedDateTime zdt = Mockito.mock(ZonedDateTime.class);
        DateTimeFormatter dateTimeFormatter =
                Mockito.mock(DateTimeFormatter.class);
        String value = null;
        Marker marker = Mockito.mock(Marker.class);
        String mango = "Foo";

        when(parserCache.getKey(scripts)).thenReturn(key);
        when(parserCache.get(key)).thenReturn(value);
        when(scriptParser.eval(scripts.get("script"))).thenReturn(val);
        when(configs.getDateTimeFormatter()).thenReturn(dateTimeFormatter);
        when(zdt.format(dateTimeFormatter)).thenReturn(value);
        when(taskInfo.getMarker()).thenReturn(marker);
        when(taskInfo.getLabel()).thenReturn(mango);

        String actual = scriptProcessor.query(scripts);

        assertEquals(value, actual);
        verify(parserCache).put(key, value);
    }

    @Test
    public void testQueryElse() throws Exception {
        Map<String, String> scripts = new HashMap<>();
        int key = 1;
        Integer val = 5;
        ZonedDateTime zdt = Mockito.mock(ZonedDateTime.class);
        DateTimeFormatter dateTimeFormatter =
                Mockito.mock(DateTimeFormatter.class);
        String value = "5";
        Marker marker = Mockito.mock(Marker.class);
        String mango = "Foo";

        when(parserCache.getKey(scripts)).thenReturn(key);
        when(parserCache.get(key)).thenReturn(null); // value is null
        when(scriptParser.eval(scripts.get("script"))).thenReturn(val);
        when(taskInfo.getMarker()).thenReturn(marker);
        when(taskInfo.getLabel()).thenReturn(mango);

        String actual = scriptProcessor.query(scripts);

        assertEquals(value, actual);
        verify(configs, never()).getDateTimeFormatter();
        verify(zdt, never()).format(dateTimeFormatter);
        verify(parserCache).put(key, value);
    }

    @Test
    public void testQuery() throws Exception {
        Map<String, String> scripts = new HashMap<>();
        int key = 1;
        ZonedDateTime zdt = Mockito.mock(ZonedDateTime.class);
        DateTimeFormatter dateTimeFormatter =
                Mockito.mock(DateTimeFormatter.class);
        String value = "5";
        Marker marker = Mockito.mock(Marker.class);
        String mango = "Foo";

        when(parserCache.getKey(scripts)).thenReturn(key);
        when(parserCache.get(key)).thenReturn(value);
        when(taskInfo.getMarker()).thenReturn(marker);
        when(taskInfo.getLabel()).thenReturn(mango);

        String actual = scriptProcessor.query(scripts);

        assertEquals(value, actual);
        verify(scriptParser, never()).eval(scripts.get("script"));
        verify(configs, never()).getDateTimeFormatter();
        verify(zdt, never()).format(dateTimeFormatter);
        verify(parserCache, never()).put(key, value);
    }
}
