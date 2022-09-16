package org.codetab.scoopi.step.parse.jsoup;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.dao.IDataDao;
import org.codetab.scoopi.defs.IDataDefDef;
import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.Fingerprint;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.helper.DataHelper;
import org.codetab.scoopi.step.base.DataFactory;
import org.codetab.scoopi.step.base.Persists;
import org.codetab.scoopi.step.mediator.JobMediator;
import org.codetab.scoopi.step.mediator.TaskMediator;
import org.codetab.scoopi.step.parse.IValueParser;
import org.codetab.scoopi.step.parse.IndexerFactory;
import org.codetab.scoopi.step.parse.ValueProcessor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ParserTest {
    @InjectMocks
    private Parser parser;

    @Mock
    private ValueParser jsoupValueParser;
    @Mock
    private DocumentHelper documentHelper;
    @Mock
    private ValueProcessor valueProcessor;
    @Mock
    private DataFactory dataFactory;
    @Mock
    private IDataDefDef dataDefDef;
    @Mock
    private IDataDao dataDao;
    @Mock
    private DataHelper dataHelper;
    @Mock
    private StopWatch timer;
    @Mock
    private IndexerFactory indexerFactory;
    @Mock
    private Persists persists;
    @Mock
    private Data data;
    @Mock
    private Document document;
    @Mock
    private IValueParser valueParser;
    @Mock
    private Fingerprint dataFingerprint;
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
    public void testPostInitializeTry() throws Exception, IOException {
        Object apple = Mockito.mock(Object.class);
        InputStream html = Mockito.mock(InputStream.class);
        org.jsoup.nodes.Document page =
                Mockito.mock(org.jsoup.nodes.Document.class);

        when(document.getDocumentObject()).thenReturn(apple);
        when(documentHelper.getDocumentHTML(document)).thenReturn(html);
        when(documentHelper.createDocument(html)).thenReturn(page);

        boolean actual = parser.postInitialize();

        assertTrue(actual);
        verify(jsoupValueParser).setPage(page);
    }

    @Test
    public void testPostInitializeTryCatchDataFormatException()
            throws Exception {
        Object apple = Mockito.mock(Object.class);
        InputStream html = Mockito.mock(InputStream.class);
        org.jsoup.nodes.Document page =
                Mockito.mock(org.jsoup.nodes.Document.class);

        when(document.getDocumentObject()).thenReturn(apple);
        when(documentHelper.createDocument(html)).thenReturn(page);
        when(documentHelper.getDocumentHTML(document))
                .thenThrow(DataFormatException.class);

        assertThrows(StepRunException.class, () -> parser.postInitialize());

        verify(jsoupValueParser, never()).setPage(page);
    }

    @Test
    public void testPostInitializeTryCatchIOException()
            throws Exception, IOException {
        Object apple = Mockito.mock(Object.class);
        InputStream html = Mockito.mock(InputStream.class);
        org.jsoup.nodes.Document page =
                Mockito.mock(org.jsoup.nodes.Document.class);

        when(document.getDocumentObject()).thenReturn(apple);
        when(documentHelper.createDocument(html)).thenReturn(page);

        when(documentHelper.getDocumentHTML(document))
                .thenThrow(IOException.class);

        assertThrows(StepRunException.class, () -> parser.postInitialize());

        verify(jsoupValueParser, never()).setPage(page);
    }

    @Test
    public void testPostInitializeTryCatchIllegalStateException()
            throws Exception, IOException {
        Object apple = Mockito.mock(Object.class);
        InputStream html = Mockito.mock(InputStream.class);
        org.jsoup.nodes.Document page =
                Mockito.mock(org.jsoup.nodes.Document.class);

        when(document.getDocumentObject()).thenReturn(apple);
        when(documentHelper.getDocumentHTML(document)).thenReturn(html);
        when(documentHelper.createDocument(html)).thenReturn(page);
        when(documentHelper.getDocumentHTML(document))
                .thenThrow(IllegalStateException.class);

        assertThrows(StepRunException.class, () -> parser.postInitialize());

        verify(jsoupValueParser, never()).setPage(page);
    }

    @Test
    public void testPostInitializeTryCatchNullPointerException()
            throws Exception, IOException {
        Object apple = Mockito.mock(Object.class);
        InputStream html = Mockito.mock(InputStream.class);
        org.jsoup.nodes.Document page =
                Mockito.mock(org.jsoup.nodes.Document.class);

        when(document.getDocumentObject()).thenReturn(apple);
        when(documentHelper.getDocumentHTML(document)).thenReturn(html);
        when(documentHelper.createDocument(html)).thenReturn(page);
        when(documentHelper.getDocumentHTML(document))
                .thenThrow(NullPointerException.class);

        assertThrows(StepRunException.class, () -> parser.postInitialize());

        verify(jsoupValueParser, never()).setPage(page);
    }
}
