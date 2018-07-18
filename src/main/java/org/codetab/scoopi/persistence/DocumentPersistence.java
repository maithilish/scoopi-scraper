package org.codetab.scoopi.persistence;

import javax.inject.Inject;

import org.codetab.scoopi.dao.DaoFactoryProvider;
import org.codetab.scoopi.dao.IDaoFactory;
import org.codetab.scoopi.dao.IDocumentDao;
import org.codetab.scoopi.dao.ORM;
import org.codetab.scoopi.exception.StepPersistenceException;
import org.codetab.scoopi.messages.Messages;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.shared.ConfigService;
import org.codetab.scoopi.util.Util;

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
     * Loads document by id.
     * @param id
     *            document id
     * @return document or null if not found
     * @throws StepPersistenceException
     *             if persistence error
     */
    public Document loadDocument(final long id) {
        if (!configService.useDataStore()) { // $NON-NLS-1$
            return null;
        }

        // get Document with documentObject
        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            IDocumentDao dao = daoFactory.getDocumentDao();
            return dao.getDocument(id);
        } catch (RuntimeException e) {
            String message =
                    Util.join(Messages.getString("DocumentPersistence.1"), //$NON-NLS-1$
                            String.valueOf(id), "]"); //$NON-NLS-1$
            throw new StepPersistenceException(message, e);
        }
    }

}
