package org.codetab.scoopi.dao;

import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.Fingerprint;

public interface IDocumentDao {

    Document get(String dirName, String fileName) throws DaoException;

    void delete(String dirName, String fileName) throws DaoException;

    Fingerprint save(String dirName, Document document) throws DaoException;

}
