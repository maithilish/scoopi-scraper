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

import org.codetab.scoopi.dao.DaoException;
import org.codetab.scoopi.dao.IDocumentDao;
import org.codetab.scoopi.dao.IMetadataDao;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.JobStateException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.exception.TransactionException;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.Fingerprint;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.Metadata;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.helper.DocumentHelper;
import org.codetab.scoopi.model.helper.MetadataHelper;
import org.codetab.scoopi.step.JobMediator;
import org.codetab.scoopi.step.PayloadFactory;
import org.codetab.scoopi.step.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Abstract Base Loader. Either, loads active document from store or if not
 * found, creates new document by fetching resource from from web or file
 * system. Delegates the fetch to the concrete sub class.
 * @author Maithilish
 *
 */
public abstract class BaseLoader extends Step {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(BaseLoader.class);

    /**
     * locator.
     */
    private Locator locator;
    /**
     * active document.
     */
    private Document document;

    @Inject
    private IMetadataDao metadataDao;
    @Inject
    private IDocumentDao documentDao;
    @Inject
    private MetadataHelper metadataHelper;
    @Inject
    private DocumentHelper documentHelper;
    @Inject
    private PayloadFactory payloadFactory;

    // FIXME - dbfix, remove daomig and persistmig if any

    @Inject
    private JobMediator jobMediator;

    private long jobId;

    private boolean fetchDocument = true;

    /**
     * Creates log marker from locator name and group.
     * @return true
     * @see org.codetab.scoopi.step.IStep#initialize()
     */
    @Override
    public boolean initialize() {
        validState(nonNull(getPayload()), "payload is null");
        validState(nonNull(getPayload().getData()), "payload data is null");

        jobId = getPayload().getJobInfo().getId();
        final Object pData = getPayload().getData();
        if (pData instanceof Locator) {
            this.locator = (Locator) pData;
        } else {
            final String message =
                    spaceit("payload is not instance of Locator, but",
                            pData.getClass().getName());
            throw new StepRunException(message);
        }
        return true;
    }

    /**
     * Tries to load locator from store, if successful, then add fields and URL
     * of input locator to loaded locator and use it as input locator. If no
     * locator found in db, then input locator with its fields is used.
     *
     * @return true
     * @see org.codetab.scoopi.step.IStep#load()
     */
    @Override
    public boolean load() {
        validState(nonNull(locator), "locator is null");

        if (!persist()) {
            return true;
        }

        String live = "PT0S"; // default
        try {
            String taskGroup = getJobInfo().getGroup();
            live = taskDef.getLive(taskGroup);
        } catch (final DefNotFoundException e1) {
        } catch (IOException e) {
            LOGGER.error(marker, "{}, unable to get live, defaults to PT0S",
                    getLabel());
        }

        String id = locator.getFingerprint().getValue();
        Metadata metadata = null;
        try {
            metadata = metadataDao.get(id);
        } catch (DaoException e) {
        }

        try {
            if (nonNull(metadata)) {
                Date toDate = metadataHelper.getToDate(
                        metadata.getDocumentDate(), live, getJobInfo());
                if (metadataHelper.isDocumentLive(toDate)) {
                    document = documentDao.get(id,
                            metadata.getDocument().getValue());
                }
            }

            if (isNull(document)) {
                // metadata or document not found or document stale
                // remove containing folder
                metadataDao.remove(id);
            } else {
                // saved document loaded
                fetchDocument = false;
                final String message = getLabeled("use saved document");
                LOGGER.debug(marker, "{}", message);
                LOGGER.trace(marker, "loaded document:{}{}", LINE, document);
            }
            return true;
        } catch (DaoException e) {
            final String message = "load document";
            throw new StepRunException(message, e);
        }
    }

    /**
     * <p>
     * Loads the active document for locator.
     * <p>
     * In case, locator id it not null (locator is loaded from DB), then gets
     * active document id from locator documents list.
     * </p>
     * <p>
     * If document id is not null, then it gets document from locator list and
     * checks whether it is expired for current live value ignoring the old
     * document toDate which is based on earlier live value. If expired, then
     * activeDocument id is set to null so that new document can be created.
     * </p>
     * <p>
     * If active document id is not null, then it loads the document with its
     * documentObject. Otherwise, it creates new document and adds it locator.
     * <p>
     * When new document is created, it fetches the documRentObject either from
     * web or file system and adds it to document using DocumentHelper which
     * compresses the documentObject. Other metadata like from and to dates are
     * also added to new document.
     * <p>
     *
     * @return true
     * @throws StepRunException
     *             if error when fetch document content or compressing it
     * @see org.codetab.scoopi.step.IStep#process()
     */
    @Override
    public boolean process() {
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

            // FIXME - dbfix remove this
            final Date fromDate = new Date();
            final Date toDate = new Date();

            // create new document
            Document newDocument = documentHelper.createDocument(
                    locator.getName(), locator.getUrl(), fromDate, toDate);
            newDocument.setDocumentObject(documentObject);

            document = newDocument;
            locator.getDocuments().add(newDocument);
            setOutput(newDocument);

            setConsistent(true);
            LOGGER.debug(marker, "{} create new document, toDate: {}",
                    getLabel(), document.getToDate());
            LOGGER.trace(marker, "create new document{}{}", LINE, document);
        } else {
            // document = locator.getDocuments().get(0);
            setOutput(document);
            setConsistent(true);
        }
        return true;
    }

    /**
     * <p>
     * Stores locator and its documents when persists is true. As DAO may clear
     * the data object after persist, they are reloaded.
     *
     * @return true;
     * @throws StepRunException
     *             if error when persist.
     * @see org.codetab.scoopi.step.IStep#store()
     */
    @Override
    public boolean store() {
        validState(nonNull(locator), "locator is null");
        validState(nonNull(document), "document is null");

        try {
            if (persist() && fetchDocument) {
                // if fetchDocument is true then folder is deleted in load()
                // create fresh folder and save locator(with document) and
                // metadata
                Fingerprint locatorFp = locator.getFingerprint();
                Fingerprint documentFp =
                        documentDao.save(locatorFp.getValue(), document);
                Metadata metadata = metadataHelper.createMetadata(locatorFp,
                        documentFp, configs.getRunDateTime());
                metadataDao.save(locatorFp.getValue(), metadata);
                LOGGER.debug(marker, "document stored, {}", getLabel());
            }
        } catch (final DaoException e) {
            final String message = "unable to store document";
            throw new StepRunException(message, e);
        }
        return true;
    }

    @Override
    public boolean handover() {
        validState(isConsistent(), "step inconsistent");

        LOGGER.debug("push document tasks to taskpool");
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
            jobMediator.pushPayloads(payloads, jobId);
        } catch (InterruptedException | JobStateException
                | TransactionException e) {
            final String message =
                    spaceit("create defined tasks for the document and push",
                            getPayload().toString());
            throw new StepRunException(message, e);
        }
        return true;
    }

    private boolean persist() {
        if (!configs.useDataStore()) {
            // disabled at global level
            return false;
        }
        if (!configs.isPersist("scoopi.persist.locator")) { //$NON-NLS-1$
            // enabled at global but disabled at model level
            return false;
        }
        // enabled at global and model level
        return true;
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
