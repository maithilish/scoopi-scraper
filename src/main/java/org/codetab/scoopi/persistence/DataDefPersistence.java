package org.codetab.scoopi.persistence;

import static java.util.Objects.nonNull;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.dao.DaoFactoryProvider;
import org.codetab.scoopi.dao.IDaoFactory;
import org.codetab.scoopi.dao.IDataDefDao;
import org.codetab.scoopi.dao.ORM;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.messages.Messages;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.shared.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * DataDef persistence and related helper methods.
 * @author Maithilish
 *
 */
public class DataDefPersistence {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(DataDefPersistence.class);

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
     * Loads all active DataDef as on run datetime from store.
     * @return list of active datadef
     * @throws CriticalException
     *             if any persistence error
     */
    public List<DataDef> loadDataDefs() {

        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            IDataDefDao dao = daoFactory.getDataDefDao();
            List<DataDef> dataDefs =
                    dao.getDataDefs(configService.getRunDateTime());
            LOGGER.debug(Messages.getString("DataDefPersistence.1"), //$NON-NLS-1$
                    dataDefs.size());
            return dataDefs;
        } catch (RuntimeException e) {
            throw new CriticalException(
                    Messages.getString("DataDefPersistence.2"), e); //$NON-NLS-1$
        }
    }

    /**
     * <p>
     * Store DataDef.
     * @param dataDef
     *            not null
     * @throws CriticalException
     *             if any persistence error
     */
    public void storeDataDef(final DataDef dataDef) {
        Validate.notNull(dataDef, Messages.getString("DataDefPersistence.3")); //$NON-NLS-1$

        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            IDataDefDao dao = daoFactory.getDataDefDao();
            String name = dataDef.getName();
            LOGGER.debug(Messages.getString("DataDefPersistence.7")); //$NON-NLS-1$
            dao.storeDataDef(dataDef);
            if (nonNull(dataDef.getId())) {
                LOGGER.debug(Messages.getString("DataDefPersistence.8"), //$NON-NLS-1$
                        dataDef.getId(), name);
            }
        } catch (RuntimeException e) {
            throw new CriticalException(
                    Messages.getString("DataDefPersistence.9"), e); //$NON-NLS-1$
        }
    }

    /**
     * <p>
     * Store DataDef.
     * @param dataDefs
     *            not null
     * @throws CriticalException
     *             if any persistence error
     */
    public void storeDataDefs(final List<DataDef> dataDefs) {
        Validate.notNull(dataDefs, Messages.getString("DataDefPersistence.3")); //$NON-NLS-1$

        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            IDataDefDao dao = daoFactory.getDataDefDao();
            for (DataDef dataDef : dataDefs) {
                String name = dataDef.getName();
                LOGGER.debug(Messages.getString("DataDefPersistence.7")); //$NON-NLS-1$
                dao.storeDataDef(dataDef);
                if (nonNull(dataDef.getId())) {
                    LOGGER.debug(Messages.getString("DataDefPersistence.8"), //$NON-NLS-1$
                            dataDef.getId(), name);
                }
            }
        } catch (RuntimeException e) {
            throw new CriticalException(
                    Messages.getString("DataDefPersistence.9"), e); //$NON-NLS-1$
        }
    }

    /**
     * <p>
     * if scoopi.useDatastore is false, then return false or if
     * </p>
     * <p>
     * scoopi.persist.dataDef is false, then return false
     * </p>
     * <p>
     * By default, scoopi persists all model objects. However, user can override
     * this with
     * <ul>
     * <li>scoopi.useDatastore=false, don't persist anything</li>
     * <li>scoopi.persist.dataDef=false, don't persist any locator</li>
     * </ul>
     * </p>
     * *
     * @return true or false
     */
    public boolean persistDataDef() {
        if (!configService.useDataStore()) {
            // disabled at global level
            return false;
        }
        if (!configService.isPersist("scoopi.persist.dataDef")) { //$NON-NLS-1$
            // enabled at global but disabled at model level
            return false;
        }
        return true;
    }
}
