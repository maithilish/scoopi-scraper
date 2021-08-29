package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.util.Maps;
import org.codetab.scoopi.model.StepInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

public class TaskDefTest {

    @InjectMocks
    private TaskDef taskDef;

    @Mock
    private Jacksons jacksons;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Yamls yamls;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TaskDefData data;

    @Before()
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetTaskNames() {
        String taskGroup = "foo";
        List<String> taskNames = Lists.newArrayList("bar");

        when(data.getTaskNamesMap().get(taskGroup)).thenReturn(taskNames);
        List<String> actual = taskDef.getTaskNames(taskGroup);

        assertThat(actual).isEqualTo(taskNames);
        assertThat(actual.getClass().getSimpleName())
                .isEqualTo("UnmodifiableRandomAccessList");
    }

    @Test
    public void testGetFirstTaskName() {
        List<String> taskNames = Lists.newArrayList("bar");
        String taskGroup = "foo";

        when(data.getTaskNamesMap().get(taskGroup)).thenReturn(taskNames);

        Optional<String> actual = taskDef.getFirstTaskName(taskGroup);

        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo("bar");
    }

    @Test
    public void testGetFirstTaskNameNotFound() {
        List<String> taskNames = Lists.newArrayList();
        String taskGroup = "foo";

        when(data.getTaskNamesMap().get(taskGroup)).thenReturn(taskNames)
                .thenReturn(null);

        assertThat(taskDef.getFirstTaskName(taskGroup)).isEmpty();
        assertThat(taskDef.getFirstTaskName(taskGroup)).isEmpty();
    }

    @Test
    public void testGetFieldValue() throws Exception {

        String taskGroup = "foo";
        String taskName = "bar";
        String fieldNames = "baz";
        String fieldValue = "curz";

        ArrayList<String> parts = Lists.newArrayList(taskGroup, taskName);
        Collections.addAll(parts, fieldNames);
        String[] pathParts = new String[parts.size()];
        parts.toArray(pathParts);

        JsonNode defs = Mockito.mock(JsonNode.class);

        when(yamls.toJsonNode(data.getDefsJson())).thenReturn(defs);
        when(jacksons.getFieldValue(defs, pathParts)).thenReturn(fieldValue);

        String actual = taskDef.getFieldValue(taskGroup, taskName, fieldNames);

        assertThat(actual).isEqualTo(fieldValue);

        // field defs already set
        actual = taskDef.getFieldValue(taskGroup, taskName, fieldNames);
        assertThat(actual).isEqualTo(fieldValue);
    }

    @Test
    public void testGetLive() throws Exception {
        String taskGroup = "foo";
        String live = "P1D";
        JsonNode defs = Mockito.mock(JsonNode.class);

        when(yamls.toJsonNode(data.getDefsJson())).thenReturn(defs);
        when(jacksons.getFieldValue(defs, taskGroup, "live")).thenReturn(live);

        String actual = taskDef.getLive(taskGroup);
        assertThat(actual).isEqualTo(live);

        actual = taskDef.getLive(taskGroup); // defs already set
        assertThat(actual).isEqualTo(live);
    }

    @Test
    public void testGetStepsName() throws Exception {
        String taskGroup = "foo";
        String taskName = "bar";
        String key = String.join("-", taskGroup, taskName);
        String stepsName = "baz";

        when(data.getStepsNameMap().get(key)).thenReturn(stepsName);
        String actual = taskDef.getStepsName(taskGroup, taskName);

        assertThat(actual).isEqualTo(stepsName);
    }

    @Test
    public void testGetNextStep() throws Exception {
        String taskGroup = "foo";
        String taskName = "bar";
        String stepName = "baz";
        String key = String.join("-", taskGroup, taskName);

        StepInfo nextStep = Mockito.mock(StepInfo.class);
        Map<String, StepInfo> stepInfoMap = Maps.newHashMap(stepName, nextStep);
        stepInfoMap.put(stepName, nextStep);

        Map<String, Map<String, StepInfo>> nextStepsMap = new HashMap<>();
        nextStepsMap.put(key, stepInfoMap);

        when(data.getNextStepsMap()).thenReturn(nextStepsMap);

        StepInfo actual = taskDef.getNextStep(taskGroup, taskName, stepName);

        assertThat(actual).isEqualTo(nextStep);
    }
}
