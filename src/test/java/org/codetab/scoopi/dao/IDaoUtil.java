package org.codetab.scoopi.dao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import javax.jdo.PersistenceManagerFactory;

/**
 * <p>
 * Interface to IDaoUtil.
 * @author Maithilish
 *
 */
public interface IDaoUtil {

    /**
     * <p>
     * Get DB config.
     * @return properties
     * @throws IOException
     *             exception
     */
    Properties getDbConfig() throws IOException;

    /**
     * <p>
     * Execute DB query.
     * @param pmf
     *            persistence manager factory
     * @param query
     *            query string
     * @throws SQLException
     *             on error
     */
    void executeQuery(PersistenceManagerFactory pmf, String query)
            throws SQLException;

    /**
     * <p>
     * Drop DB constraints.
     * @param pmf
     *            persistence manager factory
     * @param table
     *            db table
     * @param constraint
     *            db constraint
     * @throws SQLException
     *             on error
     */
    void dropConstraint(PersistenceManagerFactory pmf, String table,
            String constraint) throws SQLException;

    /**
     * <p>
     * Delete tables for classes.
     * @param schemaClasses
     *            set of classes
     */
    void deleteSchemaForClasses(HashSet<String> schemaClasses);

    /**
     * <p>
     * Create tables for classes.
     * @param schemaClasses
     *            set of classes
     */
    void createSchemaForClasses(HashSet<String> schemaClasses);

    /**
     * <p>
     * Clear DB cache.
     *
     */
    void clearCache();

    /**
     * <p>
     * Get Object of a class type.
     * @param <T>
     *            of class type
     * @param ofClass
     *            objects of classes
     * @param detachGroups
     *            detach groups
     * @return list of objects
     */
    <T> List<T> getObjects(Class<T> ofClass, List<String> detachGroups);

    /**
     * <p>
     * Get Persistence Manager Factory.
     * @return Persistence Manager Factory
     */
    PersistenceManagerFactory getPersistenceManagerFactory();

}
