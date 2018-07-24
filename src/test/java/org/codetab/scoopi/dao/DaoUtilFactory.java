package org.codetab.scoopi.dao;

import org.codetab.scoopi.dao.jdo.JdoDaoUtilFactory;

/**
 * <p>
 * DaoUtilFactory for tests.
 * @author Maithilish
 *
 */
public abstract class DaoUtilFactory {

    /**
     * instance.
     */
    private static DaoUtilFactory instance;

    /**
     * <p>
     * Get UtilDao.
     * @return IDaoUtil
     */
    public abstract IDaoUtil getUtilDao();

    /**
     * <p>
     * get Dao factory.
     * @param orm
     *            ORM type
     * @return daoUtilFactory
     */
    public static DaoUtilFactory getDaoFactory(final ORM orm) {
        if (instance == null) {
            switch (orm) {
            case JDO:
                instance = new JdoDaoUtilFactory();
                break;
            default:
                instance = new JdoDaoUtilFactory();
                break;
            }
        }
        return instance;
    }

    /**
     * <p>
     * Get ORM type.
     * @param ormName
     *            ORM type string
     * @return ORM type.
     */
    public static ORM getOrmType(final String ormName) {
        ORM orm = null;
        if (ormName == null) {
            orm = ORM.JDO;
        }
        if (ormName.toUpperCase().equals("JDO")) {
            orm = ORM.JDO;
        }
        return orm;
    }

}
