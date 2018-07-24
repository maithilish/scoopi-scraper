package org.codetab.scoopi.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * <p>
 * URLConnectionHelper tests.
 * @author Maithilish
 *
 */
public class URLConnectionHelperTest {

    private URLConnectionHelper ucHelper;

    @Before
    public void setUp() throws Exception {
        ucHelper = new URLConnectionHelper();
    }

    @Test
    public void testGetURLConnection() throws IOException {
        String urlSpec = "file:///home/x/a.txt";
        URL expected = new URL(urlSpec);

        // when
        URLConnection actual = ucHelper.getURLConnection(urlSpec);

        assertThat(actual.getURL()).isEqualTo(expected);
    }

    @Test
    public void testSetRequestProperty() throws IOException {
        URLConnection uc = ucHelper.getURLConnection("http://example.org");

        // when
        ucHelper.setRequestProperty(uc, "x", "y");

        assertThat(uc.getRequestProperty("x")).isEqualTo("y");
    }

    @Test
    public void testGetProtocol() {
        assertThat(ucHelper.getProtocol("http://example.org"))
                .isEqualTo("http");
        assertThat(ucHelper.getProtocol("https://example.org"))
                .isEqualTo("https");
        assertThat(ucHelper.getProtocol("file://example.org"))
                .isEqualTo("file");
        assertThat(ucHelper.getProtocol("org/codetab/a")).isEqualTo("resource");
        assertThat(ucHelper.getProtocol("/org/codetab/a"))
                .isEqualTo("resource");
    }

    @Test
    public void testGetContent() throws IOException {
        String str = "test string";
        InputStream in = IOUtils.toInputStream(str, "UTF-8");

        HttpURLConnection uc = Mockito.mock(HttpURLConnection.class);

        given(uc.getInputStream()).willReturn(in);

        byte[] result = ucHelper.getContent(uc);

        assertThat(result).isEqualTo(str.getBytes());
    }

    @Test
    public void testEscapeUrl() {
        assertThat(ucHelper.escapeUrl("http://example.org"))
                .isEqualTo("http://example.org");
        assertThat(ucHelper.escapeUrl("http://example.org/with space"))
                .isEqualTo("http://example.org/with%20space");
        assertThat(ucHelper.escapeUrl("http://example.org/with space/"))
                .isEqualTo("http://example.org/with%20space/");
    }

}
