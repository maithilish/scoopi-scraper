package org.codetab.scoopi.model.helper;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.commons.lang3.Validate.validState;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAmount;
import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.model.Fingerprint;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.Metadata;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

public class MetadataHelper {

    static final Logger LOGGER = LoggerFactory.getLogger(MetadataHelper.class);

    @Inject
    private Configs configs;
    @Inject
    private ObjectFactory objectFactory;

    public boolean isDocumentLive(final Date toDate) {
        // toDate > runDateTime
        return toDate.compareTo(configs.getRunDateTime()) > 0;
    }

    public Date getToDate(final Date fromDate, final String live,
            final JobInfo jobInfo) {

        notNull(fromDate, "fromDate must not be null");
        notNull(live, "live must not be null");
        notNull(jobInfo, "jobInfo must not be null");

        validState(nonNull(configs), "configService is not set");

        // convert fromDate to DateTime
        ZonedDateTime fromDateTime = ZonedDateTime
                .ofInstant(fromDate.toInstant(), ZoneId.systemDefault());
        ZonedDateTime toDate = null;

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
            // if live is not Duration string then parse it as Date
            try {
                String[] patterns =
                        configs.getConfigArray("scoopi.dateParsePattern"); //$NON-NLS-1$
                // multiple patterns so needs DateUtils
                Date td = DateUtils.parseDateStrictly(documentlive, patterns);
                toDate = ZonedDateTime.ofInstant(td.toInstant(),
                        ZoneId.systemDefault());
            } catch (ParseException | ConfigNotFoundException pe) {
                LOGGER.warn("{} live is {} {}, defaults to 0 days",
                        jobInfo.getLabel(), documentlive, e);
                TemporalAmount ta = Util.parseTemporalAmount("PT0S"); //$NON-NLS-1$
                toDate = fromDateTime.plus(ta);
            }
        }

        if (LOGGER.isTraceEnabled()) {
            Marker marker = jobInfo.getMarker();
            LOGGER.trace(marker, "document.toDate. live: {} toDate:", //$NON-NLS-1$
                    documentlive, toDate);
        }
        return Date.from(Instant.from(toDate));
    }

    public Metadata createMetadata(final Fingerprint locatorFp,
            final Fingerprint locatorWithDataFp, final Date documentDate) {
        return objectFactory.createMetadata(locatorFp, locatorWithDataFp,
                documentDate);
    }
}
