package org.codetab.scoopi.step.parse.htmlunit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

import com.gargoylesoftware.htmlunit.RefreshHandler;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class QueryAnalyzerTest {
    @InjectMocks
    private QueryAnalyzer queryAnalyzer;

    @Mock
    private Factory htmlUnitFactory;
    @Mock
    private HtmlPage page;
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
    public void testPostInitializeTryIfWebClient()
            throws MalformedURLException, Exception {
        Object apple = Mockito.mock(Object.class);

        byte[] bytes = {'F', 'o', 'o'};
        String kiwi = "Foo";
        String html = kiwi;
        String mango = "http://example.org"; // valid url
        String cherry = "Baz";
        URL uRL = Mockito.mock(URL.class);
        String apricot = "Qux";
        URL url = Mockito.mock(URL.class);
        StringWebResponse response = Mockito.mock(StringWebResponse.class);
        String key = "scoopi.webClient.timeout";
        String peach = "1000";
        int timeout = 1000;
        WebClient webClientPlum = Mockito.mock(WebClient.class);
        WebClient webClient = webClientPlum;
        RefreshHandler refreshHandler = Mockito.mock(RefreshHandler.class);
        WebClientOptions webClientOptions =
                Mockito.mock(WebClientOptions.class);
        WebClientOptions webClientOptions2 =
                Mockito.mock(WebClientOptions.class);
        WebClientOptions webClientOptions3 =
                Mockito.mock(WebClientOptions.class);
        WebClientOptions webClientOptions4 =
                Mockito.mock(WebClientOptions.class);
        WebClientOptions webClientOptions5 =
                Mockito.mock(WebClientOptions.class);
        WebWindow webWindow = Mockito.mock(WebWindow.class);
        RefreshHandler refreshHandler2 = Mockito.mock(RefreshHandler.class);

        when(document.getDocumentObject()).thenReturn(apple).thenReturn(bytes);
        when(document.getUrl()).thenReturn(mango).thenReturn(cherry)
                .thenReturn(apricot);
        when(htmlUnitFactory.createUrl(cherry)).thenReturn(url);
        when(htmlUnitFactory.createUrl("file:")).thenReturn(uRL);
        when(htmlUnitFactory.createURL(uRL, cherry)).thenReturn(url);
        when(htmlUnitFactory.createStringWebResponse(html, url))
                .thenReturn(response);
        when(configs.getConfig(key)).thenReturn(peach);
        when(htmlUnitFactory.createWebClient()).thenReturn(webClientPlum);
        when(htmlUnitFactory.createThreadedRefreshHandler())
                .thenReturn(refreshHandler);
        when(webClientPlum.getOptions()).thenReturn(webClientOptions)
                .thenReturn(webClientOptions2).thenReturn(webClientOptions3)
                .thenReturn(webClientOptions4).thenReturn(webClientOptions5);
        when(webClient.getCurrentWindow()).thenReturn(webWindow);
        when(htmlUnitFactory.createImmediateRefreshHandler())
                .thenReturn(refreshHandler2);
        when(htmlUnitFactory.createPage(response, webWindow)).thenReturn(page);

        boolean actual = queryAnalyzer.postInitialize();

        assertTrue(actual);
        verify(webClientPlum).setRefreshHandler(refreshHandler);
        verify(webClientOptions).setJavaScriptEnabled(false);
        verify(webClientOptions2).setCssEnabled(false);
        verify(webClientOptions3).setAppletEnabled(false);
        verify(webClientOptions4).setPopupBlockerEnabled(true);
        verify(webClientOptions5).setTimeout(timeout);
        verify(webClient).setRefreshHandler(refreshHandler2);
        verify(webClient).close();
    }

    @Test
    public void testPostInitializeTryElseWebClient() throws Exception {
        Object apple = Mockito.mock(Object.class);

        byte[] bytes = {'F', 'o', 'o'};
        String kiwi = "Foo";
        String html = kiwi;
        String mango = "Bar";
        String cherry = "Baz";
        URL uRL = Mockito.mock(URL.class);
        String apricot = "Qux";
        URL url = Mockito.mock(URL.class);
        StringWebResponse response = Mockito.mock(StringWebResponse.class);
        String key = "Quux";
        String peach = "Corge";
        int timeout = 120000;
        WebClient webClientPlum = Mockito.mock(WebClient.class);
        WebClient webClient = webClientPlum;
        RefreshHandler refreshHandler = Mockito.mock(RefreshHandler.class);
        WebClientOptions webClientOptions =
                Mockito.mock(WebClientOptions.class);
        WebClientOptions webClientOptions2 =
                Mockito.mock(WebClientOptions.class);
        WebClientOptions webClientOptions3 =
                Mockito.mock(WebClientOptions.class);
        WebClientOptions webClientOptions4 =
                Mockito.mock(WebClientOptions.class);
        WebClientOptions webClientOptions5 =
                Mockito.mock(WebClientOptions.class);
        WebWindow webWindow = Mockito.mock(WebWindow.class);
        RefreshHandler refreshHandler2 = Mockito.mock(RefreshHandler.class);

        when(document.getDocumentObject()).thenReturn(apple).thenReturn(bytes);
        when(document.getUrl()).thenReturn(mango).thenReturn(cherry)
                .thenReturn(apricot);
        when(htmlUnitFactory.createUrl(cherry)).thenReturn(url);
        when(htmlUnitFactory.createUrl("file:")).thenReturn(uRL);
        when(htmlUnitFactory.createURL(uRL, cherry)).thenReturn(url);
        when(htmlUnitFactory.createStringWebResponse(html, url))
                .thenReturn(response);
        when(configs.getConfig(key)).thenReturn(peach);
        // make webClient null
        when(htmlUnitFactory.createWebClient())
                .thenThrow(IllegalStateException.class);
        when(htmlUnitFactory.createThreadedRefreshHandler())
                .thenReturn(refreshHandler);
        when(webClientPlum.getOptions()).thenReturn(webClientOptions)
                .thenReturn(webClientOptions2).thenReturn(webClientOptions3)
                .thenReturn(webClientOptions4).thenReturn(webClientOptions5);
        when(webClient.getCurrentWindow()).thenReturn(webWindow);
        when(htmlUnitFactory.createPage(response, webWindow)).thenReturn(page);

        assertThrows(StepRunException.class,
                () -> queryAnalyzer.postInitialize());

        verify(webClientPlum, never()).setRefreshHandler(refreshHandler);
        verify(webClientOptions, never()).setJavaScriptEnabled(false);
        verify(webClientOptions2, never()).setCssEnabled(false);
        verify(webClientOptions3, never()).setAppletEnabled(false);
        verify(webClientOptions4, never()).setPopupBlockerEnabled(true);
        verify(webClientOptions5, never()).setTimeout(timeout);
        verify(htmlUnitFactory, never()).createImmediateRefreshHandler();
        verify(webClient, never()).setRefreshHandler(refreshHandler2);
        verify(webClient, never()).close();
    }

    @Test
    public void testPostInitializeTryCatchIllegalStateException()
            throws Exception {
        Object apple = Mockito.mock(Object.class);

        byte[] bytes = {'F', 'o', 'o'};
        String kiwi = "Foo";
        String html = kiwi;
        String mango = "Bar";
        String cherry = "Baz";
        URL uRL = Mockito.mock(URL.class);
        String apricot = "Qux";
        URL url = Mockito.mock(URL.class);
        StringWebResponse response = Mockito.mock(StringWebResponse.class);
        String key = "Quux";
        String peach = "Corge";
        int timeout = 120000;
        WebClient webClientPlum = Mockito.mock(WebClient.class);
        WebClient webClient = webClientPlum;
        RefreshHandler refreshHandler = Mockito.mock(RefreshHandler.class);
        WebClientOptions webClientOptions =
                Mockito.mock(WebClientOptions.class);
        WebClientOptions webClientOptions2 =
                Mockito.mock(WebClientOptions.class);
        WebClientOptions webClientOptions3 =
                Mockito.mock(WebClientOptions.class);
        WebClientOptions webClientOptions4 =
                Mockito.mock(WebClientOptions.class);
        WebClientOptions webClientOptions5 =
                Mockito.mock(WebClientOptions.class);
        WebWindow webWindow = Mockito.mock(WebWindow.class);
        RefreshHandler refreshHandler2 = Mockito.mock(RefreshHandler.class);

        when(document.getDocumentObject()).thenReturn(apple).thenReturn(bytes);
        when(document.getUrl()).thenReturn(mango).thenReturn(cherry)
                .thenReturn(apricot);
        when(htmlUnitFactory.createUrl(cherry)).thenReturn(url);
        when(htmlUnitFactory.createUrl("file:")).thenReturn(uRL);
        when(htmlUnitFactory.createURL(uRL, cherry)).thenReturn(url);
        when(htmlUnitFactory.createStringWebResponse(html, url))
                .thenReturn(response);
        when(configs.getConfig(key)).thenReturn(peach);
        when(htmlUnitFactory.createWebClient()).thenReturn(webClientPlum);
        when(htmlUnitFactory.createThreadedRefreshHandler())
                .thenReturn(refreshHandler);
        when(webClientPlum.getOptions()).thenReturn(webClientOptions)
                .thenReturn(webClientOptions2).thenReturn(webClientOptions3)
                .thenReturn(webClientOptions4).thenReturn(webClientOptions5);
        when(webClient.getCurrentWindow()).thenReturn(webWindow);
        when(htmlUnitFactory.createImmediateRefreshHandler())
                .thenReturn(refreshHandler2);
        when(htmlUnitFactory.createPage(response, webWindow)).thenReturn(page);

        boolean actual = queryAnalyzer.postInitialize();

        assertTrue(actual);
        verify(webClientPlum).setRefreshHandler(refreshHandler);
        verify(webClientOptions).setJavaScriptEnabled(false);
        verify(webClientOptions2).setCssEnabled(false);
        verify(webClientOptions3).setAppletEnabled(false);
        verify(webClientOptions4).setPopupBlockerEnabled(true);
        verify(webClientOptions5).setTimeout(timeout);
        verify(webClient).setRefreshHandler(refreshHandler2);
        verify(webClient).close();
    }

    @Test
    public void testPostInitializeTryCatchIOException() throws Exception {
        Object apple = Mockito.mock(Object.class);

        byte[] bytes = {'F', 'o', 'o'};
        String kiwi = "Foo";
        String html = kiwi;
        String mango = "Bar";
        String cherry = "Baz";
        URL uRL = Mockito.mock(URL.class);
        String apricot = "Qux";
        URL url = Mockito.mock(URL.class);
        StringWebResponse response = Mockito.mock(StringWebResponse.class);
        String key = "Quux";
        String peach = "Corge";
        int timeout = 120000;
        WebClient webClientPlum = Mockito.mock(WebClient.class);
        WebClient webClient = webClientPlum;
        RefreshHandler refreshHandler = Mockito.mock(RefreshHandler.class);
        WebClientOptions webClientOptions =
                Mockito.mock(WebClientOptions.class);
        WebClientOptions webClientOptions2 =
                Mockito.mock(WebClientOptions.class);
        WebClientOptions webClientOptions3 =
                Mockito.mock(WebClientOptions.class);
        WebClientOptions webClientOptions4 =
                Mockito.mock(WebClientOptions.class);
        WebClientOptions webClientOptions5 =
                Mockito.mock(WebClientOptions.class);
        WebWindow webWindow = Mockito.mock(WebWindow.class);
        RefreshHandler refreshHandler2 = Mockito.mock(RefreshHandler.class);

        when(document.getDocumentObject()).thenReturn(apple).thenReturn(bytes);
        when(document.getUrl()).thenReturn(mango).thenReturn(cherry)
                .thenReturn(apricot);
        when(htmlUnitFactory.createUrl(cherry)).thenReturn(url);
        when(htmlUnitFactory.createUrl("file:")).thenReturn(uRL);
        when(htmlUnitFactory.createURL(uRL, cherry)).thenReturn(url);
        when(htmlUnitFactory.createStringWebResponse(html, url))
                .thenReturn(response);
        when(configs.getConfig(key)).thenReturn(peach);
        when(htmlUnitFactory.createWebClient()).thenReturn(webClientPlum);
        when(htmlUnitFactory.createThreadedRefreshHandler())
                .thenReturn(refreshHandler);
        when(webClientPlum.getOptions()).thenReturn(webClientOptions)
                .thenReturn(webClientOptions2).thenReturn(webClientOptions3)
                .thenReturn(webClientOptions4).thenReturn(webClientOptions5);
        when(webClient.getCurrentWindow()).thenReturn(webWindow);
        when(htmlUnitFactory.createImmediateRefreshHandler())
                .thenReturn(refreshHandler2);
        when(htmlUnitFactory.createPage(response, webWindow)).thenReturn(page);

        boolean actual = queryAnalyzer.postInitialize();

        assertTrue(actual);
        verify(webClientPlum).setRefreshHandler(refreshHandler);
        verify(webClientOptions).setJavaScriptEnabled(false);
        verify(webClientOptions2).setCssEnabled(false);
        verify(webClientOptions3).setAppletEnabled(false);
        verify(webClientOptions4).setPopupBlockerEnabled(true);
        verify(webClientOptions5).setTimeout(timeout);
        verify(webClient).setRefreshHandler(refreshHandler2);
        verify(webClient).close();
    }

    @Test
    public void testPostInitializeTryCatchDataFormatException()
            throws Exception {
        Object apple = Mockito.mock(Object.class);

        byte[] bytes = {'F', 'o', 'o'};
        String kiwi = "Foo";
        String html = kiwi;
        String mango = "Bar";
        String cherry = "Baz";
        URL uRL = Mockito.mock(URL.class);
        String apricot = "Qux";
        URL url = Mockito.mock(URL.class);
        StringWebResponse response = Mockito.mock(StringWebResponse.class);
        String key = "Quux";
        String peach = "Corge";
        int timeout = 120000;
        WebClient webClientPlum = Mockito.mock(WebClient.class);
        WebClient webClient = webClientPlum;
        RefreshHandler refreshHandler = Mockito.mock(RefreshHandler.class);
        WebClientOptions webClientOptions =
                Mockito.mock(WebClientOptions.class);
        WebClientOptions webClientOptions2 =
                Mockito.mock(WebClientOptions.class);
        WebClientOptions webClientOptions3 =
                Mockito.mock(WebClientOptions.class);
        WebClientOptions webClientOptions4 =
                Mockito.mock(WebClientOptions.class);
        WebClientOptions webClientOptions5 =
                Mockito.mock(WebClientOptions.class);
        WebWindow webWindow = Mockito.mock(WebWindow.class);
        RefreshHandler refreshHandler2 = Mockito.mock(RefreshHandler.class);

        when(document.getDocumentObject()).thenReturn(apple).thenReturn(bytes);
        when(document.getUrl()).thenReturn(mango).thenReturn(cherry)
                .thenReturn(apricot);
        when(htmlUnitFactory.createUrl(cherry)).thenReturn(url);
        when(htmlUnitFactory.createUrl("file:")).thenReturn(uRL);
        when(htmlUnitFactory.createURL(uRL, cherry)).thenReturn(url);
        when(htmlUnitFactory.createStringWebResponse(html, url))
                .thenReturn(response);
        when(configs.getConfig(key)).thenReturn(peach);
        when(htmlUnitFactory.createWebClient()).thenReturn(webClientPlum);
        when(htmlUnitFactory.createThreadedRefreshHandler())
                .thenReturn(refreshHandler);
        when(webClientPlum.getOptions()).thenReturn(webClientOptions)
                .thenReturn(webClientOptions2).thenReturn(webClientOptions3)
                .thenReturn(webClientOptions4).thenReturn(webClientOptions5);
        when(webClient.getCurrentWindow()).thenReturn(webWindow);
        when(htmlUnitFactory.createImmediateRefreshHandler())
                .thenReturn(refreshHandler2);
        when(htmlUnitFactory.createPage(response, webWindow)).thenReturn(page);

        boolean actual = queryAnalyzer.postInitialize();

        assertTrue(actual);
        verify(webClientPlum).setRefreshHandler(refreshHandler);
        verify(webClientOptions).setJavaScriptEnabled(false);
        verify(webClientOptions2).setCssEnabled(false);
        verify(webClientOptions3).setAppletEnabled(false);
        verify(webClientOptions4).setPopupBlockerEnabled(true);
        verify(webClientOptions5).setTimeout(timeout);
        verify(webClient).setRefreshHandler(refreshHandler2);
        verify(webClient).close();
    }

    @Test
    public void testGetQueryElementsTry() {
        String xpath = "Foo";
        String apple = "Bar";
        List<String> list = new ArrayList<>();
        list.add(apple);
        List<Object> elements = new ArrayList<>();
        DomNode domNode = Mockito.mock(DomNode.class);
        elements.add(domNode);
        Exception e = Mockito.mock(Exception.class);

        when(page.getByXPath(xpath)).thenReturn(elements);
        when(domNode.asXml()).thenReturn(apple);

        List<String> actual = queryAnalyzer.getQueryElements(xpath);

        assertEquals(list, actual);
        verify(e, never()).getMessage();
    }

    @Test
    public void testGetQueryElementsTryCatchException() {
        String xpath = "Foo";
        List<String> list = new ArrayList<>();

        Exception e = Mockito.mock(Exception.class);
        String apple = "Bar";

        when(page.getByXPath(xpath)).thenThrow(IllegalStateException.class);
        when(e.getMessage()).thenReturn(apple);

        List<String> actual = queryAnalyzer.getQueryElements(xpath);

        assertEquals(list, actual);
    }

    @Test
    public void testGetPageSource() {
        byte[] bytes = {};
        String grape = new String(bytes);

        when(document.getDocumentObject()).thenReturn(bytes);

        String actual = queryAnalyzer.getPageSource();

        assertEquals(grape, actual);
    }
}
