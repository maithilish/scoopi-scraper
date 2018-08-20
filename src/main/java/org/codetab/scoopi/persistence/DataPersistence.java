package org.codetab.scoopi.persistence;

import static java.util.Objects.requireNonNull;

import javax.inject.Inject;

import org.codetab.scoopi.dao.DaoFactoryProvider;
import org.codetab.scoopi.dao.IDaoFactory;
import org.codetab.scoopi.dao.IDataDao;
import org.codetab.scoopi.dao.ORM;
import org.codetab.scoopi.exception.StepPersistenceException;
import org.codetab.scoopi.messages.Messages;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.shared.ConfigService;
import org.codetab.scoopi.util.Util;

/**
 * <p>
 * Data Persistence methods.
 * @author Maithilish
 *
 */
public class DataPersistence {

    /**
     * ConfigService.
     */
    @Inject
    private ConfigService configService;

    /**
     * DaoFactoryProvider.
     */
    @Inject
    private DaoFactoryProvider daoFactoryProvider;

    /**
     * <p>
     * Load Data from store by datadef id and document id.
     * @param dataDefId
     *            datadef id
     * @param documentId
     *            document id
     * @return data or null
     * @throws StepPersistenceException
     *             on persistence error
     */
    public Data loadData(final long dataDefId, final long documentId) {
        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            IDataDao dao = daoFactory.getDataDao();
            Data data = dao.getData(documentId, dataDefId);
            return data;
        } catch (RuntimeException e) {
            String message = Util.join(Messages.getString("DataPersistence.1"), //$NON-NLS-1$
                    "dataDefId=", //$NON-NLS-1$
                    String.valueOf(dataDefId), ",documentId=", //$NON-NLS-1$
                    String.valueOf(documentId));
            throw new StepPersistenceException(message, e);
        }
    }

    /**
     * <p>
     * Load Data from store by id.
     * @param id
     *            data id
     * @return data or null
     * @throws StepPersistenceException
     *             on persistence error
     */
    public Data loadData(final long id) {
        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            IDataDao dao = daoFactory.getDataDao();
            Data data = dao.getData(id);
            return data;
        } catch (RuntimeException e) {
            String message =
                    Util.join(Messages.getString("DataPersistence.5"), "id=", //$NON-NLS-1$ //$NON-NLS-2$
                            String.valueOf(id));
            throw new StepPersistenceException(message, e);
        }
    }

    /**
     * <p>
     * Store data.
     * @param data
     *            data to store, not null
     * @param fields
     * @throws StepPersistenceException
     *             on persistence error
     */
    public boolean storeData(final Data data) {
        requireNonNull(data, Messages.getString("DataPersistence.7")); //$NON-NLS-1$

        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            IDataDao dao = daoFactory.getDataDao();
            dao.storeData(data);
            return true;
        } catch (RuntimeException e) {
            String message = Util.join(Messages.getString("DataPersistence.12"), //$NON-NLS-1$
                    data.getName(), "]"); //$NON-NLS-1$
            throw new StepPersistenceException(message, e);
        }
    }
}
