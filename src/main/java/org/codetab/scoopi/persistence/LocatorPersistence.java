package org.codetab.scoopi.persistence;

import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.dao.DaoFactoryProvider;
import org.codetab.scoopi.dao.IDaoFactory;
import org.codetab.scoopi.dao.ILocatorDao;
import org.codetab.scoopi.dao.ORM;
import org.codetab.scoopi.exception.StepPersistenceException;
import org.codetab.scoopi.messages.Messages;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.shared.ConfigService;
import org.codetab.scoopi.util.Util;

/**
 * <p>
 * Locator Persistence methods.
 * @author Maithilish
 *
 */
public class LocatorPersistence {

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
     * Loads locator by name and group.
     * @param name
     *            locator name, not null
     * @param group
     *            locator group, not null
     * @return locator
     * @throws StepPersistenceException
     *             if persistence error
     */
    public Locator loadLocator(final String name, final String group) {
        Validate.notNull(name, Messages.getString("LocatorPersistence.0")); //$NON-NLS-1$
        Validate.notNull(group, Messages.getString("LocatorPersistence.1")); //$NON-NLS-1$

        if (!configService.useDataStore()) { // $NON-NLS-1$
            return null;
        }

        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            ILocatorDao dao = daoFactory.getLocatorDao();
            Locator existingLocator = dao.getLocator(name, group);
            return existingLocator;
        } catch (RuntimeException e) {
            String message =
                    Util.join(Messages.getString("LocatorPersistence.3"), name, //$NON-NLS-1$
                            ":", group, "]"); //$NON-NLS-1$ //$NON-NLS-2$
            throw new StepPersistenceException(message, e);
        }
    }

    /**
     * <p>
     * Loads locator by id.
     * @param id
     *            id of locator to load, not null
     * @return locator
     * @throws StepPersistenceException
     *             if persistence error
     */
    public Locator loadLocator(final long id) {

        if (!configService.useDataStore()) { // $NON-NLS-1$
            return null;
        }

        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            ILocatorDao dao = daoFactory.getLocatorDao();
            return dao.getLocator(id);
        } catch (RuntimeException e) {
            String message =
                    Util.join(Messages.getString("LocatorPersistence.7"), //$NON-NLS-1$
                            String.valueOf(id), "]"); //$NON-NLS-1$
            throw new StepPersistenceException(message, e);
        }
    }

    /**
     * <p>
     * Store locator.
     * @param locator
     *            locator to store, not null
     * @throws StepPersistenceException
     *             if persistence error
     */
    public boolean storeLocator(final Locator locator) {
        Validate.notNull(locator, Messages.getString("LocatorPersistence.9")); //$NON-NLS-1$

        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            ILocatorDao dao = daoFactory.getLocatorDao();
            dao.storeLocator(locator);
            return true;
        } catch (RuntimeException e) {
            String message =
                    Util.join(Messages.getString("LocatorPersistence.13"), //$NON-NLS-1$
                            locator.getName(), ":", locator.getGroup(), "]"); //$NON-NLS-1$//$NON-NLS-2$
            throw new StepPersistenceException(message, e);
        }
    }

    public boolean persistLocator(
            final Optional<Boolean> taskLevelPersistence) {
        if (!configService.useDataStore()) { // $NON-NLS-1$
            return false;
        }

        boolean persist = configService.isPersist("scoopi.persist.locator"); //$NON-NLS-1$

        if (taskLevelPersistence.isPresent()) {
            persist = taskLevelPersistence.get();
        }

        return persist;
    }
}
