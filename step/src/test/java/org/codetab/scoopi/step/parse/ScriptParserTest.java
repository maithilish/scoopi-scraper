package org.codetab.scoopi.step.parse;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.model.TaskInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ScriptParserTest {
    @InjectMocks
    private ScriptParser scriptParser;

    @Mock
    private ScriptEngineManager seManager;
    @Mock
    private TaskInfo taskInfo;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInitScriptEngineIfNonNull() throws Exception {
        Map<String, Object> scriptObjectMap = new HashMap<>();
        Marker marker = Mockito.mock(Marker.class);
        ScriptEngine jsEngine = Mockito.mock(ScriptEngine.class);
        String key = "Foo";
        Object orange = Mockito.mock(Object.class);
        scriptObjectMap.put(key, orange);

        FieldUtils.writeDeclaredField(scriptParser, "jsEngine", jsEngine, true);

        when(taskInfo.getMarker()).thenReturn(marker);
        when(seManager.getEngineByName("JavaScript")).thenReturn(jsEngine);
        scriptParser.initScriptEngine(scriptObjectMap);

        verify(jsEngine, never()).put(key, orange);
    }

    @Test
    public void testInitScriptEngine() {
        Map<String, Object> scriptObjectMap = new HashMap<>();
        Marker marker = Mockito.mock(Marker.class);
        ScriptEngine jsEngine = Mockito.mock(ScriptEngine.class);
        String key = "Foo";
        Object orange = Mockito.mock(Object.class);
        scriptObjectMap.put(key, orange);

        when(taskInfo.getMarker()).thenReturn(marker);
        when(seManager.getEngineByName("JavaScript")).thenReturn(jsEngine);
        scriptParser.initScriptEngine(scriptObjectMap);

        verify(jsEngine).put(key, orange);
    }

    @Test
    public void testInitScriptEngineMapException() {
        Map<String, Object> scriptObjectMap = new HashMap<>();
        Marker marker = Mockito.mock(Marker.class);
        ScriptEngine jsEngine = null; // engine null
        String key = "Foo";
        Object orange = Mockito.mock(Object.class);
        scriptObjectMap.put(key, orange);

        when(taskInfo.getMarker()).thenReturn(marker);
        when(seManager.getEngineByName("JavaScript")).thenReturn(jsEngine);

        assertThrows(CriticalException.class,
                () -> scriptParser.initScriptEngine(scriptObjectMap));
    }

    @Test
    public void testEval() throws Exception {
        String script = "Foo";
        Object grape = Mockito.mock(Object.class);
        ScriptEngine jsEngine = Mockito.mock(ScriptEngine.class);

        FieldUtils.writeDeclaredField(scriptParser, "jsEngine", jsEngine, true);

        when(jsEngine.eval(script)).thenReturn(grape);

        Object actual = scriptParser.eval(script);

        assertSame(grape, actual);
    }
}
