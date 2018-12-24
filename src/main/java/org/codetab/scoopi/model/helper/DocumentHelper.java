package org.codetab.scoopi.model.helper;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.commons.lang3.Validate.validState;
import static org.codetab.scoopi.util.Util.spaceit;

import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.zip.DataFormatException;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.system.ConfigService;
import org.codetab.scoopi.util.CompressionUtil;
import org.codetab.scoopi.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

/**
 * Helper routines to handle documents.
 * @author Maithilish
 *
 */
public class DocumentHelper {

    /**
     * logger.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(DocumentHelper.class);

    /**
     * ConfigService singleton.
     */
    @Inject
    private ConfigService configService;
    /**
     * ObjectFactory
     */
    @Inject
    private ObjectFactory objectFactory;

    /**
     * private constructor.
     */
    @Inject
    private DocumentHelper() {
    }

    /**
     * Returns id of document where toDate is &gt;= runDateTime config. When,
     * there are more than one matching documents, then the id of last one is
     * returned. If, list is null then returns null.
     * @param documents
     *            list of {@link Document}, not null
     * @return active document id or null when no matching document is found or
     *         input is empty or null.
     */
    public Long getActiveDocumentId(final List<Document> documents) {
        validState(nonNull(configService), "configService is not set");

        if (isNull(documents)) {
            return null;
        }
        Long activeDocumentId = null;
        for (Document doc : documents) {
            Date toDate = doc.getToDate();
            Date runDateTime = configService.getRunDateTime();
            // toDate > today
            if (toDate.compareTo(runDateTime) >= 0) {
                activeDocumentId = doc.getId();
            }
        }
        return activeDocumentId;
    }

    public Document getActiveDocument(final Locator locator) {
        validState(nonNull(configService), "configService is not set");

        if (isNull(locator)) {
            return null;
        }
        Document activeDocument = null;
        Date runDateTime = configService.getRunDateTime();
        for (Document document : locator.getDocuments()) {
            // toDate > rundate
            if (document.getToDate().compareTo(runDateTime) >= 0
                    && locator.getUrl().equals(document.getUrl())) {
                activeDocument = document;
            }
        }
        return activeDocument;
    }

    /*
     * get todate for live and reset document toDate to new toDate. If still
     * active for new toDate then use the active document else reset toDate to
     * runDateTime - 1 and set activeDocument to null so that new document is
     * created
     */
    public boolean resetToDate(final Document document, final Date newToDate) {
        validState(nonNull(configService), "configService is not set");

        document.setToDate(newToDate);
        // expired for new toDate
        if (newToDate.compareTo(configService.getRunDateTime()) < 0) {
            Date runDateMinusOne =
                    DateUtils.addSeconds(configService.getRunDateTime(), -1);
            document.setToDate(runDateMinusOne);
            return true;
        } else {
            return false;
        }
    }

    /**
     * get document from list
     * @param id
     * @param documents
     *            list
     * @return document if found
     * @throws NoSuchElementException
     *             if no document of that id is found
     */
    public Document getDocument(final Long id, final List<Document> documents) {
        for (Document doc : documents) {
            if (doc.getId() == id) {
                return doc;
            }
        }
        throw new NoSuchElementException(
                spaceit("no document with id:", String.valueOf(id)));
    }

    /**
     * <p>
     * Calculates document expire date from live field and from date.
     * <p>
     * Live field can hold duration (ISO-8601 duration format PnDTnHnMn.nS) or
     * date string. When live is duration then it is added to fromDate else
     * string is parsed as to date based on parse pattern provided by
     * ConfigService.
     * <p>
     * In case, live is not defined or it is zero or blank then from date is
     * returned.
     * @param fromDate
     *            document from date, not null
     * @param fields
     *            list of fields, not null
     * @return a Date which is document expire date, not null
     * @throws org.codetab.gotz.exception.FieldsParseException
     * @see java.time.Duration
     */
    public Date getToDate(final Date fromDate, final String live,
            final JobInfo jobInfo) {

        notNull(fromDate, "fromDate must not be null");
        notNull(live, "live must not be null");
        notNull(jobInfo, "jobInfo must not be null");

        validState(nonNull(configService), "configService is not set");

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
                        configService.getConfigArray("gotz.dateParsePattern"); //$NON-NLS-1$
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

    /**
     * <p>
     * Get uncompressed bytes of the documentObject.
     * @param document
     *            which has the documentObject, not null
     * @return uncompressed bytes of the documentObject, not null
     * @throws IOException
     *             if error closing stream
     * @throws DataFormatException
     *             if error decompress data
     */
    public byte[] getDocumentObject(final Document document)
            throws DataFormatException, IOException {
        notNull(document, "document must not be null");
        validState(nonNull(document.getDocumentObject()),
                "documentObject is null");

        final int bufferLength = 4086;
        return CompressionUtil.decompressByteArray(
                (byte[]) document.getDocumentObject(), bufferLength);
    }

    /**
     * <p>
     * Compresses the documentObject and sets it to Document.
     * @param document
     *            document to set, not null
     * @param documentObject
     *            object to compress and set, not null
     * @return true if success
     * @throws IOException
     *             any exception while compression
     */
    public boolean setDocumentObject(final Document document,
            final byte[] documentObject) throws IOException {
        notNull(document, "document must not be null");
        notNull(documentObject, "documentObject must not be null");

        final int bufferLength = 4086;
        byte[] compressedObject =
                CompressionUtil.compressByteArray(documentObject, bufferLength);
        document.setDocumentObject(compressedObject);
        LOGGER.debug("documentObject size {} compressed size {}",
                documentObject.length, compressedObject.length);
        return true;
    }

    /**
     * <p>
     * Factory method to create Document and set its fields.
     * <p>
     * Uses DI to create the Document.
     * @param name
     *            document name, not null
     * @param url
     *            document URL, not null
     * @param fromDate
     *            document start date, not null
     * @param toDate
     *            document expire date, not null
     * @return document, not null
     */
    public Document createDocument(final String name, final String url,
            final Date fromDate, final Date toDate) {
        notNull(name, "name must not be null");
        notNull(url, "url must not be null");
        notNull(fromDate, "fromDate must not be null");
        notNull(toDate, "toDate must not be null");

        return objectFactory.createDocument(name, url, fromDate, toDate);
    }
}
