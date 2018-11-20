package org.codetab.scoopi.defs.mig.yml.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.InvalidDefException;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Plugin;
import org.codetab.scoopi.util.TestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class PluginHelperTest {

    @Mock
    private Yamls yamls;
    @Spy
    private ObjectFactory objectFactory;

    @InjectMocks
    private PluginDefs pluginDefs;

    private static ObjectFactory factory;
    private static ObjectMapper objectMapper;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass() {
        factory = new ObjectFactory();
        objectMapper = new ObjectMapper();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetPluginField() throws IOException, DefNotFoundException {
        Plugin plugin = getTestPlugin();

        String actual = pluginDefs.getPluginField(plugin, "inPattern");
        assertThat(actual).isEqualTo("MMM ''YY");

        actual = pluginDefs.getPluginField(plugin, "outPattern");
        assertThat(actual).isEqualTo("yyyy-MM-dd");

        actual = pluginDefs.getPluginField(plugin, "roll");
        assertThat(actual).isEqualTo("DAY_OF_MONTH=ceil");
    }

    @Test
    public void testGetPlugins()
            throws DefNotFoundException, InvalidDefException, IOException {
        Entry<String, JsonNode> steps = getTestSteps();

        String taskGroup = "bs";
        String taskName = "bsTask";
        String stepName = steps.getKey();

        String plugin1Json = TestUtils.parseJson("{ name: x, class: xclz }");
        String plugin2Json = TestUtils.parseJson("{ name: y, class: yclz }");

        Plugin plugin1 = getTestPlugin("x", "xclz", taskGroup, taskName,
                stepName, plugin1Json);
        Plugin plugin2 = getTestPlugin("y", "yclz", taskGroup, taskName,
                stepName, plugin2Json);

        given(yamls.toJson(eq((JsonNode) plugin1.getDef())))
                .willReturn(plugin1Json);
        given(yamls.toJson(eq((JsonNode) plugin2.getDef())))
                .willReturn(plugin2Json);

        Optional<List<Plugin>> actual = pluginDefs.getPlugins(steps, taskGroup,
                taskName, steps.getKey());

        assertThat(actual).isPresent();
        assertThat(actual.get().size()).isEqualTo(2);

        Plugin actualPlugin1 = actual.get().get(0);
        Plugin actualPlugin2 = actual.get().get(1);

        assertThat(actualPlugin1).isEqualTo(plugin1);
        assertThat(actualPlugin2).isEqualTo(plugin2);
    }

    @Test
    public void testGetPluginsNoPluginDefined()
            throws DefNotFoundException, InvalidDefException, IOException {
        Entry<String, JsonNode> steps = getTestSteps();

        // replace with steps without plugin
        JsonNode stepsNode =
                objectMapper.readTree(TestUtils.parseJson("{ stepX: {}}"));
        steps.setValue(stepsNode);

        String taskGroup = "bs";
        String taskName = "bsTask";

        Optional<List<Plugin>> actual = pluginDefs.getPlugins(steps, taskGroup,
                taskName, steps.getKey());

        assertThat(actual).isNotPresent();
    }

    @Test
    public void testGetPluginsShouldThrowException()
            throws DefNotFoundException, InvalidDefException, IOException {
        Entry<String, JsonNode> steps = getTestSteps();

        String taskGroup = "bs";
        String taskName = "bsTask";
        String stepName = steps.getKey();

        String plugin1Json = TestUtils.parseJson("{ name: x, class: xclz }");

        Plugin plugin1 = getTestPlugin("x", "xclz", taskGroup, taskName,
                stepName, plugin1Json);

        given(yamls.toJson(eq((JsonNode) plugin1.getDef())))
                .willThrow(JsonProcessingException.class);

        testRule.expect(InvalidDefException.class);
        pluginDefs.getPlugins(steps, taskGroup, taskName, steps.getKey());
    }

    @Test
    public void testGetPluginFieldShouldThrowException()
            throws IOException, DefNotFoundException {
        Plugin plugin = getTestEmptyPlugin();

        testRule.expect(DefNotFoundException.class);
        pluginDefs.getPluginField(plugin, "inPattern");
    }

    private Plugin getTestPlugin() throws IOException {
        String defJson = new StringBuilder().append("{ name: converter,")
                .append("class: org.codetab.gotz.step.converter.DateRoller,")
                .append("axis: col,").append("inPattern: \"MMM ''YY\",")
                .append("outPattern: yyyy-MM-dd,")
                .append("roll: \"DAY_OF_MONTH=ceil\" } ").toString();

        JsonNode def = objectMapper.readTree(TestUtils.parseJson(defJson));
        return factory.createPlugin("pluginName", "pluginClass", "bs", "bsTask",
                "converter", defJson, def);
    }

    private Plugin getTestEmptyPlugin() throws IOException {
        String defJson = new StringBuilder().append("{ }").toString();
        JsonNode def = objectMapper.readTree(TestUtils.parseJson(defJson));
        return factory.createPlugin("pluginName", "pluginClass", "bs", "bsTask",
                "converter", defJson, def);
    }

    private Entry<String, JsonNode> getTestSteps() throws IOException {
        String defJson =
                new StringBuilder().append("{ stepX: {").append("plugins: [ ")
                        .append("{plugin: { name: x, class: xclz }}, ")
                        .append("{plugin: { name: y, class: yclz }} ")
                        .append("] } }").toString();

        JsonNode steps = objectMapper.readTree(TestUtils.parseJson(defJson));
        Map<String, JsonNode> map = new HashMap<>();
        map.put("stepX", steps);
        return Lists.newArrayList(map.entrySet()).get(0);
    }

    private Plugin getTestPlugin(final String name, final String clz,
            final String taskGroup, final String taskName,
            final String stepName, final String json) throws IOException {
        JsonNode def = objectMapper.readTree(json);
        return factory.createPlugin(name, clz, taskGroup, taskName, stepName,
                json, def);
    }
}
