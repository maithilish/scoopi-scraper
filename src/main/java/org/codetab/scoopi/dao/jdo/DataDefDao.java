package org.codetab.scoopi.dao.jdo;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.Date;
import java.util.List;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.codetab.scoopi.dao.IDataDefDao;
import org.codetab.scoopi.model.DataDef;

/**
 * <p>
 * JDO DataDef DAO implementation.
 * @author Maithilish
 *
 */
public final class DataDefDao implements IDataDefDao {

    /**
     * JDO PMF.
     */
    private PersistenceManagerFactory pmf;

    /**
     * <p>
     * Constructor.
     * @param pmf
     *            JDO PMF
     */
    public DataDefDao(final PersistenceManagerFactory pmf) {
        notNull(pmf, "pmf must not be null");
        this.pmf = pmf;
    }

    /**
     * <p>
     * Stores datadef.
     * @param datadef
     */
    @Override
    public void storeDataDef(final DataDef dataDef) {
        notNull(dataDef, "dataDef must not be null");

        PersistenceManager pm = getPM();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            pm.makePersistent(dataDef);
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

    /**
     * <p>
     * Get list of active datadef (detached copy) for a date.
     * @param date
     * @return list of datadef
     */
    @Override
    public List<DataDef> getDataDefs(final Date date) {
        notNull(date, "date must not be null");

        List<DataDef> dataDefs = null;
        PersistenceManager pm = getPM();
        try {
            String filter = "fromDate <= pdate && toDate >= pdate"; //$NON-NLS-1$
            String paramDecla = "java.util.Date pdate"; //$NON-NLS-1$
            Extent<DataDef> extent = pm.getExtent(DataDef.class);
            Query<DataDef> query = pm.newQuery(extent, filter);
            query.declareParameters(paramDecla);

            @SuppressWarnings("unchecked")
            List<DataDef> result = (List<DataDef>) query.execute(date);

            pm.getFetchPlan().addGroup("detachDefJson"); //$NON-NLS-1$

            dataDefs = (List<DataDef>) pm.detachCopyAll(result);
        } finally {
            pm.close();
        }
        return dataDefs;
    }

    /**
     * <p>
     * Get list of active datadef (detached copy) of a name.
     * @param name
     *            datadef name
     * @return list of datadef
     */
    @Override
    public List<DataDef> getDataDefs(final String name) {
        notNull(name, "name must not be null");

        List<DataDef> dataDefs = null;
        PersistenceManager pm = getPM();
        try {
            String filter = "name == pname"; //$NON-NLS-1$
            String paramDecla = "String pname"; //$NON-NLS-1$
            Extent<DataDef> extent = pm.getExtent(DataDef.class);
            Query<DataDef> query = pm.newQuery(extent, filter);
            query.declareParameters(paramDecla);

            @SuppressWarnings("unchecked")
            List<DataDef> result = (List<DataDef>) query.execute(name);

            pm.getFetchPlan().addGroup("detachDefJson"); //$NON-NLS-1$

            dataDefs = (List<DataDef>) pm.detachCopyAll(result);
        } finally {
            pm.close();
        }
        return dataDefs;
    }

    /**
     * <p>
     * Get persistence manager from PersistenceManagerFactory.
     * @return persistence manager
     */
    private PersistenceManager getPM() {
        PersistenceManager pm = pmf.getPersistenceManager();
        return pm;
    }
}
