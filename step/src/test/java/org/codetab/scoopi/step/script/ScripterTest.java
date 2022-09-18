package org.codetab.scoopi.step.script;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.Plugin;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.plugin.script.ScriptExecutor;
import org.codetab.scoopi.step.mediator.JobMediator;
import org.codetab.scoopi.step.mediator.TaskMediator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ScripterTest {
    @InjectMocks
    private Scripter scripter;

    @Mock
    private IPluginDef pluginDef;
    @Mock
    private ScriptExecutor scriptExecutor;
    @Mock
    private Object input;
    @Mock
    private Configs configs;
    @Mock
    private MetricsHelper metricsHelper;
    @Mock
    private ITaskDef taskDef;
    @Mock
    private TaskMediator taskMediator;
    @Mock
    private JobMediator jobMediator;
    @Mock
    private ObjectFactory factory;
    @Mock
    private Object output;
    @Mock
    private Payload payload;
    @Mock
    private Marker jobMarker;
    @Mock
    private Marker jobAbortedMarker;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testProcessTryIfPluginsIsPresent() throws Exception {
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String orange = "Foo";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String kiwi = "Bar";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String taskGroup = "Baz";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        String taskName = "Qux";
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String cherry = "Quux";
        String stepName = cherry;

        List<Plugin> pluginsList = new ArrayList<>();
        Plugin plugin = Mockito.mock(Plugin.class);
        pluginsList.add(plugin);
        Optional<List<Plugin>> plugins = Optional.of(pluginsList);

        Object scriptOutput = Mockito.mock(Object.class);

        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2);
        when(stepInfo.getStepName()).thenReturn(orange);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo3);
        when(jobInfo.getLabel()).thenReturn(kiwi);
        when(jobInfo2.getGroup()).thenReturn(taskGroup);
        when(jobInfo3.getTask()).thenReturn(taskName);
        when(stepInfo2.getStepName()).thenReturn(cherry);
        when(pluginDef.getPlugins(taskGroup, taskName, stepName))
                .thenReturn(plugins);
        when(scriptExecutor.execute(plugins.get(), input))
                .thenReturn(scriptOutput);

        scripter.process();

        assertSame(scriptOutput, scripter.getOutput());
    }

    @Test
    public void testProcessTryElsePluginsIsPresent() throws Exception {
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String orange = "Foo";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String kiwi = "Bar";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String taskGroup = "Baz";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        String taskName = "Qux";
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String cherry = "Quux";
        String stepName = cherry;
        Optional<List<Plugin>> plugins = Optional.empty();

        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2);
        when(stepInfo.getStepName()).thenReturn(orange);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo3);
        when(jobInfo.getLabel()).thenReturn(kiwi);
        when(jobInfo2.getGroup()).thenReturn(taskGroup);
        when(jobInfo3.getTask()).thenReturn(taskName);
        when(stepInfo2.getStepName()).thenReturn(cherry);
        when(pluginDef.getPlugins(taskGroup, taskName, stepName))
                .thenReturn(plugins);

        scripter.process();

        assertSame(input, scripter.getOutput());

        verifyNoInteractions(scriptExecutor);
    }

    @Test
    public void testProcessTryCatchException() throws Exception {
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String orange = "Foo";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String kiwi = "Bar";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String taskGroup = "Baz";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        String taskName = "Qux";
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String cherry = "Quux";
        String stepName = cherry;
        List<Plugin> pluginsList = new ArrayList<>();
        Plugin plugin = Mockito.mock(Plugin.class);
        pluginsList.add(plugin);
        Optional<List<Plugin>> plugins = Optional.of(pluginsList);


        doThrow(Exception.class).when(scriptExecutor).execute(plugins.get(),
                input);

        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2);
        when(stepInfo.getStepName()).thenReturn(orange);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2).thenReturn(jobInfo3);
        when(jobInfo.getLabel()).thenReturn(kiwi);
        when(jobInfo2.getGroup()).thenReturn(taskGroup);
        when(jobInfo3.getTask()).thenReturn(taskName);
        when(stepInfo2.getStepName()).thenReturn(cherry);
        when(pluginDef.getPlugins(taskGroup, taskName, stepName)).thenReturn(plugins);

        assertThrows(StepRunException.class, () -> scripter.process());

        assertSame(output, scripter.getOutput());
    }
}

