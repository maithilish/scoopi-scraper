package org.codetab.scoopi.model.helper;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.commons.lang3.Validate.validState;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAmount;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.util.Util;

public class Documents {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private Configs configs;

    public boolean isDocumentLive(final ZonedDateTime toDate) {
        // toDate > runDateTime
        return toDate.compareTo(configs.getRunDateTime()) > 0;
    }

    public ZonedDateTime getToDate(final ZonedDateTime fromDateTime,
            final String live, final JobInfo jobInfo) {

        notNull(fromDateTime, "fromDate must not be null");
        notNull(live, "live must not be null");
        notNull(jobInfo, "jobInfo must not be null");

        validState(nonNull(configs), "configService is not set");

        ZonedDateTime toDate = null;

        // live can be duration (PT1W) or date-time text
        String documentlive = live;
        if (StringUtils.equals(documentlive, "0") //$NON-NLS-1$
                || StringUtils.isBlank(documentlive)) {
            documentlive = "PT0S"; // zero second //$NON-NLS-1$
        }

        // calculate toDate
        try {
            TemporalAmount ta = Util.parseTemporalAmount(documentlive);
            toDate = fromDateTime.plus(ta);
        } catch (DateTimeParseException e) {
            // if live is not Duration string then parse it as ZonedDateTime
            try {
                DateTimeFormatter formatter = configs.getDateTimeFormatter();
                toDate = ZonedDateTime.parse(documentlive, formatter);
            } catch (DateTimeParseException pe) {
                LOG.warn("{} live is {} {}, defaults to 0 days",
                        jobInfo.getLabel(), documentlive, e);
                TemporalAmount ta = Util.parseTemporalAmount("PT0S"); //$NON-NLS-1$
                toDate = fromDateTime.plus(ta);
            }
        }

        if (LOG.isTraceEnabled()) {
            Marker marker = jobInfo.getJobMarker();
            LOG.trace(marker, "document.toDate. live: {} toDate:", //$NON-NLS-1$
                    documentlive, toDate);
        }
        return toDate;
    }

}
