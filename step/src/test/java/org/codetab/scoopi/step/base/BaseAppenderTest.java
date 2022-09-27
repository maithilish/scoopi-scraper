package org.codetab.scoopi.step.base;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
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
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.Plugin;
import org.codetab.scoopi.model.PrintPayload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.plugin.appender.Appender;
import org.codetab.scoopi.plugin.appender.Appenders;
import org.codetab.scoopi.plugin.encoder.Encoders;
import org.codetab.scoopi.plugin.encoder.IEncoder;
import org.codetab.scoopi.step.mediator.JobMediator;
import org.codetab.scoopi.step.mediator.TaskMediator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class BaseAppenderTest {

    @InjectMocks
    private TestAppender baseAppender;

    @Mock
    private IPluginDef pluginDef;
    @Mock
    private Appenders appenders;
    @Mock
    private Encoders encoders;
    @Mock
    private Data data;
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

    static class TestAppender extends BaseAppender {
        @Override
        public void process() {

        }
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInitializeIfTryIfPluginsIsPresent() throws Exception {
        Object grape = Mockito.mock(Object.class);
        Data pData = Mockito.mock(Data.class);
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String taskGroup = "Foo";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String taskName = "Bar";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        String stepsName = "Baz";
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String mango = "Qux";
        String stepName = mango;

        Plugin plugin = Mockito.mock(Plugin.class);
        List<Plugin> list = new ArrayList<>();
        list.add(plugin);
        Optional<List<Plugin>> plugins = Optional.of(list);

        when(payload.getData()).thenReturn(grape).thenReturn(pData);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo3);
        when(jobInfo.getGroup()).thenReturn(taskGroup);
        when(jobInfo2.getTask()).thenReturn(taskName);
        when(jobInfo3.getSteps()).thenReturn(stepsName);
        when(payload.getStepInfo()).thenReturn(stepInfo);
        when(stepInfo.getStepName()).thenReturn(mango);
        when(pluginDef.getPlugins(taskGroup, taskName, stepName))
                .thenReturn(plugins);
        baseAppender.initialize();

        verify(appenders).createAppenders(plugins.get(), stepsName, stepName);
        verify(encoders).createEncoders(plugins.get(), stepsName, stepName);
    }

    @Test
    public void testInitializeIfTryElsePluginsIsPresent() throws Exception {
        Object grape = Mockito.mock(Object.class);
        Data pData = Mockito.mock(Data.class);
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String taskGroup = "Foo";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String taskName = "Bar";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        String stepsName = "Baz";
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String mango = "Qux";
        String stepName = mango;
        Optional<List<Plugin>> plugins = Optional.empty();

        when(payload.getData()).thenReturn(grape).thenReturn(pData);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo3);
        when(jobInfo.getGroup()).thenReturn(taskGroup);
        when(jobInfo2.getTask()).thenReturn(taskName);
        when(jobInfo3.getSteps()).thenReturn(stepsName);
        when(payload.getStepInfo()).thenReturn(stepInfo);
        when(stepInfo.getStepName()).thenReturn(mango);
        when(pluginDef.getPlugins(taskGroup, taskName, stepName))
                .thenReturn(plugins);
        baseAppender.initialize();

        verifyNoInteractions(appenders, encoders);
    }

    @Test
    public void testInitializeIfTryCatchException() throws Exception {
        Object grape = Mockito.mock(Object.class);
        Data pData = Mockito.mock(Data.class);
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String taskGroup = "Foo";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String taskName = "Bar";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        String stepsName = "Baz";
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String mango = "Qux";
        String stepName = mango;

        when(payload.getData()).thenReturn(grape).thenReturn(pData);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo3);
        when(jobInfo.getGroup()).thenReturn(taskGroup);
        when(jobInfo2.getTask()).thenReturn(taskName);
        when(jobInfo3.getSteps()).thenReturn(stepsName);
        when(payload.getStepInfo()).thenReturn(stepInfo);
        when(stepInfo.getStepName()).thenReturn(mango);
        when(pluginDef.getPlugins(taskGroup, taskName, stepName))
                .thenThrow(IllegalStateException.class);

        assertThrows(StepRunException.class, () -> baseAppender.initialize());

        verifyNoInteractions(appenders, encoders);
    }

    @Test
    public void testInitializeElseTry() throws Exception {
        Object grape = Mockito.mock(Object.class);
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String taskGroup = "Foo";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String taskName = "Bar";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        String stepsName = "Baz";
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String mango = "Qux";
        String stepName = mango;
        Optional<List<Plugin>> plugins = Optional.empty();

        // grape is not instance of Data
        when(payload.getData()).thenReturn(grape);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo3);
        when(jobInfo.getGroup()).thenReturn(taskGroup);
        when(jobInfo2.getTask()).thenReturn(taskName);
        when(jobInfo3.getSteps()).thenReturn(stepsName);
        when(payload.getStepInfo()).thenReturn(stepInfo);
        when(stepInfo.getStepName()).thenReturn(mango);
        when(pluginDef.getPlugins(taskGroup, taskName, stepName))
                .thenReturn(plugins);

        assertThrows(StepRunException.class, () -> baseAppender.initialize());

        verifyNoInteractions(appenders, encoders);
    }

    @Test
    public void testDoAppend() throws Exception {
        Appender appender = Mockito.mock(Appender.class);
        PrintPayload printPayload = Mockito.mock(PrintPayload.class);
        baseAppender.doAppend(appender, printPayload);

        verify(appender).append(printPayload);
    }

    @Test
    public void testEncode() throws Exception {
        List<IEncoder<?>> encodersList = new ArrayList<>();
        @SuppressWarnings("unchecked")
        IEncoder<String> encoder = Mockito.mock(IEncoder.class);
        String obj = "foo";
        encodersList.add(encoder);

        when(encoder.encode(data)).thenReturn(obj);

        Object actual = baseAppender.encode(encodersList);

        assertSame(obj, actual);
    }

    @Test
    public void testLoad() {
        baseAppender.load();
    }

    @Test
    public void testStore() {
        baseAppender.store();
    }
}
