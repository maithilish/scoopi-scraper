package org.codetab.scoopi.dao;

import org.codetab.scoopi.model.Fingerprint;
import org.codetab.scoopi.model.Metadata;

public interface IMetadataDao {

    Metadata get(String id) throws DaoException;

    Fingerprint save(String id, Metadata t) throws DaoException;

    void delete(String id) throws DaoException;

    void remove(String id) throws DaoException;

}
