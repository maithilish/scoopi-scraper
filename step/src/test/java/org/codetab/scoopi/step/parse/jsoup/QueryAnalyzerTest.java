package org.codetab.scoopi.step.parse.jsoup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.DataFormatException;

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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Marker;

public class QueryAnalyzerTest {
    @InjectMocks
    private QueryAnalyzer queryAnalyzer;

    @Mock
    private org.jsoup.nodes.Document page;
    @Mock
    private DocumentHelper documentHelper;
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

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testPostInitializeTry() throws Exception {
        Object apple = Mockito.mock(Object.class);
        InputStream html = Mockito.mock(InputStream.class);
        org.jsoup.nodes.Document page1 =
                Mockito.mock(org.jsoup.nodes.Document.class);

        when(document.getDocumentObject()).thenReturn(apple);
        when(documentHelper.getDocumentHTML(document)).thenReturn(html);
        when(documentHelper.createDocument(html)).thenReturn(page1);

        boolean actual = queryAnalyzer.postInitialize();

        assertTrue(actual);
    }

    @Test
    public void testPostInitializeTryCatchDataFormatException()
            throws Exception {
        Object apple = Mockito.mock(Object.class);
        InputStream html = Mockito.mock(InputStream.class);
        org.jsoup.nodes.Document page1 =
                Mockito.mock(org.jsoup.nodes.Document.class);

        when(document.getDocumentObject()).thenReturn(apple);
        when(documentHelper.createDocument(html)).thenReturn(page1);
        when(documentHelper.getDocumentHTML(document))
                .thenThrow(DataFormatException.class);

        assertThrows(StepRunException.class,
                () -> queryAnalyzer.postInitialize());
    }

    @Test
    public void testPostInitializeTryCatchIOException() throws Exception {
        Object apple = Mockito.mock(Object.class);
        InputStream html = Mockito.mock(InputStream.class);
        org.jsoup.nodes.Document page1 =
                Mockito.mock(org.jsoup.nodes.Document.class);

        when(document.getDocumentObject()).thenReturn(apple);
        when(documentHelper.createDocument(html)).thenReturn(page1);
        when(documentHelper.getDocumentHTML(document))
                .thenThrow(IOException.class);

        assertThrows(StepRunException.class,
                () -> queryAnalyzer.postInitialize());
    }

    @Test
    public void testPostInitializeTryCatchIllegalStateException()
            throws Exception {
        Object apple = Mockito.mock(Object.class);
        InputStream html = Mockito.mock(InputStream.class);
        org.jsoup.nodes.Document page1 =
                Mockito.mock(org.jsoup.nodes.Document.class);

        when(document.getDocumentObject()).thenReturn(apple);
        when(documentHelper.createDocument(html)).thenReturn(page1);
        when(documentHelper.getDocumentHTML(document))
                .thenThrow(IllegalStateException.class);

        assertThrows(StepRunException.class,
                () -> queryAnalyzer.postInitialize());
    }

    @Test
    public void testPostInitializeTryCatchNullPointerException()
            throws Exception {
        Object apple = Mockito.mock(Object.class);
        InputStream html = Mockito.mock(InputStream.class);
        org.jsoup.nodes.Document page1 =
                Mockito.mock(org.jsoup.nodes.Document.class);

        when(document.getDocumentObject()).thenReturn(apple);
        when(documentHelper.createDocument(html)).thenReturn(page1);
        when(documentHelper.getDocumentHTML(document))
                .thenThrow(NullPointerException.class);

        assertThrows(StepRunException.class,
                () -> queryAnalyzer.postInitialize());
    }

    @Test
    public void testGetQueryElementsTry() {
        String selector = "Foo";
        String outerHtml = "Bar";
        List<String> list = new ArrayList<>();
        list.add(outerHtml);
        Elements elements = Mockito.mock(Elements.class);
        Element element = Mockito.mock(Element.class);
        Stream<Element> stream = Stream.of(element);
        Exception e = Mockito.mock(Exception.class);

        when(page.select(selector)).thenReturn(elements);
        when(elements.stream()).thenReturn(stream);
        when(element.outerHtml()).thenReturn(outerHtml);

        List<String> actual = queryAnalyzer.getQueryElements(selector);

        assertEquals(list, actual);
        verify(e, never()).getMessage();
    }

    @Test
    public void testGetQueryElementsTryCatchException() {
        String selector = "Foo";
        List<String> list = new ArrayList<>();
        Elements elements = Mockito.mock(Elements.class);
        Stream<Element> stream = Stream.of(new Element("Foo"));
        Exception e = Mockito.mock(Exception.class);
        String apple = "Bar";

        when(page.select(selector)).thenReturn(elements);
        when(elements.stream()).thenReturn(stream);
        when(e.getMessage()).thenReturn(apple);

        List<String> actual = queryAnalyzer.getQueryElements(selector);

        assertEquals(list, actual);
    }

    @Test
    public void testGetPageSource() {
        byte[] bytes = {'F', 'o', 'o'};
        String grape = new String(bytes);

        when(document.getDocumentObject()).thenReturn(bytes);

        String actual = queryAnalyzer.getPageSource();

        assertEquals(grape, actual);
    }
}
