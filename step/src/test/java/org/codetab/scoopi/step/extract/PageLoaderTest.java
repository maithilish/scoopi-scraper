package org.codetab.scoopi.step.extract;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;

import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.dao.IDocumentDao;
import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.helper.HttpHelper;
import org.codetab.scoopi.helper.IOHelper;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.helper.Documents;
import org.codetab.scoopi.step.base.FetchThrottle;
import org.codetab.scoopi.step.base.PayloadFactory;
import org.codetab.scoopi.step.base.Persists;
import org.codetab.scoopi.step.mediator.JobMediator;
import org.codetab.scoopi.step.mediator.TaskMediator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.codahale.metrics.Counter;

public class PageLoaderTest {
    @InjectMocks
    private PageLoader pageLoader;

    @Mock
    private HttpHelper httpHelper;
    @Mock
    private IOHelper ioHelper;
    @Mock
    private MetricsHelper metricsHelper;
    @Mock
    private Configs configs;
    @Mock
    private IDocumentDao documentDao;
    @Mock
    private Documents documents;
    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private PayloadFactory payloadFactory;
    @Mock
    private JobMediator jobMediator;
    @Mock
    private Persists persists;
    @Mock
    private FetchThrottle fetchThrottle;
    @Mock
    private Locator locator;
    @Mock
    private Document document;
    @Mock
    private ITaskDef taskDef;
    @Mock
    private TaskMediator taskMediator;
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
    public void testFetchDocumentObjectFromResource() throws Exception {
        String urlSpec = "Foo";
        String protocol = "resource";
        URL fileURL = Mockito.mock(URL.class);

        Counter orange = Mockito.mock(Counter.class);

        byte[] bytes = {'f', 'o', 'o'};

        when(httpHelper.getProtocol(urlSpec)).thenReturn(protocol);
        when(ioHelper.getResourceURL(urlSpec)).thenReturn(fileURL);
        when(ioHelper.toByteArray(fileURL)).thenReturn(bytes);
        when(metricsHelper.getCounter(pageLoader, "fetch", "resource"))
                .thenReturn(orange);

        byte[] actual = pageLoader.fetchDocumentObject(urlSpec);

        assertArrayEquals(bytes, actual);
        verify(orange).inc();
    }

    @Test
    public void testFetchDocumentObjectFromResourceException()
            throws Exception {
        String urlSpec = "Foo";
        String protocol = "resource";
        URL fileURL = Mockito.mock(URL.class);

        Counter orange = Mockito.mock(Counter.class);

        when(httpHelper.getProtocol(urlSpec)).thenReturn(protocol);
        when(ioHelper.getResourceURL(urlSpec)).thenReturn(fileURL);

        when(ioHelper.toByteArray(fileURL)).thenThrow(IOException.class);

        when(metricsHelper.getCounter(pageLoader, "fetch", "resource"))
                .thenReturn(orange);

        assertThrows(IOException.class,
                () -> pageLoader.fetchDocumentObject(urlSpec));

        verifyNoInteractions(orange);
    }

    @Test
    public void testFetchDocumentObjectFromFile() throws Exception {
        String urlSpec = "Foo";
        String protocol = "file";
        URL fileURL = Mockito.mock(URL.class);

        Counter orange = Mockito.mock(Counter.class);

        byte[] bytes = {'f', 'o', 'o'};

        when(httpHelper.getProtocol(urlSpec)).thenReturn(protocol);
        when(ioHelper.getURLFromSpec(urlSpec)).thenReturn(fileURL);
        when(ioHelper.toByteArray(fileURL)).thenReturn(bytes);
        when(metricsHelper.getCounter(pageLoader, "fetch", "file"))
                .thenReturn(orange);

        byte[] actual = pageLoader.fetchDocumentObject(urlSpec);

        assertArrayEquals(bytes, actual);
        verify(orange).inc();
    }

    @Test
    public void testFetchDocumentObjectFromFileException() throws Exception {
        String urlSpec = "Foo";
        String protocol = "file";
        URL fileURL = Mockito.mock(URL.class);

        Counter orange = Mockito.mock(Counter.class);

        when(httpHelper.getProtocol(urlSpec)).thenReturn(protocol);
        when(ioHelper.getURLFromSpec(urlSpec)).thenReturn(fileURL);

        when(ioHelper.toByteArray(fileURL)).thenThrow(IOException.class);

        when(metricsHelper.getCounter(pageLoader, "fetch", "file"))
                .thenReturn(orange);

        assertThrows(IOException.class,
                () -> pageLoader.fetchDocumentObject(urlSpec));

        verifyNoInteractions(orange);
    }

    @Test
    public void testFetchDocumentObjectFromHttp() throws Exception {
        String urlSpec = "Foo";
        String protocol = "http";
        int timeout = 1;
        String userAgent = "Baz";
        String urlSpecEscaped = "Qux";
        byte[] bytes = {'f', 'o', 'o'};
        Counter nlyvzzma = Mockito.mock(Counter.class);

        when(httpHelper.getProtocol(urlSpec)).thenReturn(protocol);
        when(configs.getWebClientTimeout()).thenReturn(timeout);
        when(configs.getUserAgent()).thenReturn(userAgent);
        when(httpHelper.escapeUrl(urlSpec)).thenReturn(urlSpecEscaped);
        when(httpHelper.getContent(urlSpecEscaped, userAgent, timeout))
                .thenReturn(bytes);
        when(metricsHelper.getCounter(pageLoader, "fetch", "web"))
                .thenReturn(nlyvzzma);

        byte[] actual = pageLoader.fetchDocumentObject(urlSpec);

        assertArrayEquals(bytes, actual);
        verify(nlyvzzma).inc();
    }

    @Test
    public void testFetchDocumentObjectFromHttps() throws Exception {
        String urlSpec = "Foo";
        String protocol = "https";
        int timeout = 1;
        String userAgent = "Baz";
        String urlSpecEscaped = "Qux";
        byte[] bytes = {'f', 'o', 'o'};
        Counter nlyvzzma = Mockito.mock(Counter.class);

        when(httpHelper.getProtocol(urlSpec)).thenReturn(protocol);
        when(configs.getWebClientTimeout()).thenReturn(timeout);
        when(configs.getUserAgent()).thenReturn(userAgent);
        when(httpHelper.escapeUrl(urlSpec)).thenReturn(urlSpecEscaped);
        when(httpHelper.getContent(urlSpecEscaped, userAgent, timeout))
                .thenReturn(bytes);
        when(metricsHelper.getCounter(pageLoader, "fetch", "web"))
                .thenReturn(nlyvzzma);

        byte[] actual = pageLoader.fetchDocumentObject(urlSpec);

        assertArrayEquals(bytes, actual);
        verify(nlyvzzma).inc();
    }

    @Test
    public void testFetchDocumentObjectUnknownProtocol() throws Exception {
        String urlSpec = "Foo";
        String protocol = "unknown";

        when(httpHelper.getProtocol(urlSpec)).thenReturn(protocol);

        assertThrows(IOException.class,
                () -> pageLoader.fetchDocumentObject(urlSpec));

        verifyNoInteractions(metricsHelper, ioHelper, configs);
    }
}
