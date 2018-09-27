package org.codetab.scoopi.persistence;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.Optional;

import javax.inject.Inject;

import org.codetab.scoopi.dao.DaoFactoryProvider;
import org.codetab.scoopi.dao.IDaoFactory;
import org.codetab.scoopi.dao.ILocatorDao;
import org.codetab.scoopi.dao.ORM;
import org.codetab.scoopi.exception.StepPersistenceException;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.system.ConfigService;

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
        notNull(name, "name must not be null");
        notNull(group, "group must not be null");

        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            ILocatorDao dao = daoFactory.getLocatorDao();
            Locator existingLocator = dao.getLocator(name, group);
            return existingLocator;
        } catch (RuntimeException e) {
            String message = String.join(" ", "unable to load locator, name:",
                    name, "group:", group);
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
        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            ILocatorDao dao = daoFactory.getLocatorDao();
            return dao.getLocator(id);
        } catch (RuntimeException e) {
            String message = String.join(" ", "unable to load locator, id:",
                    String.valueOf(id));
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
        notNull(locator, "locator must not be null");
        try {
            ORM orm = configService.getOrmType();
            IDaoFactory daoFactory = daoFactoryProvider.getDaoFactory(orm);
            ILocatorDao dao = daoFactory.getLocatorDao();
            dao.storeLocator(locator);
            return true;
        } catch (RuntimeException e) {
            String message = String.join(" ", "unable to store locator, name:",
                    locator.getName(), "group:", locator.getGroup());
            throw new StepPersistenceException(message, e);
        }
    }

    /**
     * <p>
     * if scoopi.useDatastore is false, then return false or if
     * </p>
     * <p>
     * scoopi.persist.locator is false, then return false
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
     * <li>scoopi.persist.locator=false, don't persist any locator</li>
     * <li>locator persist is false, don't persist the locator</li>
     * </ul>
     * </p>
     * @param locatorLevelPersistence
     * @return true or false
     */
    public boolean persist(final Optional<Boolean> locatorLevelPersistence) {
        if (!configService.useDataStore()) {
            // disabled at global level
            return false;
        }
        if (!configService.isPersist("scoopi.persist.locator")) { //$NON-NLS-1$
            // enabled at global but disabled at model level
            return false;
        }
        if (locatorLevelPersistence.isPresent()) {
            // enabled at global and model level
            // enabled or disabled at locator level
            return locatorLevelPersistence.get();
        } else {
            // undefined at locator level
            return true;
        }
    }
}
