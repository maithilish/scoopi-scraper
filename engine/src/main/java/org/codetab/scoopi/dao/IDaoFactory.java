package org.codetab.scoopi.dao;

/**
 * <p>
 * DaoFactory applies Abstract Factory Pattern to provide an interface for
 * creating families of DAO such as IDocumentDao, ILocatorDao etc., without
 * specifying their concrete classes.
 * <p>
 * Interface that defines how to make family of related products without
 * specifying their concrete classes.
 * @author Maithilish
 *
 */
public interface IDaoFactory {

    /**
     * <p>
     * Subclass should override this.
     * @return locatorDao
     */
    ILocatorDao getLocatorDao();

    /**
     * <p>
     * Subclass should override this.
     * @return documentDao
     */
    IDocumentDao getDocumentDao();

    /**
     * <p>
     * Subclass should override this.
     * @return dataDefDao
     */
    IDataDefDao getDataDefDao();

    /**
     * <p>
     * Subclass should override this.
     * @return dataDao
     */
    IDataDao getDataDao();

    /**
     * <p>
     * Subclass should override this.
     * @return dataSetDao
     */
    IDataSetDao getDataSetDao();
}
