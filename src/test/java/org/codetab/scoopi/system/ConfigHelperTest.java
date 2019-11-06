package org.codetab.scoopi.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ConfigHelperTest {

    @Mock
    private ConfigService configService;

    @InjectMocks
    private ConfigHelper configHelper;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetTimeoutDefault() throws ConfigNotFoundException {
        int actual = configHelper.getTimeout();
        assertThat(actual).isEqualTo(120000);

        given(configService.getConfig("scoopi.webClient.timeout"))
                .willThrow(ConfigNotFoundException.class);
        actual = configHelper.getTimeout();
        assertThat(actual).isEqualTo(120000);
    }

    @Test
    public void testGetTimeoutFromConfig() throws ConfigNotFoundException {

        given(configService.getConfig("scoopi.webClient.timeout"))
                .willReturn("5000");

        int actual = configHelper.getTimeout();
        assertThat(actual).isEqualTo(5000);
    }

    @Test
    public void testGetUserAgentDefault() throws ConfigNotFoundException {
        String expected =
                "Mozilla/5.0 (X11; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0"; //$NON-NLS-1$

        given(configService.getConfig("scoopi.webClient.userAgent"))
                .willThrow(ConfigNotFoundException.class);
        String actual = configHelper.getUserAgent();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetUserAgentFromConfig() throws ConfigNotFoundException {
        String expected = "chrome";

        given(configService.getConfig("scoopi.webClient.userAgent"))
                .willReturn(expected);
        String actual = configHelper.getUserAgent();
        assertThat(actual).isEqualTo(expected);
    }
}
