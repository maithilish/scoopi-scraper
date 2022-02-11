package org.codetab.scoopi.plugin.appender;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.model.Plugin;
import org.codetab.scoopi.model.PrintPayload;
import org.codetab.scoopi.plugin.appender.Appender.Marker;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ListAppenderTest {
    @InjectMocks
    private ListAppender listAppender;

    @Mock
    private Configs configs;
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
    public void testInit() {
        listAppender.init();
        assertTrue(listAppender.isInitialized());
    }

    @Test
    public void testRun() throws InterruptedException {
        PrintPayload printPayload = Mockito.mock(PrintPayload.class);
        PrintPayload eosPrintPayload = Mockito.mock(PrintPayload.class);
        Object orange = Mockito.mock(Object.class);

        when(queue.take()).thenReturn(printPayload).thenReturn(eosPrintPayload);
        when(printPayload.getData()).thenReturn(orange);
        when(eosPrintPayload.getData()).thenReturn(Marker.END_OF_STREAM);
        listAppender.run();
        List<Object> list = listAppender.getList();

        assertEquals(1, list.size());
        assertSame(orange, list.get(0));

        verify(errors, never()).inc();
    }

    @Test
    public void testRunInterrupted() throws InterruptedException {
        PrintPayload printPayload = Mockito.mock(PrintPayload.class);
        PrintPayload eosPrintPayload = Mockito.mock(PrintPayload.class);
        Object orange = Mockito.mock(Object.class);

        when(queue.take()).thenReturn(printPayload)
                .thenThrow(InterruptedException.class)
                .thenReturn(eosPrintPayload);
        when(printPayload.getData()).thenReturn(orange);
        when(eosPrintPayload.getData()).thenReturn(Marker.END_OF_STREAM);
        listAppender.run();
        List<Object> list = listAppender.getList();

        assertEquals(1, list.size());
        assertSame(orange, list.get(0));

        verify(errors).inc();
    }

    @Test
    public void testAppend() throws Exception {
        PrintPayload printPayload = Mockito.mock(PrintPayload.class);
        boolean initialized = true;
        listAppender.setInitialized(initialized);

        listAppender.append(printPayload);

        verify(queue).put(printPayload);
    }

    @Test
    public void testAppendUnintialized() throws Exception {
        PrintPayload printPayload = Mockito.mock(PrintPayload.class);
        boolean initialized = false;
        listAppender.setInitialized(initialized);

        listAppender.append(printPayload);

        verify(queue, never()).put(printPayload);
    }

    @Test
    public void testGetList() {
        List<Object> list = new ArrayList<>();

        List<Object> actual = listAppender.getList();

        assertEquals(list, actual);
    }
}
