package org.codetab.scoopi.config;

import static java.util.Objects.nonNull;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.scoopi.exception.CriticalException;

public class DerivedConfigs {

    public void addRunDates(final CompositeConfiguration configuration)
            throws ParseException {
        Date date = new Date();
        // override date with user provided date if any
        String runDateTimeString =
                configuration.getString("scoopi.runDateTimeString");
        if (nonNull(runDateTimeString)) {
            String[] dateTimeFormat = configuration
                    .getString("scoopi.dateTimeParsePattern").split(" ; ");
            date = DateUtils.parseDate(runDateTimeString, dateTimeFormat);
        }

        // date instances
        Date runDateTime = DateUtils.truncate(date, Calendar.SECOND);
        configuration.setProperty("scoopi.runDateTime", runDateTime);

        Date runDate = DateUtils.truncate(date, Calendar.DATE);
        configuration.setProperty("scoopi.runDate", runDate);

        // date and time string
        String dateTimeFormat = configuration
                .getString("scoopi.dateTimeParsePattern").split(" ; ")[0]; //$NON-NLS-1$
        runDateTimeString = DateFormatUtils.format(runDateTime, dateTimeFormat);
        configuration.setProperty("scoopi.runDateTimeString", //$NON-NLS-1$
                runDateTimeString);

        String dateFormat = configuration.getString("scoopi.dateParsePattern") //$NON-NLS-1$
                .split(" ; ")[0];
        String runDateString = DateFormatUtils.format(runDate, dateFormat);
        configuration.setProperty("scoopi.runDateString", runDateString); //$NON-NLS-1$
    }

    /**
     * Replace HighDate string with Date instance
     * @param configuration
     */
    public void replaceHighDate(final CompositeConfiguration configuration) {
        final String key = "scoopi.highDate";
        try {
            final String dateStr = configuration.getString(key);
            String[] dateTimeFormat = configuration
                    .getString("scoopi.dateTimeParsePattern").split(" ; "); //$NON-NLS-1$
            Date highDate = DateUtils.parseDate(dateStr, dateTimeFormat);
            configuration.setProperty(key, highDate);
        } catch (ParseException e) {
            throw new CriticalException("unable to parse highDate", e);
        }
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
