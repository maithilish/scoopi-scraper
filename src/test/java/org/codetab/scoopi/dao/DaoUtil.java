package org.codetab.scoopi.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.datastore.DataStoreCache;
import javax.jdo.datastore.JDOConnection;

import org.codetab.scoopi.dao.jdo.PMF;
import org.datanucleus.PersistenceNucleusContext;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.store.schema.SchemaAwareStoreManager;

/**
 * <p>
 * DaoUtil for tests.
 * @author Maithilish
 *
 */
public class DaoUtil implements IDaoUtil {

    /**
     * pmf.
     */
    private PersistenceManagerFactory pmf;

    /**
     * <p>
     * Constructor.
     * @param pmf
     *            pmf
     */
    public DaoUtil(final PersistenceManagerFactory pmf) {
        this.pmf = pmf;
    }

    @Override
    public PersistenceManagerFactory getPersistenceManagerFactory() {
        return pmf;
    }

    @Override
    public void dropConstraint(final PersistenceManagerFactory pmfac,
            final String table, final String constraint) throws SQLException {
        String query = null;
        String driver = pmfac.getConnectionDriverName().toLowerCase();
        if (driver.contains("hsqldb")) {
            query = MessageFormat.format("alter table {0} drop constraint {1}",
                    table, constraint);
        }
        if (driver.contains("mysql") || driver.contains("mariadb")) {
            query = MessageFormat.format("alter table {0} drop index {1}",
                    table.toUpperCase(), constraint.toUpperCase());
        }
        executeQuery(pmfac, query);
    }

    @Override
    public void executeQuery(final PersistenceManagerFactory pmfac,
            final String query) throws SQLException {
        PersistenceManager pm = pmfac.getPersistenceManager();
        JDOConnection jdoCon = pm.getDataStoreConnection();
        Connection connection = (Connection) jdoCon.getNativeConnection();
        Statement stmt = connection.createStatement();
        stmt.execute(query);
        connection.close();
    }

    @Override
    public Properties getDbConfig() throws IOException {
        Properties jdoProperties = new Properties();

        String propFileName = "jdoconfig.properties";
        InputStream inputStream =
                PMF.class.getClassLoader().getResourceAsStream(propFileName);

        jdoProperties.load(inputStream);
        inputStream.close();

        return jdoProperties;
    }

    @Override
    public void deleteSchemaForClasses(final HashSet<String> schemaClasses) {
        PersistenceNucleusContext ctx =
                ((JDOPersistenceManagerFactory) pmf).getNucleusContext();
        SchemaAwareStoreManager storeManager =
                ((SchemaAwareStoreManager) ctx.getStoreManager());
        storeManager.deleteSchemaForClasses(schemaClasses, new Properties());
    }

    @Override
    public void createSchemaForClasses(final HashSet<String> schemaClasses) {
        PersistenceNucleusContext ctx =
                ((JDOPersistenceManagerFactory) pmf).getNucleusContext();
        SchemaAwareStoreManager storeManager =
                ((SchemaAwareStoreManager) ctx.getStoreManager());
        storeManager.createSchemaForClasses(schemaClasses, new Properties());
    }

    @Override
    public void clearCache() {
        DataStoreCache cache = pmf.getDataStoreCache();
        cache.evictAll();
    }

    @Override
    public <T> List<T> getObjects(final Class<T> ofClass,
            final List<String> detachGroups) {
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            Extent<T> extent = pm.getExtent(ofClass);
            Query<T> query = pm.newQuery(extent);

            @SuppressWarnings("unchecked")
            List<T> list = (List<T>) query.execute();
            detachGroups.stream().forEach(pm.getFetchPlan()::addGroup);
            return (List<T>) pm.detachCopyAll(list);
        } finally {
            pm.close();
        }
    }

}
