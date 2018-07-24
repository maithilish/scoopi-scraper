package org.codetab.scoopi.dao.jdo;

import javax.jdo.PersistenceManagerFactory;

import org.codetab.scoopi.dao.DaoUtil;
import org.codetab.scoopi.dao.DaoUtilFactory;
import org.codetab.scoopi.dao.IDaoUtil;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.shared.ConfigService;

/**
 * <p>
 * JDO DaoUtilFactory for tests.
 * @author Maithilish
 *
 */
public final class JdoDaoUtilFactory extends DaoUtilFactory {

    /**
     * pmf.
     */
    private PersistenceManagerFactory pmf;

    /**
     * <p>
     * Constructor.
     *
     */
    public JdoDaoUtilFactory() {
        String userProvidedFile = "gotz.properties";
        String defaultsFile = "gotz-default.xml";
        DInjector dInjector = new DInjector();
        ConfigService configService = dInjector.instance(ConfigService.class);
        configService.init(userProvidedFile, defaultsFile);
        PMF jdoPMF = dInjector.instance(PMF.class);
        jdoPMF.init();
        pmf = jdoPMF.getFactory();
    }

    /**
     * <p>
     * Get PMF.
     * @return PMF
     */
    public PersistenceManagerFactory getFactory() {
        return pmf;
    }

    @Override
    public IDaoUtil getUtilDao() {
        return new DaoUtil(pmf);
    }

}
