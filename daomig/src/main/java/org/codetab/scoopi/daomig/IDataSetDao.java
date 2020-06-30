package org.codetab.scoopi.daomig;

import java.util.List;

import org.codetab.scoopi.model.DataSet;

/**
 * <p>
 * DataSetDao interface.
 * @author Maithilish
 *
 */
public interface IDataSetDao {
    /**
     * <p>
     * Store dataSet.
     * @param dataSet
     *            to store
     */
    void storeDataSets(List<DataSet> dataSets);

}
