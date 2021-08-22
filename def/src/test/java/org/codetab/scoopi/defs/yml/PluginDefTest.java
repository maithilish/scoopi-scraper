package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.Plugin;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class PluginDefTest {

    @InjectMocks
    private PluginDef pluginDef;

    @Mock
    private PluginDefs pluginDefs;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PluginDefData data;

    @Before()
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetPlugins() throws Exception {
        Plugin plugin = Mockito.mock(Plugin.class);
        Plugin pluginCopy = Mockito.mock(Plugin.class);

        List<Plugin> plugins = new ArrayList<>();
        plugins.add(plugin);

        String taskGroup = "foo";
        String taskName = "bar";
        String stepName = "baz";
        String key = String.join("-", taskGroup, taskName, stepName);

        when(data.getPluginMap().get(key)).thenReturn(plugins);
        when(pluginDefs.copy(plugin)).thenReturn(pluginCopy);

        Optional<List<Plugin>> actual =
                pluginDef.getPlugins(taskGroup, taskName, stepName);

        assertThat(actual).isPresent();

        assertThat(actual.get()).hasSize(1);
        assertThat(actual.get().get(0)).isSameAs(pluginCopy);
    }

    @Test
    public void testGetPluginsIsNull() throws Exception {
        Plugin plugin = Mockito.mock(Plugin.class);

        List<Plugin> plugins = new ArrayList<>();
        plugins.add(plugin);

        String taskGroup = "foo";
        String taskName = "bar";
        String stepName = "baz";
        String key = String.join("-", taskGroup, taskName, stepName);

        when(data.getPluginMap().get(key)).thenReturn(null);

        Optional<List<Plugin>> actual =
                pluginDef.getPlugins(taskGroup, taskName, stepName);

        assertThat(actual).isEmpty();
    }

    @Test
    public void testGetPluginsPlugin() throws Exception {
        Plugin plugin = Mockito.mock(Plugin.class);
        @SuppressWarnings("unchecked")
        Optional<List<Plugin>> pluginList =
                Optional.of(Mockito.mock(List.class));
        when(pluginDefs.getPlugins(plugin)).thenReturn(pluginList);

        Optional<List<Plugin>> actual = pluginDef.getPlugins(plugin);

        assertThat(actual).isPresent();

        assertThat(actual).isSameAs(pluginList);
    }

    @Test
    public void testGetValue() throws Exception {
        Plugin plugin = Mockito.mock(Plugin.class);
        String field = "foo";

        when(pluginDefs.getFieldValue(plugin, field)).thenReturn("baz");

        String actual = pluginDef.getValue(plugin, field);

        assertThat(actual).isEqualTo("baz");
    }

    @Test
    public void testGetValuePluginStringString() throws DefNotFoundException {
        Plugin plugin = Mockito.mock(Plugin.class);
        String field = "foo";
        String defaultValue = "bar";

        when(pluginDefs.getFieldValue(plugin, field)).thenReturn("baz");

        String actual = pluginDef.getValue(plugin, field, defaultValue);

        assertThat(actual).isEqualTo("baz");
    }

    @Test
    public void testGetValuePluginStringStringDefaultValue()
            throws DefNotFoundException {
        Plugin plugin = Mockito.mock(Plugin.class);
        String field = "foo";
        String defaultValue = "bar";

        when(pluginDefs.getFieldValue(plugin, field))
                .thenThrow(DefNotFoundException.class);

        String actual = pluginDef.getValue(plugin, field, defaultValue);

        assertThat(actual).isEqualTo(defaultValue);
    }

    @Test
    public void testGetArrayValues() {
        List<String> list = new ArrayList<>();
        Plugin plugin = Mockito.mock(Plugin.class);
        String field = "foo";

        when(pluginDefs.getArrayValues(plugin, field)).thenReturn(list);

        Optional<List<String>> actual = pluginDef.getArrayValues(plugin, field);

        assertThat(actual).isPresent();

        assertThat(actual.get()).isSameAs(list);
    }

    @Test
    public void testGetArrayValuesFieldNotFound() {
        Plugin plugin = Mockito.mock(Plugin.class);
        String field = "foo";

        when(pluginDefs.getArrayValues(plugin, field)).thenReturn(null);

        Optional<List<String>> actual = pluginDef.getArrayValues(plugin, field);

        assertThat(actual).isEmpty();
    }
}
