package org.codetab.scoopi.dao;

import java.time.ZonedDateTime;

import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.Fingerprint;

public interface IDocumentDao {

    Fingerprint save(Fingerprint dir, Document document) throws DaoException;

    Document get(Fingerprint dir) throws DaoException, ChecksumException;

    ZonedDateTime getDocumentDate(Fingerprint dir) throws DaoException;

    void delete(Fingerprint dir) throws DaoException;

}
