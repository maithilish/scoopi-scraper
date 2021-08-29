package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.JsonNode;

public class TaskDefBuilderTest {

    @InjectMocks
    private TaskDefBuilder taskDefBuilder;

    @Mock
    private TaskDefs taskDefs;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private StepDefs stepDefs;

    @Before()
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @SuppressWarnings({"unused", "rawtypes", "unchecked"})
    @Test
    public void testBuildData() throws Exception {

        Map<String, JsonNode> allTasks = Mockito.mock(Map.class);
        JsonNode defs = Mockito.mock(JsonNode.class);

        Map taskNamesMap = Mockito.mock(Map.class);
        Map stepsNameMap = Mockito.mock(Map.class);
        Map taskStepsMap = Mockito.mock(Map.class);
        Map stepsMap = Mockito.mock(Map.class);
        Map nextStepsMap = Mockito.mock(Map.class);

        String defsJson = "foo";

        when(defs.toString()).thenReturn(defsJson);

        when(taskDefs.getTaskNamesMap(defs)).thenReturn(taskNamesMap);
        when(taskDefs.getAllTasks(defs)).thenReturn(allTasks);
        when(stepDefs.getTaskStepsNameMap(allTasks)).thenReturn(stepsNameMap);

        when(stepDefs.getStepsMap(defs, taskNamesMap)).thenReturn(stepsMap);
        when(stepDefs.getNextStepsMap(stepsMap)).thenReturn(nextStepsMap);

        TaskDefData actual = (TaskDefData) taskDefBuilder.buildData(defs);

        assertThat(actual.getDefsJson()).isSameAs(defsJson);
        assertThat(actual.getStepsNameMap()).isSameAs(stepsNameMap);
        assertThat(actual.getTaskNamesMap()).isSameAs(taskNamesMap);
        assertThat(actual.getNextStepsMap()).isSameAs(nextStepsMap);
        assertThat(actual.getStepsMap()).isSameAs(stepsMap);
    }

}
