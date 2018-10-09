package org.codetab.scoopi.step.base;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.validState;
import static org.codetab.scoopi.util.Util.LINE;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.factory.PayloadFactory;
import org.codetab.scoopi.model.helper.DocumentHelper;
import org.codetab.scoopi.persistence.DocumentPersistence;
import org.codetab.scoopi.persistence.LocatorPersistence;
import org.codetab.scoopi.step.Step;
import org.codetab.scoopi.system.ErrorLogger;
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
    /**
     * persister.
     */
    @Inject
    private LocatorPersistence locatorPersistence;
    /**
     * persister.
     */
    @Inject
    private DocumentPersistence documentPersistence;
    /**
     * helper.
     */
    @Inject
    private DocumentHelper documentHelper;
    @Inject
    private PayloadFactory payloadFactory;
    @Inject
    private ErrorLogger errorLogger;

    /**
     * Creates log marker from locator name and group.
     * @return true
     * @see org.codetab.gotz.step.IStep#initialize()
     */
    @Override
    public boolean initialize() {
        validState(nonNull(getPayload()), "payload is null");
        validState(nonNull(getPayload().getData()), "payload data is null");

        Object pData = getPayload().getData();
        if (pData instanceof Locator) {
            this.locator = (Locator) pData;
        } else {
            String message =
                    String.join(" ", "payload is not instance of Locator, but",
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
     * @see org.codetab.gotz.step.IStep#load()
     */
    @Override
    public boolean load() {
        validState(nonNull(locator), "locator is null");

        Locator savedLocator = null;

        // load locator from db
        if (persist()) {
            savedLocator = locatorPersistence.loadLocator(locator.getName(),
                    locator.getGroup());
        }

        if (isNull(savedLocator)) {
            // use the locator from payload passed to this step
            String message = getLabeled("use locator defined in defs");
            LOGGER.debug(marker, "{}", message);
            LOGGER.trace(marker, "defined locator:{}{}", LINE, locator);
        } else {
            // update existing locator with new fields and URL
            savedLocator.setUrl(locator.getUrl());

            // switch locator to persisted locator (detached locator)
            locator = savedLocator;
            String message = getLabeled("use locator loaded from store");
            LOGGER.debug(marker, "{}", message);
            LOGGER.trace(marker, "loaded locator:{}{}", LINE, locator);
        }
        return true;
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
     * @see org.codetab.gotz.step.IStep#process()
     */
    @Override
    public boolean process() {
        validState(nonNull(locator), "locator is null");

        String taskGroup = getJobInfo().getGroup();
        String live;
        try {
            live = taskDefs.getLive(taskGroup);
        } catch (DefNotFoundException e1) {
            live = "PT0S";
        }

        /*
         * load the active document, get new todate for new live and reset
         * document toDate to new toDate. If still active for new toDate then
         * use the active document else reset toDate to runDateTime - 1 and set
         * activeDocument to null so that new document is created
         */
        Document activeDoc =
                documentHelper.getActiveDocument(locator.getDocuments());
        if (nonNull(activeDoc)) {
            Date newToDate = documentHelper.getToDate(activeDoc.getFromDate(),
                    live, getJobInfo());
            if (documentHelper.resetToDate(activeDoc, newToDate)) {
                activeDoc = null;
            }
        }

        /**
         * if activeDocumentId is null create new document otherwise load the
         * active document.
         */
        if (isNull(activeDoc)) {
            // no active document, create new one
            byte[] documentObject = null;
            try {
                // fetch documentObject as byte[]
                documentObject = fetchDocumentObject(locator.getUrl());
            } catch (IOException e) {
                String message = "unable to fetch document page";
                throw new StepRunException(message, e);
            }

            // document metadata
            Date fromDate = configService.getRunDateTime();
            Date toDate =
                    documentHelper.getToDate(fromDate, live, getJobInfo());

            // create new document
            activeDoc = documentHelper.createDocument(locator.getName(),
                    locator.getUrl(), fromDate, toDate);

            // compress and set documentObject
            try {
                documentHelper.setDocumentObject(activeDoc, documentObject);
            } catch (IOException e) {
                String message = "unable to compress document page";
                throw new StepRunException(message, e);
            }

            document = activeDoc;
            locator.getDocuments().add(document);
            setOutput(document);
            setConsistent(true);
            LOGGER.debug(marker, "{} create new document, toDate: {}",
                    getLabel(), document.getToDate());
            LOGGER.trace(marker, "create new document{}{}", LINE, document);
        } else {
            // as activeDoc comes from datastore it indicates that
            // datastore is active so load the activeDoc with doc object
            document = documentPersistence.loadDocument(activeDoc.getId());
            setOutput(document);
            setConsistent(true);
            LOGGER.debug(marker, "{}, use stored document, toDate: {}",
                    getLabel(), document.getToDate());
            LOGGER.trace(marker, "use stored document {}", document);
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
     * @see org.codetab.gotz.step.IStep#store()
     */
    @Override
    public boolean store() {
        validState(nonNull(locator), "locator is null");
        validState(nonNull(document), "document is null");

        try {
            boolean persist = persist();
            // store locator
            if (persist && locatorPersistence.storeLocator(locator)) {
                // if stored then reload locator and document
                Locator tLocator =
                        locatorPersistence.loadLocator(locator.getId());
                if (nonNull(tLocator)) {
                    locator = tLocator;
                }

                Document tDocument =
                        documentPersistence.loadDocument(document.getId());
                if (nonNull(tDocument)) {
                    document = tDocument;
                    setOutput(tDocument);
                }
                LOGGER.debug(marker, "locator and document stored, {}",
                        getLabel());
                LOGGER.trace(marker, "stored locator{}{}", LINE, locator);
            }
        } catch (RuntimeException e) {
            String message = "unable to store locator and document";
            throw new StepRunException(message, e);
        }
        return true;
    }

    @Override
    public boolean handover() {
        validState(isConsistent(), "step inconsistent");

        LOGGER.debug("push document tasks to taskpool");
        String group = getJobInfo().getGroup();

        List<String> taskNames = taskDefs.getTaskNames(group);
        List<Payload> payloads = payloadFactory.createPayloads(group, taskNames,
                getStepInfo(), getJobInfo().getName(), getOutput());

        for (Payload payload : payloads) {
            try {
                taskMediator.pushPayload(payload);
            } catch (InterruptedException e) {
                String message = String.join(" ", "handover document,",
                        payload.toString());
                errorLogger.log(CAT.INTERNAL, message, e);
            }
        }
        return true;
    }

    private boolean persist() {
        Optional<Boolean> locatorLevelPersistence = Optional.ofNullable(true);
        boolean persist = locatorPersistence.persist(locatorLevelPersistence);
        return persist;
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
