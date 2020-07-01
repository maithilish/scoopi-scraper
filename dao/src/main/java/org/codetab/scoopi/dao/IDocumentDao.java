package org.codetab.scoopi.dao;

import java.util.Date;

import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.Fingerprint;

public interface IDocumentDao {

    Fingerprint save(Fingerprint dir, Document document) throws DaoException;

    Document get(Fingerprint dir) throws DaoException, ChecksumException;

    Date getDocumentDate(Fingerprint dir) throws DaoException;

    void delete(Fingerprint dir) throws DaoException;

}
