package org.codetab.scoopi.persistence;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.notNull;
import static org.codetab.scoopi.util.Util.spaceit;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import org.apache.commons.lang3.time.DateUtils;
import org.codetab.scoopi.dao.ConfigHelper;
import org.codetab.scoopi.dao.DaoFactoryProvider;
import org.codetab.scoopi.dao.IDaoFactory;
import org.codetab.scoopi.dao.IDataDefDao;
import org.codetab.scoopi.dao.ORM;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.model.DataDef;
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
    private ConfigHelper configHelper;

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
            ORM orm = configHelper.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            IDataDefDao dao = daoFactory.getDataDefDao();
            List<DataDef> dataDefs =
                    dao.getDataDefs(configHelper.getRunDateTime());
            LOGGER.debug("dataDefs loaded {}", dataDefs.size());
            return dataDefs;
        } catch (RuntimeException e) {
            throw new CriticalException("unable to load dataDef", e);
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
        notNull(dataDef, "dataDef must not be null");

        try {
            ORM orm = configHelper.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            IDataDefDao dao = daoFactory.getDataDefDao();
            String name = dataDef.getName();
            LOGGER.debug("store datadef");
            dao.storeDataDef(dataDef);
            if (nonNull(dataDef.getId())) {
                LOGGER.debug("stored datadef: {}, id: {}", name,
                        dataDef.getId());
            }
        } catch (RuntimeException e) {
            String message =
                    spaceit("unable to store dataDef:", dataDef.getName());
            throw new CriticalException(message, e);
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
        notNull(dataDefs, "dataDefs must not be null");

        try {
            ORM orm = configHelper.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            IDataDefDao dao = daoFactory.getDataDefDao();
            for (DataDef dataDef : dataDefs) {
                String name = dataDef.getName();
                LOGGER.debug("store datadef");
                dao.storeDataDef(dataDef);
                if (nonNull(dataDef.getId())) {
                    LOGGER.debug("stored datadef: {}, id: {}", name,
                            dataDef.getId());
                }
            }
        } catch (RuntimeException e) {
            throw new CriticalException("unable to store dataDefs", e);
        }
    }

    /**
     * <p>
     * Compares active and new list of datadef and if any active datadef has a
     * new datadef version, then the active datadef highDate is reset to
     * runDateTime and new datadef is added to active list. For any new datadef,
     * there is no active datadef, then the new datadef is added active list.
     * Later updated active list is persisted to store.
     * @param dataDefs
     *            active list - existing active datadefs, not null
     * @param newDataDefs
     *            list of new datadefs, not null
     * @return true if active list is modified
     */
    public boolean markForUpdation(final List<DataDef> newDataDefs,
            final List<DataDef> oldDataDefs) {
        notNull(newDataDefs, "newDataDefs must not be null");
        notNull(oldDataDefs, "oldDataDefs must not be null");

        boolean updates = false;
        for (DataDef newDataDef : newDataDefs) {
            String name = newDataDef.getName();
            String message = null;
            try {
                DataDef oldDataDef = oldDataDefs.stream()
                        .filter(e -> e.getName().equals(name)).findFirst()
                        .get();
                if (oldDataDef.equalsForDef(newDataDef)) {
                    // no change
                    message = "no changes";
                } else {
                    // changed - update old and insert changed
                    message = "changed, insert new version";
                    updates = true;
                    Date toDate = DateUtils
                            .addSeconds(configHelper.getRunDateTime(), -1);
                    oldDataDef.setToDate(toDate);
                    oldDataDefs.add(newDataDef);
                }
            } catch (NoSuchElementException e) {
                // not exists - add new
                message = "not in store, insert new version";
                updates = true;
                oldDataDefs.add(newDataDef);
            }
            LOGGER.info("dataDef: {}, {}", name, message);
        }
        return updates;
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
     * <li>scoopi.persist.dataDef=false, don't persist any dataDef</li>
     * </ul>
     * </p>
     * *
     * @return true or false
     */
    public boolean persistDataDef() {
        if (!configHelper.useDataStore()) {
            // disabled at global level
            return false;
        }
        if (!configHelper.isPersist("scoopi.persist.dataDef")) { //$NON-NLS-1$
            // enabled at global but disabled at model level
            return false;
        }
        return true;
    }
}
