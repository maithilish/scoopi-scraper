package org.codetab.scoopi.config;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DerivedConfigs {

    private static final Logger LOG = LogManager.getLogger();

    public void addRunDates(final CompositeConfiguration configuration)
            throws ParseException {

        ZonedDateTime date;
        String runDateTimeText =
                configuration.getString("scoopi.runDateTimeText");
        DateTimeFormatter formatter = getDateTimeFormatter(
                configuration.getString("scoopi.dateTimePattern"));

        if (nonNull(runDateTimeText)) {
            LOG.debug("create runDateTime from configuration");
            date = ZonedDateTime.parse(runDateTimeText, formatter);
        } else {
            LOG.debug("create runDateTime from system clock");
            date = ZonedDateTime.now();
        }

        // set date,time instances
        ZonedDateTime runDateTime = date.truncatedTo(ChronoUnit.SECONDS);
        configuration.setProperty("scoopi.runDateTime", runDateTime);

        ZonedDateTime runDate = date.truncatedTo(ChronoUnit.DAYS);
        configuration.setProperty("scoopi.runDate", runDate);

        // date,time text
        runDateTimeText = runDateTime.format(formatter);
        configuration.setProperty("scoopi.runDateTimeText", //$NON-NLS-1$
                runDateTimeText);

        String runDateText = runDate.format(formatter);
        configuration.setProperty("scoopi.runDateText", runDateText); //$NON-NLS-1$

        LOG.debug("runDateTime      {}", runDateTime);
        LOG.debug("runDateTimeText  {}", runDateTimeText);
        LOG.debug("runDate          {}", runDate);
        LOG.debug("runDateText      {}", runDateText);
    }

    private DateTimeFormatter getDateTimeFormatter(final String pattern) {
        DateTimeFormatter formatter;
        if (isNull(pattern)) {
            formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
        } else {
            formatter = DateTimeFormatter.ofPattern(pattern);
        }
        // zone in both formatters is null
        if (isNull(formatter.getZone())) {
            formatter = formatter.withZone(ZoneId.systemDefault());
        }
        return formatter;
    }

    /**
     * Replace HighDate string with ZonedDateTime instance
     * @param configuration
     */
    public void replaceHighDate(final CompositeConfiguration configuration) {
        final String key = "scoopi.highDate";

        final String highDateText = configuration.getString(key);
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        LocalDateTime highDate = LocalDateTime.parse(highDateText, formatter);
        ZonedDateTime zonedHighDate =
                ZonedDateTime.of(highDate, ZoneId.systemDefault());
        configuration.setProperty(key, zonedHighDate);
    }

    public void addRunnerClass(final CompositeConfiguration configuration) {
        StackTraceElement[] stackElements =
                Thread.currentThread().getStackTrace();
        StackTraceElement stackElement =
                stackElements[stackElements.length - 1];
        configuration.addProperty("scoopi.runnerClass",
                stackElement.getClassName());
    }
}
