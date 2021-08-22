package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codetab.scoopi.model.Plugin;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.JsonNode;

public class PluginDefBuilderTest {

    @InjectMocks
    private PluginDefBuilder pluginDefBuilder;

    @Mock
    private PluginDefs pluginDefs;

    @Mock
    private TaskDefs taskDefs;

    @Mock
    private StepDefs stepDefs;

    @Before()
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testBuildData() throws Exception {
        Map<String, String> stepsNameMap = new HashMap<>();
        Map<String, List<Plugin>> pluginMap = new HashMap<>();
        Map<String, JsonNode> allTasks = new HashMap<>();
        Map<String, JsonNode> stepsMap = new HashMap<>();
        JsonNode node = Mockito.mock(JsonNode.class);
        JsonNode defs = Mockito.mock(JsonNode.class);

        when(taskDefs.getAllTasks(node)).thenReturn(allTasks);
        when(stepDefs.getTaskStepsNameMap(allTasks)).thenReturn(stepsNameMap);
        when(stepDefs.getStepNodeMap(allTasks, stepsNameMap))
                .thenReturn(stepsMap);
        when(pluginDefs.getPluginMap(stepsMap)).thenReturn(pluginMap);

        PluginDefData actual = (PluginDefData) pluginDefBuilder.buildData(defs);

        assertThat(actual.getPluginMap()).isSameAs(pluginMap);
    }
}
