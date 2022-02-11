package org.codetab.scoopi.plugin.appender;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.helper.IOHelper;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.Plugin;
import org.codetab.scoopi.model.PrintPayload;
import org.codetab.scoopi.plugin.appender.Appender.Marker;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class FileAppenderTest {
    @InjectMocks
    private FileAppender fileAppender;

    @Mock
    private IOHelper ioHelper;
    @Mock
    private Configs configs;
    @Mock
    private JobFilePath jobFilePath;
    @Mock
    private IPluginDef pluginDef;
    @Mock
    private Errors errors;
    @Mock
    private BlockingQueue<PrintPayload> queue;
    @Mock
    private Plugin plugin;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInit() throws DefNotFoundException {
        String field = "file";
        String apple = "Bar";
        String baseDir = "Baz";
        String banana = "Qux";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(banana);
        ZonedDateTime cherry = Mockito.mock(ZonedDateTime.class);
        String dirTimestamp = "Quux";

        when(pluginDef.getValue(plugin, field)).thenReturn(apple);
        when(configs.getConfig("scoopi.appender.file.baseDir", ""))
                .thenReturn(baseDir);
        when(configs.getConfig("outputDirTimestampPattern", "yyyyMMMdd-HHmmss"))
                .thenReturn(banana);
        when(configs.getRunDateTime()).thenReturn(cherry);
        when(cherry.format(formatter)).thenReturn(dirTimestamp);
        fileAppender.init();

        verify(errors, never()).inc();
    }

    @Test
    public void testInitDefNotFound() throws DefNotFoundException {
        String field = "file";
        String baseDir = "Baz";
        String banana = "Qux";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(banana);
        ZonedDateTime cherry = Mockito.mock(ZonedDateTime.class);
        String dirTimestamp = "Quux";

        when(pluginDef.getValue(plugin, field))
                .thenThrow(DefNotFoundException.class);
        when(configs.getConfig("scoopi.appender.file.baseDir", ""))
                .thenReturn(baseDir);
        when(configs.getConfig("outputDirTimestampPattern", "yyyyMMMdd-HHmmss"))
                .thenReturn(banana);
        when(configs.getRunDateTime()).thenReturn(cherry);
        when(cherry.format(formatter)).thenReturn(dirTimestamp);
        fileAppender.init();

        verify(errors).inc();
    }

    @Test
    public void testRun() throws Exception {
        PrintPayload printPayload = Mockito.mock(PrintPayload.class);
        PrintPayload eosPrintPayload = Mockito.mock(PrintPayload.class);
        JobInfo banana = Mockito.mock(JobInfo.class);
        long cherry = 1L;
        PrintWriter writer = Mockito.mock(PrintWriter.class);
        Object data = Mockito.mock(Object.class);
        Object o = Mockito.mock(Object.class);
        String apricot = "Bar";

        String baseDir = "scoopi";
        String filePath = "output/data.txt";
        String fileDir = "output/";
        String fileBaseName = "data";
        String fileExtension = "txt";
        String dirTimestamp = "2020Jan10-022030";
        ZonedDateTime runDateTime = ZonedDateTime.of(2020, 01, 10, 2, 20, 30, 0,
                ZoneId.of("Asia/Kolkata"));
        String dataFilePath = "scoopi/output//2020Jan10-022030/data-1.txt";

        when(configs.getConfig("outputDirTimestampPattern", "yyyyMMMdd-HHmmss"))
                .thenReturn("yyyyMMMdd-HHmmss");
        when(configs.getConfig("scoopi.appender.file.baseDir", ""))
                .thenReturn(baseDir);
        when(configs.getRunDateTime()).thenReturn(runDateTime);
        when(pluginDef.getValue(plugin, "file")).thenReturn(filePath);
        when(queue.take()).thenReturn(printPayload).thenReturn(eosPrintPayload);
        when(printPayload.getData()).thenReturn(data);
        when(eosPrintPayload.getData()).thenReturn(Marker.END_OF_STREAM);
        when(printPayload.getJobInfo()).thenReturn(banana);
        when(banana.getId()).thenReturn(cherry);
        when(jobFilePath.getPath(baseDir, fileDir, fileBaseName, fileExtension,
                dirTimestamp, cherry)).thenReturn(dataFilePath);
        when(ioHelper.getPrintWriter(dataFilePath)).thenReturn(writer);
        when(o.toString()).thenReturn(apricot);

        fileAppender.init();
        fileAppender.run();

        verify(errors, never()).inc();
        verify(writer).println(data);
        verify(printPayload).setProcessed(true);
        verify(printPayload).finished();
    }

    @Test
    public void testRunInterrupted() throws Exception {
        PrintPayload printPayload = Mockito.mock(PrintPayload.class);
        PrintPayload eosPrintPayload = Mockito.mock(PrintPayload.class);
        JobInfo banana = Mockito.mock(JobInfo.class);
        long cherry = 1L;
        PrintWriter writer = Mockito.mock(PrintWriter.class);
        Object data = Mockito.mock(Object.class);
        Object o = Mockito.mock(Object.class);
        String apricot = "Bar";

        String baseDir = "scoopi";
        String filePath = "output/data.txt";
        String fileDir = "output/";
        String fileBaseName = "data";
        String fileExtension = "txt";
        String dirTimestamp = "2020Jan10-022030";
        ZonedDateTime runDateTime = ZonedDateTime.of(2020, 01, 10, 2, 20, 30, 0,
                ZoneId.of("Asia/Kolkata"));
        String dataFilePath = "scoopi/output//2020Jan10-022030/data-1.txt";

        when(configs.getConfig("outputDirTimestampPattern", "yyyyMMMdd-HHmmss"))
                .thenReturn("yyyyMMMdd-HHmmss");
        when(configs.getConfig("scoopi.appender.file.baseDir", ""))
                .thenReturn(baseDir);
        when(configs.getRunDateTime()).thenReturn(runDateTime);
        when(pluginDef.getValue(plugin, "file")).thenReturn(filePath);
        when(queue.take()).thenReturn(printPayload)
                .thenThrow(InterruptedException.class)
                .thenReturn(eosPrintPayload);
        when(printPayload.getData()).thenReturn(data);
        when(eosPrintPayload.getData()).thenReturn(Marker.END_OF_STREAM);
        when(printPayload.getJobInfo()).thenReturn(banana);
        when(banana.getId()).thenReturn(cherry);
        when(jobFilePath.getPath(baseDir, fileDir, fileBaseName, fileExtension,
                dirTimestamp, cherry)).thenReturn(dataFilePath);
        when(ioHelper.getPrintWriter(dataFilePath)).thenReturn(writer);
        when(o.toString()).thenReturn(apricot);

        fileAppender.init();
        fileAppender.run();

        verify(errors).inc();
        verify(writer).println(data);
        verify(printPayload).setProcessed(true);
        verify(printPayload).finished();
    }

    @Test
    public void testRunWriteListData() throws Exception {
        PrintPayload printPayload = Mockito.mock(PrintPayload.class);
        PrintPayload eosPrintPayload = Mockito.mock(PrintPayload.class);
        JobInfo banana = Mockito.mock(JobInfo.class);
        long cherry = 1L;
        PrintWriter writer = Mockito.mock(PrintWriter.class);
        List<String> data = new ArrayList<>();
        String dataLine = "foo";
        data.add(dataLine);
        Object o = Mockito.mock(Object.class);
        String apricot = "Bar";

        String baseDir = "scoopi";
        String filePath = "output/data.txt";
        String fileDir = "output/";
        String fileBaseName = "data";
        String fileExtension = "txt";
        String dirTimestamp = "2020Jan10-022030";
        ZonedDateTime runDateTime = ZonedDateTime.of(2020, 01, 10, 2, 20, 30, 0,
                ZoneId.of("Asia/Kolkata"));
        String dataFilePath = "scoopi/output//2020Jan10-022030/data-1.txt";

        when(configs.getConfig("outputDirTimestampPattern", "yyyyMMMdd-HHmmss"))
                .thenReturn("yyyyMMMdd-HHmmss");
        when(configs.getConfig("scoopi.appender.file.baseDir", ""))
                .thenReturn(baseDir);
        when(configs.getRunDateTime()).thenReturn(runDateTime);
        when(pluginDef.getValue(plugin, "file")).thenReturn(filePath);
        when(queue.take()).thenReturn(printPayload).thenReturn(eosPrintPayload);
        when(printPayload.getData()).thenReturn(data);
        when(eosPrintPayload.getData()).thenReturn(Marker.END_OF_STREAM);
        when(printPayload.getJobInfo()).thenReturn(banana);
        when(banana.getId()).thenReturn(cherry);
        when(jobFilePath.getPath(baseDir, fileDir, fileBaseName, fileExtension,
                dirTimestamp, cherry)).thenReturn(dataFilePath);
        when(ioHelper.getPrintWriter(dataFilePath)).thenReturn(writer);
        when(o.toString()).thenReturn(apricot);

        fileAppender.init();
        fileAppender.run();

        verify(errors, never()).inc();
        verify(writer).println(dataLine);
        verify(printPayload).setProcessed(true);
        verify(printPayload).finished();
    }

    @Test
    public void testRunIOException() throws Exception {
        PrintPayload printPayload = Mockito.mock(PrintPayload.class);
        PrintPayload eosPrintPayload = Mockito.mock(PrintPayload.class);
        JobInfo banana = Mockito.mock(JobInfo.class);
        long cherry = 1L;
        PrintWriter writer = Mockito.mock(PrintWriter.class);
        Object data = Mockito.mock(Object.class);
        Object o = Mockito.mock(Object.class);
        String apricot = "Bar";

        String baseDir = "scoopi";
        String filePath = "output/data.txt";
        String fileDir = "output/";
        String fileBaseName = "data";
        String fileExtension = "txt";
        String dirTimestamp = "2020Jan10-022030";
        ZonedDateTime runDateTime = ZonedDateTime.of(2020, 01, 10, 2, 20, 30, 0,
                ZoneId.of("Asia/Kolkata"));
        String dataFilePath = "scoopi/output//2020Jan10-022030/data-1.txt";

        when(configs.getConfig("outputDirTimestampPattern", "yyyyMMMdd-HHmmss"))
                .thenReturn("yyyyMMMdd-HHmmss");
        when(configs.getConfig("scoopi.appender.file.baseDir", ""))
                .thenReturn(baseDir);
        when(configs.getRunDateTime()).thenReturn(runDateTime);
        when(pluginDef.getValue(plugin, "file")).thenReturn(filePath);
        when(queue.take()).thenReturn(printPayload).thenReturn(eosPrintPayload);
        when(printPayload.getData()).thenReturn(data);
        when(eosPrintPayload.getData()).thenReturn(Marker.END_OF_STREAM);
        when(printPayload.getJobInfo()).thenReturn(banana);
        when(banana.getId()).thenReturn(cherry);
        when(jobFilePath.getPath(baseDir, fileDir, fileBaseName, fileExtension,
                dirTimestamp, cherry)).thenReturn(dataFilePath);
        when(ioHelper.getPrintWriter(dataFilePath)).thenReturn(writer);
        when(o.toString()).thenReturn(apricot);
        doThrow(IOException.class).when(ioHelper).getPrintWriter(dataFilePath);

        fileAppender.init();
        fileAppender.run();

        verify(errors, never()).inc();
        verify(printPayload).setProcessed(false);
        verify(printPayload).finished();
    }

    @Test
    public void testAppend() throws Exception {
        PrintPayload printPayload = Mockito.mock(PrintPayload.class);
        boolean initialized = true;
        fileAppender.setInitialized(initialized);

        fileAppender.append(printPayload);

        verify(queue).put(printPayload);
    }

    @Test
    public void testInitializeQueue() throws DefNotFoundException {
        String configKey = "scoopi.appender.queueSize";
        int defaultValue = 4096;
        int qSize = 4096;
        String qSizeInPlugin = "8192";

        when(configs.getInt(configKey, defaultValue)).thenReturn(qSize);
        when(pluginDef.getValue(plugin, "queueSize")).thenReturn(qSizeInPlugin);
        fileAppender.initializeQueue();

        BlockingQueue<PrintPayload> q = fileAppender.getQueue();
        assertEquals(Integer.parseInt(qSizeInPlugin), q.remainingCapacity());
    }

    @Test
    public void testInitializeQueueDefNotFoundException()
            throws DefNotFoundException {
        String configKey = "scoopi.appender.queueSize";
        int defaultValue = 4096;
        int qSize = 2048;

        when(configs.getInt(configKey, defaultValue)).thenReturn(qSize);
        when(pluginDef.getValue(plugin, "queueSize"))
                .thenThrow(DefNotFoundException.class);
        fileAppender.initializeQueue();

        BlockingQueue<PrintPayload> q = fileAppender.getQueue();
        assertEquals(qSize, q.remainingCapacity());
    }

    @Test
    public void testInitializeQueueInvalidSize() throws DefNotFoundException {
        String configKey = "scoopi.appender.queueSize";
        int defaultValue = 4096;
        int qSize = 2048;
        String qSizeInPlugin = "invalid-size";

        when(configs.getInt(configKey, defaultValue)).thenReturn(qSize);
        when(pluginDef.getValue(plugin, "queueSize")).thenReturn(qSizeInPlugin);
        fileAppender.initializeQueue();

        BlockingQueue<PrintPayload> q = fileAppender.getQueue();
        assertEquals(qSize, q.remainingCapacity());
    }

    @Test
    public void testSetName() {
        String appenderName = "foo";
        fileAppender.setName(appenderName);
        assertEquals(appenderName, fileAppender.getName());
    }
}
