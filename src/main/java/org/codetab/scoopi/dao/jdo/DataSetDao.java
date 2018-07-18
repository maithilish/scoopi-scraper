package org.codetab.scoopi.dao.jdo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.dao.IDataSetDao;
import org.codetab.scoopi.exception.StepPersistenceException;
import org.codetab.scoopi.messages.Messages;
import org.codetab.scoopi.model.DataSet;
import org.codetab.scoopi.util.Util;

/**
 * <p>
 * JDO DataSetDao implementation.
 * @author Maithilish
 *
 */
public final class DataSetDao implements IDataSetDao {

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
    public DataSetDao(final PersistenceManagerFactory pmf) {
        Validate.notNull(pmf, Messages.getString("DataSetDao.0")); //$NON-NLS-1$
        this.pmf = pmf;
    }

    /**
     * <p>
     * Store dataSet. If name and group are not unique then dataSets is not
     * persisted and exception is thrown. Otherwise, it compares existing
     * dataSets with the input dataSets and creates a list of items which don't
     * exists in store. Finally, the created list is bulk persisted.
     * @param dataSets
     *            list to store
     * @throws StepPersistenceException
     *             if name and group is not unique
     */
    @Override
    public void storeDataSets(final List<DataSet> dataSets) {
        Validate.notNull(dataSets, Messages.getString("DataSetDao.1")); //$NON-NLS-1$

        // TODO filter on col or row
        List<String> names = dataSets.stream().map(DataSet::getName).distinct()
                .collect(Collectors.toList());
        List<String> groups = dataSets.stream().map(DataSet::getGroup)
                .distinct().collect(Collectors.toList());

        if (names.size() != 1 || groups.size() != 1) {
            String message = Util.join(Messages.getString("DataSetDao.2"), //$NON-NLS-1$
                    names.toString(), groups.toString());
            throw new StepPersistenceException(message);
        }

        String name = names.get(0);
        String group = groups.get(0);

        List<DataSet> pDataSets = new ArrayList<>();

        PersistenceManager pm = getPM();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            String filter = "name == pname && group == pgroup"; //$NON-NLS-1$
            String paramDecla = "String pname,String pgroup"; //$NON-NLS-1$
            Extent<DataSet> extent = pm.getExtent(DataSet.class);
            Query<DataSet> query = pm.newQuery(extent, filter);
            query.declareParameters(paramDecla);
            query.setParameters(name, group);
            List<DataSet> oDataSets = query.executeList();
            oDataSets = (List<DataSet>) pm.detachCopyAll(oDataSets);

            for (DataSet dataSet : dataSets) {
                if (!oDataSets.contains(dataSet)) {
                    pDataSets.add(dataSet);
                }
            }

            for (DataSet dataSet : pDataSets) {
                pm.makePersistent(dataSet);
            }
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
     * Get persistence manager from PersistenceManagerFactory.
     * @return persistence manager
     */
    private PersistenceManager getPM() {
        PersistenceManager pm = pmf.getPersistenceManager();
        return pm;
    }
}
