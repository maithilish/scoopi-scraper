package org.codetab.scoopi.daomig;

import javax.inject.Inject;

import org.codetab.scoopi.di.DInjector;

/**
 * <p>
 * Provides concrete implementation of IDaoFactory for an ORM type.
 * <p>
 * It defines a Simple Factory method which creates DaoFactory of ORM type which
 * in turn uses Abstract Factory Pattern to create a families of DAO.
 * @author Maithilish
 *
 */
public class DaoFactoryProvider {

    /**
     * DI.
     */
    @Inject
    private DInjector dInjector;

    /**
     * DaoFactory constructor.
     * <p>
     *
     */
    @Inject
    public DaoFactoryProvider() {
    }

    /**
     * <p>
     * Get instance of DaoFactory of ORM type. At present, only JDO is
     * supported.
     * @param orm
     *            type JDO, JPA or DEFAULT
     * @return DaoFactory of JDO type
     * @throws UnsupportedOperationException
     *             if ORM type is JPA
     */
    public IDaoFactory getDaoFactory(final ORM orm) {
        IDaoFactory instance = null;
        switch (orm) {
        case JDO:
            instance = dInjector.instance(
                    org.codetab.scoopi.daomig.jdo.JdoDaoFactory.class);
            break;
        case JPA:
            throw new UnsupportedOperationException("JPA not yet supported");
        default:
            instance = dInjector.instance(
                    org.codetab.scoopi.daomig.jdo.JdoDaoFactory.class);
            break;
        }
        return instance;
    }

}
