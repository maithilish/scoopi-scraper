package org.codetab.scoopi.step.base;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.messages.Messages;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.helper.DocumentHelper;
import org.codetab.scoopi.persistence.DocumentPersistence;
import org.codetab.scoopi.persistence.LocatorPersistence;
import org.codetab.scoopi.step.Step;
import org.codetab.scoopi.util.Util;
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

    /**
     * Creates log marker from locator name and group.
     * @return true
     * @see org.codetab.gotz.step.IStep#initialize()
     */
    @Override
    public boolean initialize() {
        Validate.validState(nonNull(getPayload()),
                Messages.getString("BaseLoader.29")); //$NON-NLS-1$
        Validate.validState(nonNull(getPayload().getData()),
                Messages.getString("BaseLoader.30")); //$NON-NLS-1$

        Object pData = getPayload().getData();
        if (pData instanceof Locator) {
            this.locator = (Locator) pData;
        } else {
            String message = Util.join(Messages.getString("BaseLoader.28"), //$NON-NLS-1$
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
        Validate.validState(nonNull(locator),
                Messages.getString("BaseLoader.1")); //$NON-NLS-1$

        Locator savedLocator = null;

        // load locator from db
        if (persist()) {
            savedLocator = locatorPersistence.loadLocator(locator.getName(),
                    locator.getGroup());
        }

        if (isNull(savedLocator)) {
            // use the locator from payload passed to this step
            LOGGER.debug("{} {}", getLabel(), //$NON-NLS-1$
                    Messages.getString("BaseLoader.3")); //$NON-NLS-1$
        } else {
            // update existing locator with new fields and URL
            savedLocator.setUrl(locator.getUrl());

            // switch locator to persisted locator (detached locator)
            locator = savedLocator;

            LOGGER.debug("{} {}", getLabel(), //$NON-NLS-1$
                    Messages.getString("BaseLoader.5")); //$NON-NLS-1$
            LOGGER.trace(getMarker(), Messages.getString("BaseLoader.6"), //$NON-NLS-1$
                    Util.LINE, locator);
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
        Validate.validState(nonNull(locator),
                Messages.getString("BaseLoader.7")); //$NON-NLS-1$

        String taskGroup = getJobInfo().getGroup();
        String taskName = getJobInfo().getTask();
        String live;
        try {
            live = taskProvider.getFieldValue(taskGroup, taskName, "live");
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
                String message = Messages.getString("BaseLoader.8"); //$NON-NLS-1$
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
                String message = Messages.getString("BaseLoader.9"); //$NON-NLS-1$
                throw new StepRunException(message, e);
            }

            document = activeDoc;
            locator.getDocuments().add(document);
            setData(document);
            setConsistent(true);
            LOGGER.info(Messages.getString("BaseLoader.2"), getLabel(), //$NON-NLS-1$
                    document.getToDate());
            LOGGER.trace(getMarker(), Messages.getString("BaseLoader.11"), //$NON-NLS-1$
                    document);
        } else {
            // as activeDoc comes from datastore it indicates that
            // datastore is active so load the activeDoc with doc object
            document = documentPersistence.loadDocument(activeDoc.getId());
            setData(document);
            setConsistent(true);
            LOGGER.info(Messages.getString("BaseLoader.12"), getLabel(), //$NON-NLS-1$
                    document.getToDate());
            LOGGER.trace(getMarker(), Messages.getString("BaseLoader.13"), //$NON-NLS-1$
                    document);
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
        Validate.validState(nonNull(locator),
                Messages.getString("BaseLoader.14")); //$NON-NLS-1$
        Validate.validState(nonNull(document),
                Messages.getString("BaseLoader.15")); //$NON-NLS-1$

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
                    setData(tDocument);
                }
                LOGGER.debug(Messages.getString("BaseLoader.16"), getLabel()); //$NON-NLS-1$
                LOGGER.trace(getMarker(), Messages.getString("BaseLoader.17"), //$NON-NLS-1$
                        Util.LINE, locator);
            }
        } catch (RuntimeException e) {
            String message = Messages.getString("BaseLoader.18"); //$NON-NLS-1$
            throw new StepRunException(message, e);
        }
        return true;
    }

    private boolean persist() {
        // TODO code this and move it to locatorPersistence.persistLocator()
        Optional<Boolean> taskLevelPersistenceDefined =
                Optional.ofNullable(true);
        boolean persist =
                locatorPersistence.persistLocator(taskLevelPersistenceDefined);
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
