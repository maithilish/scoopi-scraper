package org.codetab.scoopi.step.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.helper.AnalyzerConsole;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.step.mediator.JobMediator;
import org.codetab.scoopi.step.mediator.TaskMediator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class BaseQueryAnalyzerTest {
    @InjectMocks
    private TestBaseQueryAnalyzer baseQueryAnalyzer;

    @Mock
    private Document document;
    @Mock
    private AnalyzerConsole analyzerConsole;
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

    static class TestBaseQueryAnalyzer extends BaseQueryAnalyzer {

        private String status;

        @Override
        protected boolean postInitialize() {
            status = "foo";
            return true;
        }

        @Override
        protected List<String> getQueryElements(final String query) {
            List<String> list = new ArrayList<>();
            list.add("query result");
            return list;
        }

        @Override
        protected String getPageSource() {
            return "Dummy Page";
        }

        public String getStatus() {
            return status;
        }
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInitializeIf() throws Exception {
        Object grape = Mockito.mock(Object.class);
        Document pData = Mockito.mock(Document.class);

        when(payload.getData()).thenReturn(grape).thenReturn(pData);
        baseQueryAnalyzer.initialize();

        assertEquals("foo", baseQueryAnalyzer.getStatus());
        assertEquals(pData,
                FieldUtils.readField(baseQueryAnalyzer, "document", true));
    }

    @Test
    public void testInitializeElse() throws Exception {
        Object grape = Mockito.mock(Object.class);

        // not instance of Document
        when(payload.getData()).thenReturn(grape);

        assertThrows(StepRunException.class,
                () -> baseQueryAnalyzer.initialize());

        assertNull(baseQueryAnalyzer.getStatus());
        assertEquals(document,
                FieldUtils.readField(baseQueryAnalyzer, "document", true));
    }

    @Test
    public void testProcessIfStringUtilsIsBlankOption1() {
        String input = "1";
        String blank = "";
        String pageSource = "Dummy Page";
        List<String> elements = new ArrayList<>();

        when(analyzerConsole.getInput(System.in)).thenReturn(input)
                .thenReturn(blank);
        baseQueryAnalyzer.process();

        verify(analyzerConsole).showPageSource(pageSource);
        verify(analyzerConsole, never()).writePageSource(pageSource);
        verify(analyzerConsole, never()).showElements(elements);
    }

    @Test
    public void testProcessIfStringUtilsIsBlankOption2() {
        String input = "2";
        String blank = "";
        String pageSource = "Dummy Page";
        List<String> elements = new ArrayList<>();

        when(analyzerConsole.getInput(System.in)).thenReturn(input)
                .thenReturn(blank);
        baseQueryAnalyzer.process();

        verify(analyzerConsole, never()).showPageSource(pageSource);
        verify(analyzerConsole).writePageSource(pageSource);
        verify(analyzerConsole, never()).showElements(elements);
    }

    @Test
    public void testProcessIfStringUtilsIsBlankOptionDefault() {
        String input = "3";
        String blank = "";
        String pageSource = "Dummy Page";
        List<String> elements = new ArrayList<>();
        elements.add("query result");

        when(analyzerConsole.getInput(System.in)).thenReturn(input)
                .thenReturn(blank);
        baseQueryAnalyzer.process();

        verify(analyzerConsole, never()).showPageSource(pageSource);
        verify(analyzerConsole, never()).writePageSource(pageSource);
        verify(analyzerConsole).showElements(elements);
    }

    @Test
    public void testProcessElseStringUtilsIsBlank() {
        String blank = "";
        String pageSource = "Dummy Page";
        List<String> elements = new ArrayList<>();

        when(analyzerConsole.getInput(System.in)).thenReturn(blank);
        baseQueryAnalyzer.process();

        verify(analyzerConsole, never()).showPageSource(pageSource);
        verify(analyzerConsole, never()).writePageSource(pageSource);
        verify(analyzerConsole, never()).showElements(elements);
    }

    @Test
    public void testPostInitialize() {
        boolean actual = baseQueryAnalyzer.postInitialize();

        assertTrue(actual);
        assertEquals("foo", baseQueryAnalyzer.getStatus());
    }

    @Test
    public void testGetQueryElements() {
        String query = "Foo";
        List<String> elements = new ArrayList<>();
        elements.add("query result");

        List<String> actual = baseQueryAnalyzer.getQueryElements(query);
        assertEquals(elements, actual);
    }

    @Test
    public void testGetPageSource() {
        String actual = baseQueryAnalyzer.getPageSource();
        assertEquals("Dummy Page", actual);
    }

    @Test
    public void testLoad() {

        baseQueryAnalyzer.load();

        verifyNoInteractions(document, analyzerConsole, configs, metricsHelper,
                taskDef, taskMediator, jobMediator, factory, output, payload,
                jobMarker, jobAbortedMarker);
    }

    @Test
    public void testStore() {
        baseQueryAnalyzer.store();

        verifyNoInteractions(document, analyzerConsole, configs, metricsHelper,
                taskDef, taskMediator, jobMediator, factory, output, payload,
                jobMarker, jobAbortedMarker);
    }
}
