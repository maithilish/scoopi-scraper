package org.codetab.scoopi.step.load;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.exception.JobRunException;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
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

public class DataAppenderTest {
    @InjectMocks
    private DataAppender dataAppender;

    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private Errors errors;
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

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcessAppend() throws Exception {
        String appenderName = "Foo";
        Set<String> set = new HashSet<>();
        set.add(appenderName);

        Appender appender = Mockito.mock(Appender.class);

        List<IEncoder<?>> encodersList = new ArrayList<>();
        IEncoder<Data> encoder = Mockito.mock(IEncoder.class);
        encodersList.add(encoder);

        Data dataObj = Mockito.mock(Data.class);
        Object encodedData = dataObj;
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        PrintPayload printPayload = Mockito.mock(PrintPayload.class);
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String orange = "Bar";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String kiwi = "Baz";
        boolean cherry = true;

        when(appenders.keySet()).thenReturn(set);
        when(appenders.get(appenderName)).thenReturn(appender);
        when(encoders.get(appenderName)).thenReturn(encodersList);
        when(encoder.encode(data)).thenReturn(dataObj);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2);
        when(objectFactory.createPrintPayload(jobInfo, encodedData))
                .thenReturn(printPayload);
        when(payload.getStepInfo()).thenReturn(stepInfo);
        when(stepInfo.getStepName()).thenReturn(orange);
        when(jobInfo2.getLabel()).thenReturn(kiwi);
        when(printPayload.isFinished()).thenReturn(cherry);

        dataAppender.process();
        assertSame(data, dataAppender.getOutput());

        verify(appender).append(printPayload);
        verify(errors, never()).inc();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcessAppendException() throws Exception {
        String appenderName = "Foo";
        Set<String> set = new HashSet<>();
        set.add(appenderName);

        Appender appender = Mockito.mock(Appender.class);

        List<IEncoder<?>> encodersList = new ArrayList<>();
        IEncoder<Data> encoder = Mockito.mock(IEncoder.class);
        encodersList.add(encoder);

        Data dataObj = Mockito.mock(Data.class);
        Object encodedData = dataObj;
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        PrintPayload printPayload = Mockito.mock(PrintPayload.class);
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String orange = "Bar";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String kiwi = "Baz";
        boolean cherry = true;

        when(appenders.keySet()).thenReturn(set);
        when(appenders.get(appenderName)).thenReturn(appender);
        when(encoders.get(appenderName)).thenReturn(encodersList);
        when(encoder.encode(data)).thenReturn(dataObj);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2);
        when(objectFactory.createPrintPayload(jobInfo, encodedData))
                .thenReturn(printPayload);
        when(payload.getStepInfo()).thenReturn(stepInfo);
        when(stepInfo.getStepName()).thenReturn(orange);
        when(jobInfo2.getLabel()).thenReturn(kiwi);
        when(printPayload.isFinished()).thenReturn(cherry);

        doThrow(InterruptedException.class).when(appender).append(printPayload);

        dataAppender.process();

        assertSame(data, dataAppender.getOutput());

        verify(errors, times(1)).inc();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcessAppendPrintPayloadNotFinished() throws Exception {
        String appenderName = "Foo";
        Set<String> set = new HashSet<>();
        set.add(appenderName);

        Appender appender = Mockito.mock(Appender.class);

        List<IEncoder<?>> encodersList = new ArrayList<>();
        IEncoder<Data> encoder = Mockito.mock(IEncoder.class);
        encodersList.add(encoder);

        Data dataObj = Mockito.mock(Data.class);
        Object encodedData = dataObj;
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        PrintPayload printPayload = Mockito.mock(PrintPayload.class);
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String orange = "Bar";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String kiwi = "Baz";
        boolean cherry = false; // not finished

        when(appenders.keySet()).thenReturn(set);
        when(appenders.get(appenderName)).thenReturn(appender);
        when(encoders.get(appenderName)).thenReturn(encodersList);
        when(encoder.encode(data)).thenReturn(dataObj);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2);
        when(objectFactory.createPrintPayload(jobInfo, encodedData))
                .thenReturn(printPayload);
        when(payload.getStepInfo()).thenReturn(stepInfo);
        when(stepInfo.getStepName()).thenReturn(orange);
        when(jobInfo2.getLabel()).thenReturn(kiwi);
        when(printPayload.isFinished()).thenReturn(cherry);

        assertThrows(JobRunException.class, () -> dataAppender.process());

        assertSame(output, dataAppender.getOutput()); // unchanged

        verify(appender).append(printPayload);
        verify(errors, never()).inc();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcessAppendPrintPayloadFinishedException()
            throws Exception {
        String appenderName = "Foo";
        Set<String> set = new HashSet<>();
        set.add(appenderName);

        Appender appender = Mockito.mock(Appender.class);

        List<IEncoder<?>> encodersList = new ArrayList<>();
        IEncoder<Data> encoder = Mockito.mock(IEncoder.class);
        encodersList.add(encoder);

        Data dataObj = Mockito.mock(Data.class);
        Object encodedData = dataObj;
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        PrintPayload printPayload = Mockito.mock(PrintPayload.class);
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String orange = "Bar";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String kiwi = "Baz";

        when(appenders.keySet()).thenReturn(set);
        when(appenders.get(appenderName)).thenReturn(appender);
        when(encoders.get(appenderName)).thenReturn(encodersList);
        when(encoder.encode(data)).thenReturn(dataObj);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2);
        when(objectFactory.createPrintPayload(jobInfo, encodedData))
                .thenReturn(printPayload);
        when(payload.getStepInfo()).thenReturn(stepInfo);
        when(stepInfo.getStepName()).thenReturn(orange);
        when(jobInfo2.getLabel()).thenReturn(kiwi);

        when(printPayload.isFinished()).thenThrow(InterruptedException.class);

        assertThrows(JobRunException.class, () -> dataAppender.process());

        assertSame(output, dataAppender.getOutput()); // unchanged
        verify(appender).append(printPayload);
        verify(errors, never()).inc();
    }
}
