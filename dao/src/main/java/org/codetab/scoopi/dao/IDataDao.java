package org.codetab.scoopi.dao;

import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.Fingerprint;

public interface IDataDao {

    Data get(Fingerprint dir, Fingerprint file)
            throws DaoException, ChecksumException;

    void save(Fingerprint dir, Fingerprint file, Data data) throws DaoException;

    void delete(Fingerprint locatorId, Fingerprint dataFingerprint)
            throws DaoException;
}
