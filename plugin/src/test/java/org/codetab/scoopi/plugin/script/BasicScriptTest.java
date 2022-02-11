package org.codetab.scoopi.plugin.script;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.model.Plugin;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class BasicScriptTest {
    @InjectMocks
    private BasicScript basicScript;

    @Mock
    private Plugin plugin;
    @Mock
    private IPluginDef pluginDef;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testExecute() throws Exception {
        Object input = "foo";
        String output = "hi foo";
        String functionName = "callFoo";

        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngine engine = engineManager.getEngineByName("JavaScript");
        basicScript.setScriptEngine(engine);
        engine.eval("function callFoo(input) { return 'hi ' + input; };");

        when(pluginDef.getValue(plugin, "entryPoint", "execute"))
                .thenReturn(functionName);

        Object actual = basicScript.execute(input);

        assertEquals(output, actual);
    }

    @Test
    public void testSetPlugin() throws IllegalAccessException {
        Plugin p = Mockito.mock(Plugin.class);
        basicScript.setPlugin(p);

        assertSame(p,
                FieldUtils.readDeclaredField(basicScript, "plugin", true));
    }

    @Test
    public void testSetScriptEngine() throws IllegalAccessException {
        ScriptEngine scriptEngine = Mockito.mock(ScriptEngine.class);
        basicScript.setScriptEngine(scriptEngine);

        assertSame(scriptEngine, FieldUtils.readDeclaredField(basicScript,
                "scriptEngine", true));
    }
}
