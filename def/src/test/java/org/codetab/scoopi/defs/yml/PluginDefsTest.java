package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.InvalidDefException;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Plugin;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public class PluginDefsTest {

    @InjectMocks
    private PluginDefs pluginDefs;

    @Mock
    private Jacksons jacksons;

    @Mock
    private Yamls yamls;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ObjectFactory objectFactory;

    @Before()
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetPluginMap() throws Exception {
        String taskGroup = "foo";
        String taskName = "bar";
        String stepName = "baz";
        String key = String.join("-", taskGroup, taskName, stepName);

        String pluginName = "cruz";
        String className = "ducks";

        Plugin plugin = Mockito.mock(Plugin.class);
        String defJson = "boom";

        JsonNode jPlugin = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        List<JsonNode> jPlugins = new ArrayList<>();
        jPlugins.add(jPlugin);

        JsonNode steps = Mockito.mock(JsonNode.class);
        Map<String, JsonNode> stepsMap = new HashMap<>();
        stepsMap.put(key, steps);

        when(steps.findValues("plugin")).thenReturn(jPlugins);
        when(jPlugin.at("/class").asText()).thenReturn(className);
        when(jPlugin.at("/name").asText()).thenReturn(pluginName);
        when(yamls.toJson(jPlugin)).thenReturn(defJson);
        when(objectFactory.createPlugin(pluginName, className, taskGroup,
                taskName, stepName, defJson, jPlugin)).thenReturn(plugin);

        Map<String, List<Plugin>> actual = pluginDefs.getPluginMap(stepsMap);

        assertThat(actual).hasSize(1);
        assertThat(actual.get(key)).hasSize(1); // list of plugin

        assertThat(actual.get(key).get(0)).isSameAs(plugin);
    }

    @Test
    public void testGetPluginMapNoPluginsDefined() throws Exception {
        String taskGroup = "foo";
        String taskName = "bar";
        String stepName = "baz";
        String key = String.join("-", taskGroup, taskName, stepName);

        // empty plugin list
        List<JsonNode> jPlugins = new ArrayList<>();

        JsonNode steps = Mockito.mock(JsonNode.class);
        Map<String, JsonNode> stepsMap = new HashMap<>();
        stepsMap.put(key, steps);

        when(steps.findValues("plugin")).thenReturn(jPlugins);

        Map<String, List<Plugin>> actual = pluginDefs.getPluginMap(stepsMap);

        assertThat(actual).hasSize(1);
        assertThat(actual.get(key)).isNull();
    }

    @Test
    public void testGetPluginMapInvalidPluginDef() throws Exception {
        String taskGroup = "foo";
        String taskName = "bar";
        String stepName = "baz";
        String key = String.join("-", taskGroup, taskName, stepName);

        String pluginName = "cruz";
        String className = "ducks";

        JsonNode jPlugin = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        List<JsonNode> jPlugins = new ArrayList<>();
        jPlugins.add(jPlugin);

        JsonNode steps = Mockito.mock(JsonNode.class);
        Map<String, JsonNode> stepsMap = new HashMap<>();
        stepsMap.put(key, steps);

        when(yamls.toJson(jPlugin)).thenThrow(JsonProcessingException.class);
        when(jPlugin.at("/class").asText()).thenReturn(className);
        when(jPlugin.at("/name").asText()).thenReturn(pluginName);
        when(steps.findValues("plugin")).thenReturn(jPlugins);

        assertThrows(InvalidDefException.class,
                () -> pluginDefs.getPluginMap(stepsMap));
    }

    @Test
    public void testGetPlugins() throws Exception {
        String className = "foo";
        String defJson = "bar";
        String pluginName = "baz";

        Plugin plugin = Mockito.mock(Plugin.class);
        JsonNode pluginDef = Mockito.mock(JsonNode.class);

        JsonNode jPlugin = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        List<JsonNode> jPlugins = new ArrayList<>();
        jPlugins.add(jPlugin);

        Plugin childPlugin = Mockito.mock(Plugin.class);

        when(plugin.getDef()).thenReturn(pluginDef);
        when(yamls.toJson(jPlugin)).thenReturn(defJson);
        when(jPlugin.at("/class").asText()).thenReturn(className);
        when(jPlugin.at("/name").asText()).thenReturn(pluginName);

        when(plugin.getTaskGroup()).thenReturn("ttaskgroup");
        when(plugin.getTaskName()).thenReturn("ttaskname");
        when(plugin.getStepName()).thenReturn("tstepname");

        when(objectFactory.createPlugin(pluginName, className,
                plugin.getTaskGroup(), plugin.getTaskName(),
                plugin.getStepName(), defJson, jPlugin))
                        .thenReturn(childPlugin);

        when(pluginDef.findValues("plugin")).thenReturn(jPlugins);

        Optional<List<Plugin>> actual = pluginDefs.getPlugins(plugin);

        assertThat(actual).isPresent();
        assertThat(actual.get()).hasSize(1);

        assertThat(actual.get().get(0)).isSameAs(childPlugin);
    }

    @Test
    public void testGetPluginsInvalidDef() throws Exception {
        Plugin plugin = Mockito.mock(Plugin.class);
        JsonNode pluginDef = Mockito.mock(JsonNode.class);

        JsonNode jPlugin = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        List<JsonNode> jPlugins = new ArrayList<>();
        jPlugins.add(jPlugin);

        when(plugin.getDef()).thenReturn(pluginDef);
        when(yamls.toJson(jPlugin)).thenThrow(JsonProcessingException.class);

        when(pluginDef.findValues("plugin")).thenReturn(jPlugins);

        when(plugin.toString()).thenReturn("plugin toString");

        assertThrows(InvalidDefException.class,
                () -> pluginDefs.getPlugins(plugin));
    }

    @Test
    public void testGetPluginsNoPluginDefined() throws Exception {
        Plugin plugin = Mockito.mock(Plugin.class);
        JsonNode pluginDef = Mockito.mock(JsonNode.class);

        List<JsonNode> jPlugins = new ArrayList<>();

        when(plugin.getDef()).thenReturn(pluginDef);
        when(pluginDef.findValues("plugin")).thenReturn(jPlugins);

        Optional<List<Plugin>> actual = pluginDefs.getPlugins(plugin);

        assertThat(actual).isEmpty();
    }

    @Test
    public void testGetFieldValue() throws Exception {
        String path = "foo";
        JsonNode jField = Mockito.mock(JsonNode.class);
        JsonNode def = Mockito.mock(JsonNode.class);
        Plugin plugin = Mockito.mock(Plugin.class);
        String field = "bar";
        String value = "baz";

        when(plugin.getDef()).thenReturn(def);
        when(jacksons.path(field)).thenReturn(path);
        when(def.at(path)).thenReturn(jField);

        when(jField.isMissingNode()).thenReturn(false);
        when(jField.asText()).thenReturn(value);

        String actual = pluginDefs.getFieldValue(plugin, field);

        assertThat(actual).isEqualTo(value);
    }

    @Test
    public void testGetFieldValueDefNotFound() throws Exception {
        String path = "foo";
        JsonNode jField = Mockito.mock(JsonNode.class);
        JsonNode def = Mockito.mock(JsonNode.class);
        Plugin plugin = Mockito.mock(Plugin.class);
        String field = "bar";

        when(plugin.getDef()).thenReturn(def);
        when(jacksons.path(field)).thenReturn(path);
        when(def.at(path)).thenReturn(jField);

        when(jField.isMissingNode()).thenReturn(true);
        when(plugin.toString()).thenReturn("baz");

        assertThrows(DefNotFoundException.class,
                () -> pluginDefs.getFieldValue(plugin, field));
    }

    @Test
    public void testGetArrayValues() {
        JsonNode def = Mockito.mock(JsonNode.class);
        Plugin plugin = Mockito.mock(Plugin.class);
        String field = "foo";
        List<String> expected = new ArrayList<>();

        when(plugin.getDef()).thenReturn(def);

        when(jacksons.getArrayAsStrings(def, field)).thenReturn(expected);

        List<String> actual = pluginDefs.getArrayValues(plugin, field);

        assertThat(actual).isSameAs(expected);
    }

    @Test
    public void testCopy() {
        JsonNode jPlugin = Mockito.mock(JsonNode.class);
        JsonNode jPluginCopy = Mockito.mock(JsonNode.class);
        Plugin plugin = Mockito.mock(Plugin.class);
        Plugin pluginCopy = Mockito.mock(Plugin.class);

        when(plugin.getDef()).thenReturn(jPlugin);

        when(plugin.getName()).thenReturn("bar");
        when(plugin.getClassName()).thenReturn("baz");
        when(plugin.getTaskGroup()).thenReturn("qux");
        when(plugin.getTaskName()).thenReturn("quux");
        when(plugin.getStepName()).thenReturn("corge");
        when(plugin.getDefJson()).thenReturn("grault");
        when(jPlugin.deepCopy()).thenReturn(jPluginCopy);

        when(objectFactory.createPlugin(plugin.getName(), plugin.getClassName(),
                plugin.getTaskGroup(), plugin.getTaskName(),
                plugin.getStepName(), plugin.getDefJson(), jPlugin.deepCopy()))
                        .thenReturn(pluginCopy);

        Plugin actual = pluginDefs.copy(plugin);

        assertThat(actual).isSameAs(pluginCopy);
    }
}
