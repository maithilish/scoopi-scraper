package org.codetab.scoopi.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.ParseException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DerivedConfigsTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    private CompositeConfiguration configuration;
    private DerivedConfigs derivedConfigs;

    @Before
    public void setUp() throws Exception {
        configuration = new CompositeConfiguration();
        derivedConfigs = new DerivedConfigs();
    }

    @Test
    public void testAddRunDates() throws ParseException {
        String dateText = "2021-01-10T10:20:05";
        String dateTimePattern = "yyyy-MM-dd'T'HH:mm:ss";

        configuration.addProperty("scoopi.runDateTimeText", dateText);
        configuration.addProperty("scoopi.dateTimePattern", dateTimePattern);

        derivedConfigs.addRunDates(configuration);

        ZonedDateTime aRunDateTime =
                (ZonedDateTime) configuration.getProperty("scoopi.runDateTime");
        ZonedDateTime eRunDateTime =
                parseZonedDateTime(dateText, dateTimePattern);
        assertThat(aRunDateTime).isEqualTo(eRunDateTime);

        ZonedDateTime aRunDate =
                (ZonedDateTime) configuration.getProperty("scoopi.runDate");
        ZonedDateTime eRunDate = eRunDateTime.truncatedTo(ChronoUnit.DAYS);
        assertThat(aRunDate).isEqualTo(eRunDate);

        String aRunDateTimeText =
                (String) configuration.getProperty("scoopi.runDateTimeText");
        String eRunDateTimeText =
                formatZonedDateTime(eRunDateTime, dateTimePattern);
        assertThat(aRunDateTimeText).isEqualTo(eRunDateTimeText);

        String aRunDateText =
                (String) configuration.getProperty("scoopi.runDateText");
        String eRunDateText = formatZonedDateTime(eRunDate, dateTimePattern);
        assertThat(aRunDateText).isEqualTo(eRunDateText);
    }

    @Test
    public void testAddRunDatesDateTimePatternNotDefined()
            throws ParseException {
        String dateText = "2021-01-10T10:20:05";
        String dateTimePattern = "yyyy-MM-dd'T'HH:mm:ss";

        // don't set pattern, defaults to ISO
        configuration.addProperty("scoopi.runDateTimeText", dateText);

        derivedConfigs.addRunDates(configuration);

        ZonedDateTime aRunDateTime =
                (ZonedDateTime) configuration.getProperty("scoopi.runDateTime");
        ZonedDateTime eRunDateTime =
                parseZonedDateTime(dateText, dateTimePattern);
        assertThat(aRunDateTime).isEqualTo(eRunDateTime);

        ZonedDateTime aRunDate =
                (ZonedDateTime) configuration.getProperty("scoopi.runDate");
        ZonedDateTime eRunDate = eRunDateTime.truncatedTo(ChronoUnit.DAYS);
        assertThat(aRunDate).isEqualTo(eRunDate);

        String aRunDateTimeText =
                (String) configuration.getProperty("scoopi.runDateTimeText");
        String eRunDateTimeText =
                formatZonedDateTime(eRunDateTime, dateTimePattern);
        assertThat(aRunDateTimeText).isEqualTo(eRunDateTimeText);

        String aRunDateText =
                (String) configuration.getProperty("scoopi.runDateText");
        String eRunDateText = formatZonedDateTime(eRunDate, dateTimePattern);
        assertThat(aRunDateText).isEqualTo(eRunDateText);
    }

    @Test
    public void testAddRunDatesRunDateNotDefinedByUser() throws ParseException {
        String dateTimePattern = "yyyy-MM-dd'T'HH:mm:ss";

        configuration.addProperty("scoopi.dateTimePattern", dateTimePattern);

        derivedConfigs.addRunDates(configuration);

        assertThat(
                (ZonedDateTime) configuration.getProperty("scoopi.runDateTime"))
                        .isNotNull();
        assertThat((ZonedDateTime) configuration.getProperty("scoopi.runDate"))
                .isNotNull();
        assertThat((String) configuration.getProperty("scoopi.runDateTimeText"))
                .isNotNull();
        assertThat((String) configuration.getProperty("scoopi.runDateText"))
                .isNotNull();
    }

    @Test
    public void testReplaceHighDate() {
        String dateText = "2037-12-31T23:59:59.999";
        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS";
        ZonedDateTime expected = parseZonedDateTime(dateText, pattern);

        configuration.addProperty("scoopi.highDate", dateText);
        derivedConfigs.replaceHighDate(configuration);
        ZonedDateTime highDate =
                (ZonedDateTime) configuration.getProperty("scoopi.highDate");
        assertThat(highDate).isEqualTo(expected);
    }

    @Test
    public void testAddRunnerClass() {
        StackTraceElement[] stackElements =
                Thread.currentThread().getStackTrace();
        String expected =
                stackElements[stackElements.length - 1].getClassName();
        derivedConfigs.addRunnerClass(configuration);
        assertThat(configuration.getProperty("scoopi.runnerClass"))
                .isEqualTo(expected);
    }

    private ZonedDateTime parseZonedDateTime(final String dateText,
            final String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern)
                .withZone(ZoneId.systemDefault());
        return ZonedDateTime.parse(dateText, formatter);
    }

    private String formatZonedDateTime(final ZonedDateTime date,
            final String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern)
                .withZone(ZoneId.systemDefault());
        return formatter.format(date);
    }
}
