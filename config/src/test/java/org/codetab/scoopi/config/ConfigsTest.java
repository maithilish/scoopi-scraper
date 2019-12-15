package org.codetab.scoopi.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
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
    private ConfigService configService;

    @InjectMocks
    private Configs configs;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInitConfigService() {
        configs.initConfigService("x", "y");
        verify(configService).init("x", "y");
    }

    @Test
    public void testGetConfig() throws ConfigNotFoundException {
        given(configService.getConfig("xyz")).willReturn("xxx");

        configs.getConfig("xyz");

        verify(configService).getConfig("xyz");
    }

    @Test
    public void testGetConfigNull() throws ConfigNotFoundException {
        given(configService.getConfig(null))
                .willThrow(ConfigNotFoundException.class);

        testRule.expect(ConfigNotFoundException.class);
        configs.getConfig(null); // sut
    }

    @Test
    public void testGetConfigArray() throws ConfigNotFoundException {
        String[] array = {"x", "y"};
        given(configService.getConfigArray("xyz")).willReturn(array);

        configs.getConfigArray("xyz");

        verify(configService).getConfigArray("xyz");
    }

    @Test
    public void testGetConfigArrayNull() throws ConfigNotFoundException {
        given(configService.getConfigArray("xyz"))
                .willThrow(ConfigNotFoundException.class);

        testRule.expect(ConfigNotFoundException.class);
        configs.getConfigArray("xyz"); // sut
    }

    @Test
    public void testIsTrue() {
        String key = "xyz";
        given(configService.getBoolean(key, false)).willReturn(true, false);

        boolean actual = configs.isTrue(key);
        assertThat(actual).isTrue();

        actual = configs.isTrue(key);
        assertThat(actual).isFalse();
    }

    @Test
    public void testGetBoolean() {
        String key = "xyz";
        given(configService.getBoolean(key, true)).willReturn(true, false);

        boolean actual = configs.getBoolean(key);
        assertThat(actual).isTrue();

        actual = configs.getBoolean(key);
        assertThat(actual).isFalse();
    }

    @Test
    public void testGetProperty() {
        String key = "xyz";
        String expected = "test";
        given(configService.getProperty(key)).willReturn(expected);

        Object actual = configs.getProperty(key);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testSetProperty() {
        String key = "xyz";
        String value = "test";
        configs.setProperty(key, value);

        verify(configService).setProperty(key, value);
    }

    @Test
    public void testGetRunDateParsedRunDate()
            throws ConfigNotFoundException, ParseException {
        String runDate = "10-12-2019";
        String patterns = "dd-MM-yyyy";
        Date expected = DateUtils.parseDate(runDate, patterns);
        given(configService.getProperty("scoopi.parsed.runDate"))
                .willReturn(expected);

        Date actual = configs.getRunDate();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetRunDateNullParsedRunDate()
            throws ConfigNotFoundException, ParseException {
        String runDate = "10-12-2019";
        String patterns = "dd-MM-yyyy";
        Date expected = DateUtils.parseDate(runDate, patterns);

        given(configService.getProperty("scoopi.parsed.runDate"))
                .willReturn(null);
        given(configService.getConfig("scoopi.runDate")).willReturn(runDate);
        given(configService.getConfig("scoopi.dateParsePattern"))
                .willReturn(patterns);

        Date actual = configs.getRunDate();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetRunDateParseException()
            throws ConfigNotFoundException, ParseException {
        String runDate = "10-12-2019";
        String patterns = "dd/MM/yyyy";

        given(configService.getProperty("scoopi.parsed.runDate"))
                .willReturn(null);
        given(configService.getConfig("scoopi.runDate")).willReturn(runDate);
        given(configService.getConfig("scoopi.dateParsePattern"))
                .willReturn(patterns);

        testRule.expect(CriticalException.class);
        configs.getRunDate();
    }

    @Test
    public void testGetRunDateConfigNotFoundException()
            throws ConfigNotFoundException, ParseException {
        given(configService.getProperty("scoopi.parsed.runDate"))
                .willReturn(null);
        given(configService.getConfig("scoopi.runDate"))
                .willThrow(ConfigNotFoundException.class);

        testRule.expect(CriticalException.class);
        configs.getRunDate();
    }

    @Test
    public void testGetRunDateTimeParsedRunDate()
            throws ConfigNotFoundException, ParseException {
        String runDateTime = "10-12-2019 20:59:59.000";
        String patterns = "dd-MM-yyyy HH:mm:ss.SSS";
        Date expected = DateUtils.parseDate(runDateTime, patterns);
        given(configService.getProperty("scoopi.parsed.runDateTime"))
                .willReturn(expected);

        Date actual = configs.getRunDateTime();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetRunDateTimeNullParsedRunDate()
            throws ConfigNotFoundException, ParseException {
        String runDateTime = "10-12-2019 20:59:59.000";
        String patterns = "dd-MM-yyyy HH:mm:ss.SSS";
        Date expected = DateUtils.parseDate(runDateTime, patterns);

        given(configService.getProperty("scoopi.parsed.runDateTime"))
                .willReturn(null);
        given(configService.getConfig("scoopi.runDateTime"))
                .willReturn(runDateTime);
        given(configService.getConfig("scoopi.dateTimeParsePattern"))
                .willReturn(patterns);

        Date actual = configs.getRunDateTime();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetRunDateTimeParseException()
            throws ConfigNotFoundException, ParseException {
        String runDateTime = "10-12-2019 20:59:59.000";
        String patterns = "dd/MM/yyyy HH:mm:ss.SSS";

        given(configService.getProperty("scoopi.parsed.runDateTime"))
                .willReturn(null);
        given(configService.getConfig("scoopi.runDateTime"))
                .willReturn(runDateTime);
        given(configService.getConfig("scoopi.dateTimeParsePattern"))
                .willReturn(patterns);

        testRule.expect(CriticalException.class);
        configs.getRunDateTime();
    }

    @Test
    public void testGetRunDateTimeConfigNotFoundException()
            throws ConfigNotFoundException, ParseException {
        given(configService.getProperty("scoopi.parsed.runDateTime"))
                .willReturn(null);
        given(configService.getConfig("scoopi.runDateTime"))
                .willThrow(ConfigNotFoundException.class);

        testRule.expect(CriticalException.class);
        configs.getRunDateTime();
    }

    @Test
    public void testGetHighDateParsedRunDate()
            throws ConfigNotFoundException, ParseException {
        String runDateTime = "10-12-2019 20:59:59.000";
        String patterns = "dd-MM-yyyy HH:mm:ss.SSS";
        Date expected = DateUtils.parseDate(runDateTime, patterns);
        given(configService.getProperty("scoopi.parsed.runHighDate"))
                .willReturn(expected);

        Date actual = configs.getHighDate();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetHighDateNullParsedRunDate()
            throws ConfigNotFoundException, ParseException {
        String runDateTime = "10-12-2019 20:59:59.000";
        String patterns = "dd-MM-yyyy HH:mm:ss.SSS";
        Date expected = DateUtils.parseDate(runDateTime, patterns);

        given(configService.getProperty("scoopi.parsed.runHighDate"))
                .willReturn(null);
        given(configService.getConfig("scoopi.highDate"))
                .willReturn(runDateTime);
        given(configService.getConfig("scoopi.dateTimeParsePattern"))
                .willReturn(patterns);

        Date actual = configs.getHighDate();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetHighDateParseException()
            throws ConfigNotFoundException, ParseException {
        String runDateTime = "10-12-2019 20:59:59.000";
        String patterns = "dd/MM/yyyy HH:mm:ss.SSS";

        given(configService.getProperty("scoopi.parsed.runHighDate"))
                .willReturn(null);
        given(configService.getConfig("scoopi.highDate"))
                .willReturn(runDateTime);
        given(configService.getConfig("scoopi.dateTimeParsePattern"))
                .willReturn(patterns);

        testRule.expect(CriticalException.class);
        configs.getHighDate();
    }

    @Test
    public void testGetHighDateConfigNotFoundException()
            throws ConfigNotFoundException, ParseException {
        given(configService.getProperty("scoopi.parsed.runHighDate"))
                .willReturn(null);
        given(configService.getConfig("scoopi.highDate"))
                .willThrow(ConfigNotFoundException.class);

        testRule.expect(CriticalException.class);
        configs.getHighDate();
    }

    @Test
    public void testIsTestMode() {
        String eclipseTestRunner =
                "org.eclipse.jdt.internal.junit.runner.RemoteTestRunner"; //$NON-NLS-1$
        String mavenTestRunner =
                "org.apache.maven.surefire.booter.ForkedBooter"; //$NON-NLS-1$

        given(configService.getRunnerClass()).willReturn("xyz",
                eclipseTestRunner, mavenTestRunner);
        assertThat(configs.isTestMode()).isFalse();
        assertThat(configs.isTestMode()).isTrue();
        assertThat(configs.isTestMode()).isTrue();
    }

    @Test
    public void testIsDevMode() {
        given(configService.getString("scoopi.mode")).willReturn("dev", "test");
        assertThat(configs.isDevMode()).isTrue();
        assertThat(configs.isDevMode()).isFalse();
    }

    @Test
    public void testGetStage() {
        given(configService.getRunnerClass()).willReturn("xyz");
        given(configService.getString("scoopi.mode")).willReturn("test");
        assertThat(configs.getStage()).isEqualTo("stage: production");

        String eclipseTestRunner =
                "org.eclipse.jdt.internal.junit.runner.RemoteTestRunner"; //$NON-NLS-1$
        given(configService.getRunnerClass()).willReturn(eclipseTestRunner);
        given(configService.getString("scoopi.mode")).willReturn("test");
        assertThat(configs.getStage()).isEqualTo("stage: test");

        given(configService.getRunnerClass()).willReturn("xyz");
        given(configService.getString("scoopi.mode")).willReturn("dev");
        assertThat(configs.getStage()).isEqualTo("stage: dev");
    }

    @Test
    public void testIsPersist() {
        given(configService.getBoolean("xyz", true)).willReturn(true, false);
        assertThat(configs.isPersist("xyz")).isTrue();
        assertThat(configs.isPersist("xyz")).isFalse();
    }

    @Test
    public void testUseDataStore() {
        given(configService.getBoolean("scoopi.useDatastore", true))
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
