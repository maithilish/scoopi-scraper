package org.codetab.scoopi.step.parse.htmlunit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ImmediateRefreshHandler;
import com.gargoylesoftware.htmlunit.RefreshHandler;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.ThreadedRefreshHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class FactoryTest {
    @InjectMocks
    private Factory factory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreatePage() throws Exception {
        String html = "Foo";
        URL url = new URL("http://example.org");
        StringWebResponse response = factory.createStringWebResponse(html, url);

        WebClient webClient = factory.createWebClient();

        HtmlPage actual =
                factory.createPage(response, webClient.getCurrentWindow());
        assertEquals(html, actual.asText());
    }

    @Test
    public void testCreateWebClient() {

        WebClient actual = factory.createWebClient();

        assertNotNull(actual);
        assertEquals(BrowserVersion.CHROME, actual.getBrowserVersion());
    }

    @Test
    public void testCreateThreadedRefreshHandler() {

        RefreshHandler actual = factory.createThreadedRefreshHandler();

        assertNotNull(actual);
        assertTrue(actual instanceof ThreadedRefreshHandler);
    }

    @Test
    public void testCreateUrl() throws Exception {
        String url = "http://example.org";
        URL uRL = new URL(url);

        URL actual = factory.createUrl(url);

        assertEquals(uRL, actual);
    }

    @Test
    public void testCreateURL() throws Exception {
        URL context = new URL("http://example.org");
        String url = "Foo";
        URL uRL = new URL(context, url);

        URL actual = factory.createURL(context, url);

        assertEquals(uRL, actual);
    }

    @Test
    public void testCreateStringWebResponse() throws Exception {
        String html = "Foo";
        URL url = new URL("http://example.org");

        StringWebResponse actual = factory.createStringWebResponse(html, url);

        assertEquals(html, actual.getContentAsString());
    }

    @Test
    public void testCreateImmediateRefreshHandler() {

        RefreshHandler actual = factory.createImmediateRefreshHandler();

        assertNotNull(actual);
        assertTrue(actual instanceof ImmediateRefreshHandler);
    }
}
