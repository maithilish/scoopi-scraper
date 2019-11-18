package org.codetab.scoopi.dao.jdo;

import javax.jdo.PersistenceManagerFactory;

import org.codetab.scoopi.dao.IDaoUtil;
import org.codetab.scoopi.di.DInjector;

/**
 * <p>
 * JDO DaoUtilFactory for tests.
 * @author Maithilish
 *
 */
public final class JdoDaoUtilFactory {

    /**
     * pmf.
     */
    private PersistenceManagerFactory pmf;

    /**
     * <p>
     * Constructor.
     *
     */
    public JdoDaoUtilFactory(final DInjector dInjector) {
        PMF p = dInjector.instance(PMF.class);
        p.init();
        pmf = p.getFactory();
    }

    /**
     * <p>
     * Get PMF.
     * @return PMF
     */
    public PersistenceManagerFactory getFactory() {
        return pmf;
    }

    public IDaoUtil getUtilDao() {
        return new JdoDaoUtil(pmf);
    }
}
