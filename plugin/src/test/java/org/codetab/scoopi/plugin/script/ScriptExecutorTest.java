package org.codetab.scoopi.plugin.script;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;

import org.codetab.scoopi.model.Plugin;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ScriptExecutorTest {
    @InjectMocks
    private ScriptExecutor scriptExecutor;

    @Mock
    private ScriptFactory scriptFactory;
    @Mock
    private ScriptEnginePool scriptEnginePool;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testExecute() throws Exception {
        List<Plugin> plugins = new ArrayList<>();
        Plugin plugin = Mockito.mock(Plugin.class);
        plugins.add(plugin);

        Object input = Mockito.mock(Object.class);
        Object scriptInput = input;

        ScriptEngine engine = Mockito.mock(ScriptEngine.class);
        IScript script = Mockito.mock(IScript.class);
        Object scriptOutput = Mockito.mock(Object.class);

        when(scriptEnginePool.borrowObject(plugin)).thenReturn(engine);
        when(scriptFactory.createScript(engine, plugin)).thenReturn(script);
        when(script.execute(scriptInput)).thenReturn(scriptOutput);

        Object actual = scriptExecutor.execute(plugins, input);

        assertSame(scriptOutput, actual);
        verify(scriptEnginePool).returnObject(plugin, engine);
    }

    @Test
    public void testExecuteEngineIsNull() throws Exception {
        List<Plugin> plugins = new ArrayList<>();
        Plugin plugin = Mockito.mock(Plugin.class);
        plugins.add(plugin);

        Object input = Mockito.mock(Object.class);

        ScriptEngine engine = Mockito.mock(ScriptEngine.class);

        when(scriptEnginePool.borrowObject(plugin)).thenReturn(null); // null

        assertThrows(NullPointerException.class,
                () -> scriptExecutor.execute(plugins, input));

        verify(scriptEnginePool, never()).returnObject(plugin, engine);
    }
}
