package org.codetab.scoopi.step.base;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.validState;
import static org.codetab.scoopi.util.Util.LINE;
import static org.codetab.scoopi.util.Util.spaceit;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.dao.ChecksumException;
import org.codetab.scoopi.dao.DaoException;
import org.codetab.scoopi.dao.IDocumentDao;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.JobStateException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.exception.TransactionException;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.Fingerprint;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.helper.Documents;
import org.codetab.scoopi.step.JobMediator;
import org.codetab.scoopi.step.PayloadFactory;
import org.codetab.scoopi.step.Step;

/**
 * <p>
 * Abstract Base Loader. Loads saved document from store, if not found creates
 * new document by fetching resource from from web or file system. Delegates the
 * fetch to the concrete sub class.
 * @author Maithilish
 *
 */
public abstract class BaseLoader extends Step {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private IDocumentDao documentDao;
    @Inject
    private Documents documents;
    @Inject
    private ObjectFactory objectFactory;
    @Inject
    private PayloadFactory payloadFactory;
    @Inject
    private JobMediator jobMediator;
    @Inject
    private Persists persists;

    private Locator locator;
    private Document document;

    private boolean fetchDocument = true;

    private boolean persist;

    /**
     * Get and assign locator from payload
     * @return true
     * @see org.codetab.scoopi.step.IStep#initialize()
     */
    @Override
    public void initialize() {
        validState(nonNull(getPayload()), "payload is null");
        validState(nonNull(getPayload().getData()), "payload data is null");

        final Object pData = getPayload().getData();
        if (pData instanceof Locator) {
            this.locator = (Locator) pData;
        } else {
            final String message =
                    spaceit("payload is not instance of Locator, but",
                            pData.getClass().getName());
            throw new StepRunException(message);
        }

        persist = persists.persistDocument();
    }

    /**
     * Loads document from store, if exists. Loads metadata and uses the
     * documentDate and live to get toDate and finds whether existing document
     * is live. If document live then loads it from store and marks
     * fetchDocument as false.
     * <p>
     * If metadata or document not found or document stale then removes
     * containing folder (metadata, document and parsed data files)
     * @return true
     * @see org.codetab.scoopi.step.IStep#load()
     */
    @Override
    public void load() {
        validState(nonNull(locator), "locator is null");

        if (!persist) {
            return;
        }

        String live = "PT0S"; // default
        try {
            String taskGroup = getJobInfo().getGroup();
            live = taskDef.getLive(taskGroup);
        } catch (final DefNotFoundException e1) {
        } catch (IOException e) {
            LOG.error(jobMarker, "{}, unable to get live, defaults to PT0S",
                    getLabel());
        }

        Fingerprint locatorFp = locator.getFingerprint();

        Date documentToDate = null;
        try {
            Date documentDate = documentDao.getDocumentDate(locatorFp);
            documentToDate =
                    documents.getToDate(documentDate, live, getJobInfo());
        } catch (DaoException e) {
            documentToDate = configs.getRunDateTime();
        }

        try {
            if (documents.isDocumentLive(documentToDate)) {
                try {
                    document = documentDao.get(locatorFp);
                } catch (ChecksumException e) {
                    LOG.error(jobMarker, "load document {}", e);
                }
            }

            if (isNull(document)) {
                // document not found or stale or checksum error
                // remove containing folder (document and data files)
                documentDao.delete(locatorFp);
            } else {
                // don't fetch fresh document, use saved document
                fetchDocument = false;
                final String message = getLabeled("use saved document");
                LOG.debug(jobMarker, "{}", message);
                LOG.trace(jobMarker, "loaded document:{}{}", LINE, document);
            }
        } catch (DaoException e) {
            final String message = "load document";
            throw new StepRunException(message, e);
        }
    }

    /**
     * <p>
     * If fetchDocument is true, fetches the documRentObject either from web or
     * file system and adds it to newly created document.
     * <p>
     *
     * @return true
     * @throws StepRunException
     *             if error when fetch document content or compressing it
     * @see org.codetab.scoopi.step.IStep#process()
     */
    @Override
    public void process() {
        validState(nonNull(locator), "locator is null");

        /**
         * if activeDocumentId is null create new document otherwise load the
         * active document.
         */
        if (fetchDocument) {
            // no active document, create new one
            byte[] documentObject = null;
            try {
                // fetch documentObject as byte[]
                documentObject = fetchDocumentObject(locator.getUrl());
            } catch (final IOException e) {
                final String message = "unable to fetch document page";
                throw new StepRunException(message, e);
            }

            // create new document
            Date documentDate = configs.getRunDateTime();
            Document newDocument = objectFactory.createDocument(
                    locator.getName(), documentDate, locator.getUrl(),
                    locator.getFingerprint());
            newDocument.setDocumentObject(documentObject);

            document = newDocument;
            setOutput(newDocument);

            LOG.debug(jobMarker, "{} create new document", getLabel());
            LOG.trace(jobMarker, "create new document{}{}", LINE, document);
        } else {
            setOutput(document);
        }
    }

    /**
     * <p>
     * Stores metadata and document when persists is true. Locator is not stored
     * as they are always created from defs.
     *
     * @return true;
     * @throws StepRunException
     *             if error when persist.
     * @see org.codetab.scoopi.step.IStep#store()
     */
    @Override
    public void store() {
        validState(nonNull(locator), "locator is null");
        validState(nonNull(document), "document is null");

        try {
            if (fetchDocument && persist) {
                // If fetchDocument is true then existing data files are deleted
                // in load(). Create fresh folder and save document
                Fingerprint locatorFp = locator.getFingerprint();
                documentDao.save(locatorFp, document);
                LOG.debug(jobMarker, "document stored, {}", getLabel());
            }
        } catch (final DaoException e) {
            final String message = "unable to store document";
            throw new StepRunException(message, e);
        }
    }

    @Override
    public void handover() {
        validState(nonNull(getOutput()), "output is not set");

        LOG.debug("push document tasks to taskpool");
        final String group = getJobInfo().getGroup();

        /*
         * one or more tasks are applied to a document, create new payloads for
         * each task
         */
        final List<String> taskNames = taskDef.getTaskNames(group);
        final List<Payload> payloads = payloadFactory.createPayloads(group,
                taskNames, getStepInfo(), getJobInfo().getName(), getOutput());

        for (final Payload payload : payloads) {
            // treat each task as new job with new seq job id
            payload.getJobInfo().setId(jobMediator.getJobIdSequence());
        }

        // mark this job as finished and push new task jobs for this document
        try {
            long jobId = getPayload().getJobInfo().getId();
            jobMediator.pushPayloads(payloads, jobId);
        } catch (InterruptedException | JobStateException
                | TransactionException e) {
            final String message =
                    spaceit("create defined tasks for the document and push",
                            getPayload().toString());
            throw new StepRunException(message, e);
        }
    }

    /**
     * <p>
     * Returns whether document is loaded.
     * @return true if document is not null
     */
    public boolean isDocumentLoaded() {
        return nonNull(document);
    }

    /**
     * <p>
     * Fetch document from web or file system. Template method to be implemented
     * by subclass.
     * @param url
     *            to fetch
     * @return document contents as byte array
     * @throws IOException
     *             on error
     */
    public abstract byte[] fetchDocumentObject(String url) throws IOException;
}
