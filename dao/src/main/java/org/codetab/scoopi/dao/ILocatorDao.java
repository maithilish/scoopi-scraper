package org.codetab.scoopi.dao;

import org.codetab.scoopi.model.Locator;

/**
 * <p>
 * LocatorDao interface.
 * @author Maithilish
 *
 */
public interface ILocatorDao {

    /**
     * <p>
     * Store locator.
     * @param locator
     *            locator to store
     */
    void storeLocator(Locator locator);

    /**
     * <p>
     * Get locator by name and group.
     * @param name
     *            locator name
     * @param group
     *            locator group
     * @return locator
     */
    Locator getLocator(String name, String group);

    /**
     * <p>
     * Get locator by id.
     * @param id
     *            locator id
     * @return locator
     */
    Locator getLocator(long id);
}
