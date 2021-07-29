package org.codetab.scoopi.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.Test;

public class HttpFactoryTest {

    private HttpFactory httpFactory;

    @Before
    public void setUp() throws Exception {
        httpFactory = new HttpFactory();
    }

    @Test
    public void testGetHttpClient() throws IOException {
        // not possible to test configs
        CloseableHttpClient client = httpFactory.getHttpClient("foo", 1000);
        client.close();
    }

    @Test
    public void testHttpGet() {
        String url = "http://example.org";
        HttpGet actual = httpFactory.httpGet(url);
        assertThat(actual.getURI().toString()).isEqualTo(url);
    }

}
