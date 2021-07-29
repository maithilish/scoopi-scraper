package org.codetab.scoopi.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class HttpHelperTest {

    @InjectMocks
    private HttpHelper httpHelper;

    @Mock
    private HttpFactory httpFactory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetContent() throws ClientProtocolException, IOException {
        byte[] content = new String("<html>hello</html>").getBytes();
        String userAgent = "foo";
        int timeout = 1000;
        String url = "http://example.org";
        CloseableHttpClient client = Mockito.mock(CloseableHttpClient.class);
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response =
                Mockito.mock(CloseableHttpResponse.class);
        HttpEntity httpEntity = new ByteArrayEntity(content);
        StatusLine statusLine = Mockito.mock(StatusLine.class);

        when(httpFactory.getHttpClient(userAgent, timeout)).thenReturn(client);
        when(httpFactory.httpGet(url)).thenReturn(httpGet);
        when(client.execute(httpGet)).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(response.getEntity()).thenReturn(httpEntity).thenReturn(null);

        byte[] actual = httpHelper.getContent(url, userAgent, timeout);

        assertThat(actual).isEqualTo(content);

        assertThrows(IOException.class,
                () -> httpHelper.getContent(url, userAgent, timeout));
    }

    @Test
    public void testGetContentBadRequest()
            throws ClientProtocolException, IOException {
        byte[] content = new String("<html>hello</html>").getBytes();
        String userAgent = "foo";
        int timeout = 1000;
        String url = "http://example.org";
        CloseableHttpClient client = Mockito.mock(CloseableHttpClient.class);
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response =
                Mockito.mock(CloseableHttpResponse.class);
        HttpEntity httpEntity = new ByteArrayEntity(content);
        StatusLine statusLine = Mockito.mock(StatusLine.class);

        when(httpFactory.getHttpClient(userAgent, timeout)).thenReturn(client);
        when(httpFactory.httpGet(url)).thenReturn(httpGet);
        when(client.execute(httpGet)).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode())
                .thenReturn(HttpURLConnection.HTTP_BAD_REQUEST);
        when(response.getEntity()).thenReturn(httpEntity).thenReturn(null);

        assertThrows(IOException.class,
                () -> httpHelper.getContent(url, userAgent, timeout));
    }

    @Test
    public void testEscapeUrl() {
        String actual = httpHelper.escapeUrl("http://example.org/foobar");
        assertThat(actual).isEqualTo("http://example.org/foobar");

        actual = httpHelper.escapeUrl("http://example.org/foo bar");
        assertThat(actual).isEqualTo("http://example.org/foo%20bar");
    }

    @Test
    public void testGetProtocol() {
        String actual = httpHelper.getProtocol("http://example.org/foobar");
        assertThat(actual).isEqualTo("http");

        actual = httpHelper.getProtocol("foo");
        assertThat(actual).isEqualTo("resource");
    }

}
