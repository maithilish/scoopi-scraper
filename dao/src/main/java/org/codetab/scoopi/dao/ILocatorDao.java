package org.codetab.scoopi.dao;

import org.codetab.scoopi.model.Fingerprint;
import org.codetab.scoopi.model.Locator;

public interface ILocatorDao {

    Locator get(String dirName, String fileName) throws DaoException;

    void delete(String dirName, String fileName) throws DaoException;

    Fingerprint save(String dirName, Locator locator) throws DaoException;

}
