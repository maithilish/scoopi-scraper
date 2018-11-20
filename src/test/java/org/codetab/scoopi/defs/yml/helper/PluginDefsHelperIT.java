package org.codetab.scoopi.defs.yml.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.codetab.scoopi.defs.yml.YamlLoader;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.InvalidDefException;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Plugin;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public class PluginDefsHelperIT {

    private static PluginDefs pluginDefs;
    private static Yamls yamls;
    private static DInjector di;
    private static JsonNode taskDefs;
    private static JsonNode testYamlNode;

    private ObjectFactory factory;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass()
            throws IOException, DefNotFoundException {
        testYamlNode = YamlLoader
                .load("/testdefs/yml/helper/plugin-defs-helper-it.yml");
        taskDefs = testYamlNode.at("/taskGroups");
        di = new DInjector();
    }

    @Before
    public void setUp() throws Exception {
        pluginDefs = di.instance(PluginDefs.class);
        yamls = di.instance(Yamls.class);
        factory = di.instance(ObjectFactory.class);

    }

    @Test
    public void testGetTaskStepsMap()
            throws DefNotFoundException, InvalidDefException {
        JsonNode expected1 =
                taskDefs.at("/taskGroup1/task1/steps/steps11/step11");
        JsonNode expected2 =
                taskDefs.at("/taskGroup1/task1/steps/steps11/step12");
        JsonNode expected3 =
                taskDefs.at("/taskGroup1/task2/steps/steps21/step21");
        JsonNode expected4 =
                taskDefs.at("/taskGroup2/task3/steps/steps31/step31");

        String key1 = "taskGroup1-task1-step11";
        String key2 = "taskGroup1-task1-step12";
        String key3 = "taskGroup1-task2-step21";
        String key4 = "taskGroup2-task3-step31";

        Map<String, JsonNode> actual = pluginDefs.getTaskStepsMap(taskDefs);

        assertThat(actual.size()).isEqualTo(4);
        assertThat(actual.get(key1)).isEqualTo(expected1);
        assertThat(actual.get(key2)).isEqualTo(expected2);
        assertThat(actual.get(key3)).isEqualTo(expected3);
        assertThat(actual.get(key4)).isEqualTo(expected4);
    }

    @Test
    public void testGetTaskStepsMapShouldThrowException()
            throws DefNotFoundException, InvalidDefException {
        JsonNode invalidDef = testYamlNode.at("/taskGroupsMultipleSteps");
        testRule.expect(IllegalStateException.class);
        pluginDefs.getTaskStepsMap(invalidDef);
    }

    @Test
    public void testGetPluginMap() throws DefNotFoundException,
            InvalidDefException, JsonProcessingException {

        Map<String, JsonNode> stepsMap = pluginDefs.getTaskStepsMap(taskDefs);
        Map<String, Optional<List<Plugin>>> actual =
                pluginDefs.getPluginMap(stepsMap);

        assertThat(actual.size()).isEqualTo(4);

        String key = "taskGroup1-task1-step12";
        JsonNode jplugins =
                taskDefs.at("/taskGroup1/task1/steps/steps11/step12/plugins");
        JsonNode jPlugin = jplugins.findValues("plugin").get(0);
        Plugin expected =
                factory.createPlugin("plugin11", "plugin11 class", "taskGroup1",
                        "task1", "step12", yamls.toJson(jPlugin), jPlugin);
        assertThat(actual.get(key).get().get(0)).isEqualTo(expected);

        key = "taskGroup1-task2-step21";
        assertThat(actual.get(key)).isNotPresent();

        key = "taskGroup2-task3-step31";
        jplugins =
                taskDefs.at("/taskGroup2/task3/steps/steps31/step31/plugins");
        jPlugin = jplugins.findValues("plugin").get(0);
        expected =
                factory.createPlugin("plugin31", "plugin31 class", "taskGroup2",
                        "task3", "step31", yamls.toJson(jPlugin), jPlugin);

        jPlugin = jplugins.findValues("plugin").get(1);
        Plugin expected2 =
                factory.createPlugin("plugin32", "plugin32 class", "taskGroup2",
                        "task3", "step31", yamls.toJson(jPlugin), jPlugin);
        assertThat(actual.get(key).get()).contains(expected, expected2);

    }

}
