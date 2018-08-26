package org.codetab.scoopi.persistence;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

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
            String message = String.join(" ", "unable to load data for",
                    "dataDefId:", String.valueOf(dataDefId), ",documentId:",
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

    /**
     * <p>
     * if scoopi.useDatastore is false, then return false or if
     * </p>
     * <p>
     * scoopi.persist.data is false, then return false
     * </p>
     * <p>
     * otherwise, if taskLevelPersistence is defined then its value and if not
     * then true
     * </p>
     * <p>
     * By default, scoopi persists all model objects. However, user can override
     * this
     * <ul>
     * <li>scoopi.useDatastore=false, don't persist anything</li>
     * <li>scoopi.persist.data=false, don't persist any data</li>
     * <li>task/persist is false, don't persist the data owned by task</li>
     * </ul>
     * </p>
     * @param taskLevelPersistence
     * @return true or false
     */
    public boolean persist(final Optional<Boolean> taskLevelPersistence) {
        if (!configService.useDataStore()) {
            // disabled at global level
            return false;
        }
        if (!configService.isPersist("scoopi.persist.data")) { //$NON-NLS-1$
            // enabled at global but disabled at model level
            return false;
        }
        if (taskLevelPersistence.isPresent()) {
            // enabled at global and model level
            // enabled or disabled at task level
            return taskLevelPersistence.get();
        } else {
            // undefined at task level
            return true;
        }
    }
}
