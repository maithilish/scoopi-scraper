package org.codetab.scoopi.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ConfigsTest {

    @Mock
    private ConfigProperties configProperties;
    @InjectMocks
    private Configs configs;

    private String configKey;
    private String configValue;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        configKey = "test";
        configValue = "value";
    }

    @Test
    public void testGetConfigString() throws ConfigNotFoundException {
        when(configProperties.getConfig(configKey)).thenReturn(configValue);
        String actual = configs.getConfig(configKey);
        assertThat(actual).isEqualTo(configValue);
    }

    @Test
    public void testGetConfigStringConfigNotFound()
            throws ConfigNotFoundException {
        when(configProperties.getConfig(configKey))
                .thenThrow(ConfigNotFoundException.class);
        assertThrows(ConfigNotFoundException.class,
                () -> configs.getConfig(configKey));
    }

    @Test
    public void testGetConfigStringDefault() throws ConfigNotFoundException {
        String def = "defaultValue";
        when(configProperties.getConfig(configKey)).thenReturn(configValue)
                .thenThrow(ConfigNotFoundException.class);

        String actual = configs.getConfig(configKey, def);
        assertThat(actual).isEqualTo(configValue);

        actual = configs.getConfig(configKey, def);
        assertThat(actual).isEqualTo(def);
    }

    @Test
    public void testGetConfigArray() throws ConfigNotFoundException {
        String[] strs = {"foo", "bar", "baz"};
        when(configProperties.getStringArray(configKey)).thenReturn(strs);
        String[] actual = configs.getConfigArray(configKey);
        assertThat(actual).isSameAs(strs);
    }

    @Test
    public void testGetConfigArrayConfigNotFound()
            throws ConfigNotFoundException {
        when(configProperties.getStringArray(configKey))
                .thenThrow(ConfigNotFoundException.class);
        assertThrows(ConfigNotFoundException.class,
                () -> configs.getConfigArray(configKey));
    }

    @Test
    public void testGetBoolean() {
        when(configProperties.getBoolean(configKey, true)).thenReturn(false);

        boolean actual = configs.getBoolean(configKey, true);
        assertThat(actual).isEqualTo(false);
    }

    @Test
    public void testGetInt() {
        when(configProperties.getInt(configKey, 10)).thenReturn(5);

        int actual = configs.getInt(configKey, 10);
        assertThat(actual).isEqualTo(5);
    }

    @Test
    public void testGetIntStringDefault() {
        when(configProperties.getInt(configKey, 10)).thenReturn(5);

        int actual = configs.getInt(configKey, "10");
        assertThat(actual).isEqualTo(5);
    }

    @Test
    public void testGetProperty() {
        Date date = new Date();
        when(configProperties.get(configKey)).thenReturn(date);

        Object actual = configs.getProperty(configKey);
        assertThat(actual).isSameAs(date);
    }

    @Test
    public void testSetProperty() {
        configs.setProperty(configKey, configValue);
        verify(configProperties).put(configKey, configValue);
    }

    @Test
    public void testGetRunDate() {
        ZonedDateTime runDate = ZonedDateTime.now();
        when(configProperties.get("scoopi.runDate")).thenReturn(runDate);
        ZonedDateTime actual = configs.getRunDate();
        assertThat(actual).isEqualTo(runDate);
    }

    @Test
    public void testGetRunDateIsNull() {
        when(configProperties.get("scoopi.runDate")).thenReturn(null);
        assertThrows(CriticalException.class, () -> configs.getRunDate());
    }

    @Test
    public void testGetRunDateText() throws ConfigNotFoundException {
        String runDateText = "2021-01-10";
        when(configProperties.getConfig("scoopi.runDateText"))
                .thenReturn(runDateText);
        String actual = configs.getRunDateText();
        assertThat(actual).isEqualTo(runDateText);
    }

    @Test
    public void testGetRunDateTextNotFound() throws ConfigNotFoundException {
        when(configProperties.getConfig("scoopi.runDateText"))
                .thenThrow(ConfigNotFoundException.class);
        assertThrows(CriticalException.class, () -> configs.getRunDateText());
    }

    @Test
    public void testGetRunDateTime() {
        ZonedDateTime runDateTime = ZonedDateTime.now();
        when(configProperties.get("scoopi.runDateTime"))
                .thenReturn(runDateTime);
        ZonedDateTime actual = configs.getRunDateTime();
        assertThat(actual).isEqualTo(runDateTime);
    }

    @Test
    public void testGetRunDateTimeIsNull() {
        when(configProperties.get("scoopi.runDateTime")).thenReturn(null);
        assertThrows(CriticalException.class, () -> configs.getRunDateTime());
    }

    @Test
    public void testGetRunDateTimeText() throws ConfigNotFoundException {
        String runDateTimeText = "2021-01-10";
        when(configProperties.getConfig("scoopi.runDateTimeText"))
                .thenReturn(runDateTimeText);
        String actual = configs.getRunDateTimeText();
        assertThat(actual).isEqualTo(runDateTimeText);
    }

    @Test
    public void testGetRunDateTimeTextNotFound()
            throws ConfigNotFoundException {
        when(configProperties.getConfig("scoopi.runDateTimeText"))
                .thenThrow(ConfigNotFoundException.class);
        assertThrows(CriticalException.class,
                () -> configs.getRunDateTimeText());
    }

    @Test
    public void testGetDateTimeFormatter() throws ConfigNotFoundException {
        String pattern = "dd-MM-yyyy'T'hh:mm:ss.SSS ZZZZZ VV";
        when(configProperties.getConfig("scoopi.dateTimePattern"))
                .thenReturn(pattern);

        DateTimeFormatter actual = configs.getDateTimeFormatter();

        // not possible to compare DateTimeFormatter, so compare formatted dates
        ZonedDateTime zDateTime = ZonedDateTime.now();
        DateTimeFormatter expected = DateTimeFormatter.ofPattern(pattern);

        assertThat(actual.format(zDateTime))
                .isEqualTo(expected.format(zDateTime));
    }

    @Test
    public void testGetDateTimeFormatterConfigNotFound()
            throws ConfigNotFoundException {
        when(configProperties.getConfig("scoopi.dateTimePattern"))
                .thenThrow(ConfigNotFoundException.class);
        DateTimeFormatter actual = configs.getDateTimeFormatter();
        // possible to compare two instances of ISO formatters
        assertThat(actual).isEqualTo(DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    @Test
    public void testGetHighDate() {
        ZonedDateTime highDate = ZonedDateTime.now();
        when(configProperties.get("scoopi.highDate")).thenReturn(highDate);
        ZonedDateTime actual = configs.getHighDate();
        assertThat(actual).isEqualTo(highDate);
    }

    @Test
    public void testIsCluster() {
        when(configProperties.getBoolean("scoopi.cluster.enable", false))
                .thenReturn(true);
        assertThat(configs.isCluster()).isTrue();
    }

    @Test
    public void testIsTestMode() throws ConfigNotFoundException {
        String eclipseTestRunner =
                "org.eclipse.jdt.internal.junit.runner.RemoteTestRunner";
        String mavenTestRunner =
                "org.apache.maven.surefire.booter.ForkedBooter";
        when(configProperties.getConfig("scoopi.runnerClass"))
                .thenReturn(eclipseTestRunner).thenReturn(mavenTestRunner)
                .thenReturn("someRunner")
                .thenThrow(ConfigNotFoundException.class);

        assertThat(configs.isTestMode()).isTrue();
        assertThat(configs.isTestMode()).isTrue();
        assertThat(configs.isTestMode()).isFalse();
        assertThat(configs.isTestMode()).isFalse();
    }

    @Test
    public void testIsDevMode() throws ConfigNotFoundException {
        when(configProperties.getConfig("scoopi.mode")).thenReturn("dev")
                .thenThrow(ConfigNotFoundException.class);

        assertThat(configs.isDevMode()).isTrue();
        assertThat(configs.isDevMode()).isFalse();
    }

    @Test
    public void testIsPersist() {
        when(configProperties.getBoolean(configKey, true)).thenReturn(true)
                .thenReturn(false);
        assertThat(configs.isPersist(configKey)).isTrue();
        assertThat(configs.isPersist(configKey)).isFalse();
    }

    @Test
    public void testUseDataStore() {
        configKey = "scoopi.datastore.enable";
        when(configProperties.getBoolean(configKey, true)).thenReturn(true)
                .thenReturn(false);
        assertThat(configs.useDataStore()).isTrue();
        assertThat(configs.useDataStore()).isFalse();
    }

    @Test
    public void testGetStageDev() throws ConfigNotFoundException {
        when(configProperties.getConfig("scoopi.mode")).thenReturn("dev");
        when(configProperties.getConfig("scoopi.runnerClass"))
                .thenReturn("undefined");
        assertThat(configs.getStage()).isEqualTo("stage: dev");
    }

    @Test
    public void testGetStageTest() throws ConfigNotFoundException {
        String mavenTestRunner =
                "org.apache.maven.surefire.booter.ForkedBooter";
        when(configProperties.getConfig("scoopi.mode")).thenReturn("undefined");
        when(configProperties.getConfig("scoopi.runnerClass"))
                .thenReturn(mavenTestRunner);
        assertThat(configs.getStage()).isEqualTo("stage: test");
    }

    @Test
    public void testGetStageProd() throws ConfigNotFoundException {
        when(configProperties.getConfig("scoopi.mode")).thenReturn("undefined");
        when(configProperties.getConfig("scoopi.runnerClass"))
                .thenReturn("undefined");
        assertThat(configs.getStage()).isEqualTo("stage: production");
    }

    @Test
    public void testGetWebClientTimeout() throws ConfigNotFoundException {
        when(configProperties.getInt("scoopi.webClient.timeout", 120000))
                .thenReturn(1000);
        assertThat(configs.getWebClientTimeout()).isEqualTo(1000);
    }

    @Test
    public void testGetUserAgent() throws ConfigNotFoundException {
        String defaultUserAgent =
                "Mozilla/5.0 (X11; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0";
        configKey = "scoopi.webClient.userAgent";
        when(configProperties.getConfig(configKey)).thenReturn("chrome")
                .thenThrow(ConfigNotFoundException.class);
        assertThat(configs.getUserAgent()).isEqualTo("chrome");
        assertThat(configs.getUserAgent()).isEqualTo(defaultUserAgent);
    }

    @Test
    public void testIsMetricsServerEnabledCluster() {
        when(configProperties.getBoolean("scoopi.cluster.enable", false))
                .thenReturn(true);

        assertThat(configs.isMetricsServerEnabled()).isFalse();

        System.setProperty("scoopi.metrics.server.enable", "false");
        assertThat(configs.isMetricsServerEnabled()).isFalse();

        System.setProperty("scoopi.metrics.server.enable", "true");
        assertThat(configs.isMetricsServerEnabled()).isTrue();

        System.clearProperty("scoopi.metrics.server.enable");
    }

    @Test
    public void testIsMetricsServerEnabledSolo() {
        when(configProperties.getBoolean("scoopi.cluster.enable", false))
                .thenReturn(false);

        when(configProperties.getBoolean("scoopi.metrics.server.enable", true))
                .thenReturn(true).thenReturn(false);

        assertThat(configs.isMetricsServerEnabled()).isTrue();
        assertThat(configs.isMetricsServerEnabled()).isFalse();
    }

}
