package org.codetab.scoopi.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.text.ParseException;
import java.util.Date;

import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ConfigsTest {

    @Mock
    private ConfigProperties configProperties;

    @InjectMocks
    private Configs configs;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetConfig() throws ConfigNotFoundException {
        given(configProperties.getProperty("xyz")).willReturn("xxx");

        configs.getConfig("xyz");

        verify(configProperties).getProperty("xyz");
    }

    @Test
    public void testGetConfigNull() throws ConfigNotFoundException {
        given(configProperties.getProperty(null))
                .willThrow(ConfigNotFoundException.class);

        testRule.expect(ConfigNotFoundException.class);
        configs.getConfig(null); // sut
    }

    @Test
    public void testGetConfigArray() throws ConfigNotFoundException {
        String[] array = {"x", "y"};
        given(configProperties.getStringArray("xyz")).willReturn(array);

        configs.getConfigArray("xyz");

        verify(configProperties).getStringArray("xyz");
    }

    @Test
    public void testGetConfigArrayNull() throws ConfigNotFoundException {
        given(configProperties.getStringArray("xyz"))
                .willThrow(ConfigNotFoundException.class);

        testRule.expect(ConfigNotFoundException.class);
        configs.getConfigArray("xyz"); // sut
    }

    @Test
    public void testIsTrue() {
        String key = "xyz";
        given(configProperties.getBoolean(key, false)).willReturn(true, false);

        boolean actual = configs.isTrue(key);
        assertThat(actual).isTrue();

        actual = configs.isTrue(key);
        assertThat(actual).isFalse();
    }

    @Test
    public void testGetBoolean() {
        String key = "xyz";
        given(configProperties.getBoolean(key, true)).willReturn(true, false);

        boolean actual = configs.getBoolean(key);
        assertThat(actual).isTrue();

        actual = configs.getBoolean(key);
        assertThat(actual).isFalse();
    }

    @Test
    public void testGetProperty() {
        String key = "xyz";
        String expected = "test";
        given(configProperties.get(key)).willReturn(expected);

        Object actual = configs.getProperty(key);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testSetProperty() {
        String key = "xyz";
        String value = "test";
        configs.setProperty(key, value);

        verify(configProperties).put(key, value);
    }

    @Test
    public void testGetRunDate()
            throws ConfigNotFoundException, ParseException {
        Date date = new Date();
        given(configProperties.get("scoopi.runDate")).willReturn(date);

        Date actual = configs.getRunDate();

        assertThat(actual).isEqualTo(date);
    }

    @Test
    public void testGetRunDateException()
            throws ConfigNotFoundException, ParseException {
        given(configProperties.get("scoopi.runDate")).willReturn(null);

        testRule.expect(CriticalException.class);
        configs.getRunDate();
    }

    @Test
    public void testGetRunDateString()
            throws ConfigNotFoundException, ParseException {
        String dateString = new Date().toString();
        given(configProperties.getProperty("scoopi.runDateString"))
                .willReturn(dateString);

        String actual = configs.getRunDateString();

        assertThat(actual).isEqualTo(dateString);
    }

    @Test
    public void testGetRunDateStringException()
            throws ConfigNotFoundException, ParseException {
        given(configProperties.getProperty("scoopi.runDateString"))
                .willThrow(ConfigNotFoundException.class);

        testRule.expect(CriticalException.class);
        configs.getRunDateString();
    }

    @Test
    public void testGetRunDateTime()
            throws ConfigNotFoundException, ParseException {
        Date date = new Date();
        given(configProperties.get("scoopi.runDateTime")).willReturn(date);

        Date actual = configs.getRunDateTime();

        assertThat(actual).isEqualTo(date);
    }

    @Test
    public void testGetRunDateTimeException()
            throws ConfigNotFoundException, ParseException {
        given(configProperties.get("scoopi.runDateTime")).willReturn(null);

        testRule.expect(CriticalException.class);
        configs.getRunDateTime();
    }

    @Test
    public void testGetRunDateTimeString()
            throws ConfigNotFoundException, ParseException {
        String dateString = new Date().toString();
        given(configProperties.getProperty("scoopi.runDateTimeString"))
                .willReturn(dateString);

        String actual = configs.getRunDateTimeString();

        assertThat(actual).isEqualTo(dateString);
    }

    @Test
    public void testGetRunDateTimeStringException()
            throws ConfigNotFoundException, ParseException {
        given(configProperties.getProperty("scoopi.runDateTimeString"))
                .willThrow(ConfigNotFoundException.class);

        testRule.expect(CriticalException.class);
        configs.getRunDateTimeString();
    }

    @Test
    public void testIsTestMode() throws ConfigNotFoundException {
        String eclipseTestRunner =
                "org.eclipse.jdt.internal.junit.runner.RemoteTestRunner"; //$NON-NLS-1$
        String mavenTestRunner =
                "org.apache.maven.surefire.booter.ForkedBooter"; //$NON-NLS-1$

        given(configProperties.getProperty("scoopi.runnerClass"))
                .willReturn("xyz", eclipseTestRunner, mavenTestRunner);
        assertThat(configs.isTestMode()).isFalse();
        assertThat(configs.isTestMode()).isTrue();
        assertThat(configs.isTestMode()).isTrue();
    }

    @Test
    public void testIsDevMode() {
        given(configProperties.getProperty("scoopi.mode", "")).willReturn("dev",
                "test");
        assertThat(configs.isDevMode()).isTrue();
        assertThat(configs.isDevMode()).isFalse();
    }

    @Test
    public void testGetStage() throws ConfigNotFoundException {
        given(configProperties.getProperty("scoopi.runnerClass"))
                .willReturn("xyz");
        given(configProperties.getProperty("scoopi.runnerClass"))
                .willReturn("test");
        assertThat(configs.getStage()).isEqualTo("stage: production");

        String eclipseTestRunner =
                "org.eclipse.jdt.internal.junit.runner.RemoteTestRunner"; //$NON-NLS-1$
        given(configProperties.getProperty("scoopi.runnerClass"))
                .willReturn(eclipseTestRunner);
        assertThat(configs.getStage()).isEqualTo("stage: test");

        given(configProperties.getProperty("scoopi.runnerClass"))
                .willReturn("xyz");
        given(configProperties.getProperty("scoopi.mode", ""))
                .willReturn("dev");
        assertThat(configs.getStage()).isEqualTo("stage: dev");
    }

    @Test
    public void testIsPersist() {
        given(configProperties.getBoolean("xyz", true)).willReturn(true, false);
        assertThat(configs.isPersist("xyz")).isTrue();
        assertThat(configs.isPersist("xyz")).isFalse();
    }

    @Test
    public void testUseDataStore() {
        given(configProperties.getBoolean("scoopi.useDatastore", true))
                .willReturn(true, false);
        assertThat(configs.useDataStore()).isTrue();
        assertThat(configs.useDataStore()).isFalse();
    }

    @Test
    public void testGetTimeoutDefault() throws ConfigNotFoundException {
        int actual = configs.getTimeout();
        assertThat(actual).isEqualTo(120000);

        given(configs.getConfig("scoopi.webClient.timeout"))
                .willThrow(ConfigNotFoundException.class);
        actual = configs.getTimeout();
        assertThat(actual).isEqualTo(120000);
    }

    @Test
    public void testGetTimeoutFromConfig() throws ConfigNotFoundException {

        given(configs.getConfig("scoopi.webClient.timeout")).willReturn("5000");

        int actual = configs.getTimeout();
        assertThat(actual).isEqualTo(5000);
    }

    @Test
    public void testGetUserAgentDefault() throws ConfigNotFoundException {
        String expected =
                "Mozilla/5.0 (X11; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0"; //$NON-NLS-1$

        given(configs.getConfig("scoopi.webClient.userAgent"))
                .willThrow(ConfigNotFoundException.class);
        String actual = configs.getUserAgent();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetUserAgentFromConfig() throws ConfigNotFoundException {
        String expected = "chrome";

        given(configs.getConfig("scoopi.webClient.userAgent"))
                .willReturn(expected);
        String actual = configs.getUserAgent();
        assertThat(actual).isEqualTo(expected);
    }

}
