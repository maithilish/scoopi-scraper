package org.codetab.scoopi.plugin.script;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.script.ScriptEngine;

import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.model.Plugin;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ScriptFactoryTest {
    @InjectMocks
    private ScriptFactory scriptFactory;

    @Mock
    private DInjector di;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateScript() throws Exception {
        ScriptEngine engine = Mockito.mock(ScriptEngine.class);
        Plugin plugin = Mockito.mock(Plugin.class);
        String apple = "Foo";
        IScript script = Mockito.mock(IScript.class);

        when(plugin.getClassName()).thenReturn(apple);
        when(di.instance(apple, IScript.class)).thenReturn(script);

        IScript actual = scriptFactory.createScript(engine, plugin);

        assertSame(script, actual);
        verify(script).setPlugin(plugin);
        verify(script).setScriptEngine(engine);
    }
}
