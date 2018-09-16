package org.codetab.scoopi.step.parse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.model.TaskInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ScriptParserTest {

    @Mock
    private ScriptEngineManager scriptEngineMgr;
    @Mock
    private TaskInfo taskInfo;

    @InjectMocks
    private ScriptParser scriptParser;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testEval() throws ScriptException {
        Date date = new Date();
        Map<String, Object> scriptObjectMap = new HashMap<>();
        scriptObjectMap.put("now", date);

        ScriptEngine engine =
                new ScriptEngineManager().getEngineByName("JavaScript");

        given(scriptEngineMgr.getEngineByName("JavaScript")).willReturn(engine);
        scriptParser.initScriptEngine(scriptObjectMap);

        long actual = (long) scriptParser.eval("now.getTime()");

        assertThat(actual).isEqualTo(date.getTime());
    }

    @Test
    public void testEvalInvalidState() throws ScriptException {
        try {
            scriptParser.eval("now.getTime()");
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage())
                    .isEqualTo("script engine not initialized");
        }
    }

    @Test
    public void testInitScriptEngineAlreadyInitialized()
            throws ScriptException {
        ScriptEngine engine =
                new ScriptEngineManager().getEngineByName("JavaScript");
        given(scriptEngineMgr.getEngineByName("JavaScript")).willReturn(engine);

        scriptParser.initScriptEngine(new HashMap<>());
        scriptParser.initScriptEngine(null);

        verify(scriptEngineMgr, times(1)).getEngineByName("JavaScript");
    }

    @Test
    public void testInitScriptEngineShouldThrowException()
            throws ScriptException {
        given(scriptEngineMgr.getEngineByName("JavaScript")).willReturn(null);

        testRule.expect(CriticalException.class);
        scriptParser.initScriptEngine(new HashMap<>());
    }
}
