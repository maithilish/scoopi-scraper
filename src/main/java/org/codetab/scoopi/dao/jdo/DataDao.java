package org.codetab.scoopi.dao.jdo;

import java.util.List;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.dao.IDataDao;
import org.codetab.scoopi.messages.Messages;
import org.codetab.scoopi.model.Data;

/**
 * <p>
 * JDO DataDao implementation.
 * @author Maithilish
 *
 */
public final class DataDao implements IDataDao {

    /**
     * persistence manager factory.
     */
    private PersistenceManagerFactory pmf;

    /**
     * <p>
     * DataDao constructor.
     * @param pmf
     *            persistence manager factory
     */
    public DataDao(final PersistenceManagerFactory pmf) {
        Validate.notNull(pmf, Messages.getString("DataDao.0")); //$NON-NLS-1$
        this.pmf = pmf;
    }

    /**
     * <p>
     * Store data.
     * @param data
     *            to store
     */
    @Override
    public void storeData(final Data data) {
        Validate.notNull(data, Messages.getString("DataDao.1")); //$NON-NLS-1$

        PersistenceManager pm = getPM();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            pm.makePersistent(data);
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
     * Get detached copy of data by document id and datadef id. These two id
     * forms the unique constraint in data table.
     * @param documentId
     *            document id
     * @param dataDefId
     *            datadef id
     * @return data if no matching data then return null
     * @throws IllegalStateException
     *             if multiple matching is found data
     */
    @Override
    public Data getData(final long documentId, final long dataDefId) {
        PersistenceManager pm = getPM();
        List<Data> data = null;
        try {
            String filter = "documentId == rId && dataDefId == dId"; //$NON-NLS-1$
            String paramDecla = "Long rId,Long dId"; //$NON-NLS-1$
            Extent<Data> extent = pm.getExtent(Data.class);
            Query<Data> query = pm.newQuery(extent, filter);
            query.declareParameters(paramDecla);
            query.setParameters(documentId, dataDefId);
            data = query.executeList();
            pm.getFetchPlan().addGroup("detachMembers"); //$NON-NLS-1$
            data = (List<Data>) pm.detachCopyAll(data);
        } finally {
            pm.close();
        }
        switch (data.size()) {
        case 0:
            return null;
        case 1:
            return data.get(0);
        default:
            throw new IllegalStateException(Messages.getString("DataDao.2") //$NON-NLS-1$
                    + documentId + "][" + dataDefId + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * <p>
     * Get detached copy of data by id.
     * @param id
     *            data id
     * @return data if no matching data then return null
     */
    @Override
    public Data getData(final long id) {
        PersistenceManager pm = getPM();
        Data data = null;
        try {
            Object result = pm.getObjectById(Data.class, id);
            pm.getFetchPlan().addGroup("detachMembers"); //$NON-NLS-1$
            data = (Data) pm.detachCopy(result);
        } finally {
            pm.close();
        }
        return data;
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
