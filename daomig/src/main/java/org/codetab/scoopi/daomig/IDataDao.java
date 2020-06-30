package org.codetab.scoopi.daomig;

import org.codetab.scoopi.model.Data;

/**
 * <p>
 * DataDao interface.
 * @author Maithilish
 *
 */
public interface IDataDao {
    /**
     * <p>
     * Store data.
     * @param data
     *            to store
     */
    void storeData(Data data);

    /**
     * <p>
     * Get Data from document id and datadef id.
     * @param documentId
     *            document id
     * @param dataDefId
     *            datadef id
     * @return data
     */
    Data getData(long documentId, long dataDefId);

    /**
     * <p>
     * Get Data from data id.
     * @param id
     *            data id.
     * @return data
     */
    Data getData(long id);

}
