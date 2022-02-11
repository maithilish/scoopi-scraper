package org.codetab.scoopi.plugin.script;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.script.ScriptEngine;

import org.apache.commons.pool2.PooledObject;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.helper.IOHelper;
import org.codetab.scoopi.model.Plugin;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ScriptEngineFactoryTest {
    @InjectMocks
    private ScriptEngineFactory scriptEngineFactory;

    @Mock
    private IOHelper ioHelper;
    @Mock
    private IPluginDef pluginDef;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreate() throws Exception {
        Plugin plugin = Mockito.mock(Plugin.class);
        String taskGroup = "Foo";
        String taskName = "Bar";
        String stepName = "Baz";
        String pluginName = "Qux";
        String key =
                String.join("-", taskGroup, taskName, stepName, pluginName);

        String mainScript = "Quux";
        Optional<List<String>> scripts = Optional.empty();
        String script = "Corge";
        Reader reader = Mockito.mock(Reader.class);

        when(plugin.getTaskGroup()).thenReturn(taskGroup);
        when(plugin.getTaskName()).thenReturn(taskName);
        when(plugin.getStepName()).thenReturn(stepName);
        when(plugin.getName()).thenReturn(pluginName);
        when(pluginDef.getValue(plugin, "script")).thenReturn(mainScript);
        when(ioHelper.getReader(mainScript)).thenReturn(reader);
        when(pluginDef.getArrayValues(plugin, "scripts")).thenReturn(scripts);
        when(ioHelper.getReader(script)).thenReturn(reader);

        ScriptEngine actual = scriptEngineFactory.create(plugin);

        verify(reader, times(2)).close();
        assertEquals(key, actual.get("scoopiPluginKey"));
    }

    @Test
    public void testCreateScriptsIsPresent() throws Exception {
        Plugin plugin = Mockito.mock(Plugin.class);
        String taskGroup = "Foo";
        String taskName = "Bar";
        String stepName = "Baz";
        String pluginName = "Qux";
        String key =
                String.join("-", taskGroup, taskName, stepName, pluginName);

        String mainScript = "Quux";

        String script = "Corge";
        List<String> scriptList = Collections.singletonList(script);
        Optional<List<String>> scripts = Optional.of(scriptList);

        Reader reader = Mockito.mock(Reader.class);

        when(plugin.getTaskGroup()).thenReturn(taskGroup);
        when(plugin.getTaskName()).thenReturn(taskName);
        when(plugin.getStepName()).thenReturn(stepName);
        when(plugin.getName()).thenReturn(pluginName);
        when(pluginDef.getValue(plugin, "script")).thenReturn(mainScript);
        when(ioHelper.getReader(mainScript)).thenReturn(reader);
        when(pluginDef.getArrayValues(plugin, "scripts")).thenReturn(scripts);
        when(ioHelper.getReader(script)).thenReturn(reader);

        ScriptEngine actual = scriptEngineFactory.create(plugin);

        verify(reader, times(4)).close();
        assertEquals(key, actual.get("scoopiPluginKey"));
    }

    @Test
    public void testWrap() {
        ScriptEngine value = Mockito.mock(ScriptEngine.class);

        PooledObject<ScriptEngine> actual = scriptEngineFactory.wrap(value);

        assertSame(value, actual.getObject());
    }
}
