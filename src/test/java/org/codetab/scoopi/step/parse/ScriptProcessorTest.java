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
import org.codetab.scoopi.model.TaskInfo;
import org.codetab.scoopi.step.parse.cache.ParserCache;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ScriptProcessorTest {

    @Mock
    private IAxisDefs axisDefs;
    @Mock
    private ScriptParser scriptParser;
    @Mock
    private ParserCache parserCache;
    @Mock
    private TaskInfo taskInfo;

    @InjectMocks
    private ScriptProcessor scriptProcessor;

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
    public void testInit() {
        Map<String, Object> scriptObjectMap = new HashMap<>();

        scriptProcessor.init(scriptObjectMap);

        verify(scriptParser).initScriptEngine(scriptObjectMap);
    }

    @Test
    public void testInitNullParams() {
        try {
            scriptProcessor.init(null);
            fail("should throw exception");
        } catch (NullPointerException e) {
            assertThat(e.getMessage())
                    .isEqualTo("scriptObjectMap must not be null");
        }
    }

    @Test
    public void testGetScripts() {
        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "def");
        AxisName axisName = AxisName.COL;
        String script = "script";

        given(axisDefs.getQuery(dataDef, axisName, "script"))
                .willReturn(script);

        Map<String, String> actual =
                scriptProcessor.getScripts(dataDef, axisName);

        assertThat(actual.size()).isEqualTo(1);
        assertThat(actual.get("script")).isEqualTo(script);
    }

    @Test
    public void testGetScriptsShouldThrowException() {
        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "def");
        AxisName axisName = AxisName.COL;
        String script = "undefined";

        given(axisDefs.getQuery(dataDef, axisName, "script"))
                .willReturn(script);

        try {
            scriptProcessor.getScripts(dataDef, axisName);
            fail("should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            assertThat(e.getMessage()).isEqualTo("script not defined");
        }

    }

    @Test
    public void testQuery() throws ScriptException {
        String script = "script";
        Map<String, String> scripts = new HashMap<>();
        scripts.put("script", script);
        int key = 5;
        Date obj = new Date();

        given(parserCache.getKey(scripts)).willReturn(key);
        given(parserCache.get(key)).willReturn(null);
        given(scriptParser.eval(script)).willReturn(obj);

        String actual = scriptProcessor.query(scripts);

        assertThat(actual).isEqualTo(obj.toString());

        verify(parserCache).put(key, obj.toString());
    }

    @Test
    public void testQueryFromCache() throws ScriptException {
        Map<String, String> scripts = new HashMap<>();
        int key = 5;
        String value = "test";

        given(parserCache.getKey(scripts)).willReturn(key);
        given(parserCache.get(key)).willReturn(value);

        String actual = scriptProcessor.query(scripts);

        assertThat(actual).isEqualTo(value);
    }

}
