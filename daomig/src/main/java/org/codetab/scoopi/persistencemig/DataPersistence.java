package org.codetab.scoopi.persistencemig;

import static org.apache.commons.lang3.Validate.notNull;
import static org.codetab.scoopi.util.Util.spaceit;

import java.util.Optional;

import javax.inject.Inject;

import org.codetab.scoopi.daomig.ConfigHelper;
import org.codetab.scoopi.daomig.DaoFactoryProvider;
import org.codetab.scoopi.daomig.IDaoFactory;
import org.codetab.scoopi.daomig.IDataDao;
import org.codetab.scoopi.daomig.ORM;
import org.codetab.scoopi.exception.StepPersistenceException;
import org.codetab.scoopi.model.Data;

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
    private ConfigHelper configHelper;

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
            ORM orm = configHelper.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            IDataDao dao = daoFactory.getDataDao();
            Data data = dao.getData(documentId, dataDefId);
            return data;
        } catch (RuntimeException e) {
            String message = spaceit("unable to load data, dataDefId:",
                    String.valueOf(dataDefId), ", documentId:",
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
            ORM orm = configHelper.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            IDataDao dao = daoFactory.getDataDao();
            Data data = dao.getData(id);
            return data;
        } catch (RuntimeException e) {
            String message =
                    spaceit("unable to load data, id:", String.valueOf(id));
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
        notNull(data, "data must not be null");

        try {
            ORM orm = configHelper.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            IDataDao dao = daoFactory.getDataDao();
            dao.storeData(data);
            return true;
        } catch (RuntimeException e) {
            String message = spaceit("unable to store data:", data.getName());
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
     * <li>task/persist/data is false, don't persist the data owned by the
     * task</li>
     * </ul>
     * </p>
     * @param taskLevelPersistence
     * @return true or false
     */
    public boolean persist(final Optional<Boolean> taskLevelPersistence) {
        if (!configHelper.useDataStore()) {
            // disabled at global level
            return false;
        }
        if (!configHelper.isPersist("scoopi.persist.data")) { //$NON-NLS-1$
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
