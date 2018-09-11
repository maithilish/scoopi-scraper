package org.codetab.scoopi.persistence;

import javax.inject.Inject;

import org.codetab.scoopi.dao.DaoFactoryProvider;
import org.codetab.scoopi.dao.IDaoFactory;
import org.codetab.scoopi.dao.IDocumentDao;
import org.codetab.scoopi.dao.ORM;
import org.codetab.scoopi.exception.StepPersistenceException;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.system.ConfigService;

/**
 * <p>
 * Document persistence methods.
 * @author Maithilish
 *
 */
public class DocumentPersistence {

    /**
     * Config service.
     */
    @Inject
    private ConfigService configService;
    /**
     * DaoFactory provider.
     */
    @Inject
    private DaoFactoryProvider daoFactoryProvider;

    /**
     * <p>
     * Loads document with documentObject by id
     * @param id
     *            document id
     * @return document or null if not found
     * @throws StepPersistenceException
     *             if persistence error
     */
    public Document loadDocument(final long id) {
        // get Document with documentObject
        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            IDocumentDao dao = daoFactory.getDocumentDao();
            return dao.getDocument(id);
        } catch (RuntimeException e) {
            String message = String.join(" ", "unable to load document, id:",
                    String.valueOf(id));
            throw new StepPersistenceException(message, e);
        }
    }
}
