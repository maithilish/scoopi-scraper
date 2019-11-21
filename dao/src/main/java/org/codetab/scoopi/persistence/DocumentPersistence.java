package org.codetab.scoopi.persistence;

import static org.codetab.scoopi.util.Util.spaceit;

import javax.inject.Inject;

import org.codetab.scoopi.dao.ConfigHelper;
import org.codetab.scoopi.dao.DaoFactoryProvider;
import org.codetab.scoopi.dao.IDaoFactory;
import org.codetab.scoopi.dao.IDocumentDao;
import org.codetab.scoopi.dao.ORM;
import org.codetab.scoopi.exception.StepPersistenceException;
import org.codetab.scoopi.model.Document;

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
    private ConfigHelper configService;
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
            String message =
                    spaceit("unable to load document, id:", String.valueOf(id));
            throw new StepPersistenceException(message, e);
        }
    }
}
