package org.codetab.scoopi.helper;

import static org.assertj.core.api.Assertions.assertThat;

import org.codetab.scoopi.config.Configs;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class HttpHelperTest {

    @Mock
    private Configs configs;

    @InjectMocks
    private HttpHelper httpHelper;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    // integration test HttpHelper.getContent() method

    @Test
    public void testEscapeUrl() {
        String actual = httpHelper.escapeUrl("http://example.org");
        assertThat(actual).isEqualTo("http://example.org");

        actual = httpHelper.escapeUrl("http://example xyz.org");
        assertThat(actual).isEqualTo("http://example%20xyz.org");
    }

    @Test
    public void testGetProtocol() {
        String actual = httpHelper.getProtocol("http://example.org");
        assertThat(actual).isEqualTo("http");

        actual = httpHelper.getProtocol("https://example.org");
        assertThat(actual).isEqualTo("https");

        actual = httpHelper.getProtocol("file://xyz");
        assertThat(actual).isEqualTo("file");

        actual = httpHelper.getProtocol("/hello");
        assertThat(actual).isEqualTo("resource");
    }

}
