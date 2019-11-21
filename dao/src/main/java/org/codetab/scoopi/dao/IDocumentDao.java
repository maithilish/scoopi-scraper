package org.codetab.scoopi.dao;

import org.codetab.scoopi.model.Document;

/**
 * <p>
 * DocumentDao interface.
 * @author Maithilish
 *
 */
public interface IDocumentDao {

    /**
     * <p>
     * Get Document by id.
     * @param id
     *            document id
     * @return document
     */
    Document getDocument(long id);

}
