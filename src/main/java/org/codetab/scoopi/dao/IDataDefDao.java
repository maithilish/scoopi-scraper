package org.codetab.scoopi.dao;

import java.util.Date;
import java.util.List;

import org.codetab.scoopi.model.DataDef;

/**
 * <p>
 * DataDefDao interface.
 * @author Maithilish
 *
 */
public interface IDataDefDao {

    /**
     * <p>
     * Store DataDef.
     * @param dataDef
     *            datadef to store
     */
    void storeDataDef(DataDef dataDef);

    /**
     * <p>
     * Get list of datadef for a date.
     * @param date
     *            date
     * @return list of datadef
     */
    List<DataDef> getDataDefs(Date date);

    /**
     * <p>
     * Get list of datadef for a name includes active as well as previous
     * versions.
     * @param name
     *            datadef name
     * @return list of datadef
     */
    List<DataDef> getDataDefs(String name);
}
