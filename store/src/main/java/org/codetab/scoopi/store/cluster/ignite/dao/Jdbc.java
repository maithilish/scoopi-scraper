package org.codetab.scoopi.store.cluster.ignite.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.inject.Inject;

import org.apache.commons.dbcp2.BasicDataSource;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Not singleton. Each dao (jobDao, keyStoreDao) gets an instance as data source
 * close may affect others
 * @author m
 *
 */
public class Jdbc {

    static final Logger LOGGER = LoggerFactory.getLogger(Jdbc.class);

    @Inject
    private Configs configs;
    @Inject
    private BasicDataSource ds;

    private int maxTry = 1;

    public boolean init() {
        try {
            maxTry = Integer.parseInt(
                    configs.getConfig("scoopi.cluster.datasource.sqlRetry"));
        } catch (NumberFormatException | ConfigNotFoundException e) {
            maxTry = 1;
        }
        try {
            ds.setDriverClassName(
                    configs.getConfig("scoopi.cluster.datasource.driver"));
            ds.setUrl(configs.getConfig("scoopi.cluster.datasource.url"));
            return true;
        } catch (final ConfigNotFoundException e) {
            throw new CriticalException("unable to connect to Ignite", e);
        }
    }

    public boolean close() {
        try {
            ds.close();
            LOGGER.info("Ignite connection pool closed");
            return true;
        } catch (final SQLException e) {
            throw new CriticalException("close Ignite connection pool", e);
        }
    }

    // select methods: result set
    public <R> R select(final String sql,
            final SqlFunction<ResultSet, R> sqlFunc) throws SQLException {
        SQLException ex = null;
        for (int i = 1; i <= maxTry; i++) {
            try (Connection conn = ds.getConnection()) {
                return select(conn, sql, sqlFunc);
            } catch (final SQLException e) {
                ex = e;
            }
        }
        LOGGER.error("giving up after {} tries", maxTry);
        throw ex;
    }

    public <R> R select(final Connection conn, final String sql,
            final SqlFunction<ResultSet, R> sqlFunc) throws SQLException {
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            return sqlFunc.apply(rs);
        }
    }

    public <R> R select(final String sql,
            final SqlFunction<ResultSet, R> sqlFunc, final Object... args)
            throws SQLException {
        SQLException ex = null;
        for (int i = 1; i <= maxTry; i++) {
            try (Connection conn = ds.getConnection()) {
                return select(conn, sql, sqlFunc, args);
            } catch (final SQLException e) {
                ex = e;
            }
        }
        LOGGER.error("giving up after {} tries", maxTry);
        throw ex;
    }

    public <R> R select(final Connection conn, final String sql,
            final SqlFunction<ResultSet, R> sqlFunc, final Object... args)
            throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int p = 0; p < args.length; p++) {
                stmt.setObject(p + 1, args[p]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return sqlFunc.apply(rs);
            }
        }
    }

    // update methods - update/insert/delete/create
    public int update(final String sql) throws SQLException {
        SQLException ex = null;
        for (int i = 1; i <= maxTry; i++) {
            try (Connection conn = ds.getConnection()) {
                return update(conn, sql);
            } catch (final SQLException e) {
                ex = e;
            }
        }
        LOGGER.error("giving up after {} tries", maxTry);
        throw ex;
    }

    public int update(final Connection conn, final String sql)
            throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            return stmt.getUpdateCount();
        }
    }

    public int update(final String sql, final Object... args)
            throws SQLException {
        SQLException ex = null;
        for (int i = 1; i <= maxTry; i++) {
            try (Connection conn = ds.getConnection()) {
                return update(conn, sql, args);
            } catch (final SQLException e) {
                ex = e;
            }
        }
        LOGGER.error("giving up after {} tries", maxTry);
        throw ex;
    }

    public int update(final Connection conn, final String sql,
            final Object... args) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int p = 0; p < args.length; p++) {
                stmt.setObject(p + 1, args[p]);
            }
            stmt.execute();
            return stmt.getUpdateCount();
        }
    }

    // execute a self contained SQLFunction and return value
    public <T, R> R execute(final SqlFunction<T, R> sqlFunc, final T t)
            throws SQLException {
        SQLException ex = null;
        for (int i = 1; i <= maxTry; i++) {
            try {
                return sqlFunc.apply(t);
            } catch (final SQLException e) {
                ex = e;
            }
        }
        LOGGER.error("giving up after {} tries", maxTry);
        throw ex;
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
