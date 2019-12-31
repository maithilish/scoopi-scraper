package org.codetab.scoopi.store.cluster.ignite.dao;

import static org.codetab.scoopi.util.Util.spaceit;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.inject.Inject;

import org.codetab.scoopi.exception.CriticalException;

public class KeyStoreDao {

    @Inject
    private Jdbc jdbc;

    public boolean init() {
        return jdbc.init();
    }

    public boolean close() {
        return jdbc.close();
    }

    public boolean createTables() throws SQLException {
        final String sql =
                spaceit("create table IF NOT EXISTS keystore (key varchar(128)",
                        "PRIMARY KEY not null, value varchar(128) not null)",
                        "WITH \"template=REPLICATED,atomicity=ATOMIC\"");
        return jdbc.update(sql) == 0;
    }

    // insert or replace
    public boolean putValue(final String key, final String value) {
        String sql = "merge into keystore(key,value) KEY (key) values(?,?)";
        try {
            jdbc.update(sql, key, value);
            return true;
        } catch (final SQLException e) {
            String message = "keystore put";
            throw new CriticalException(message, e);
        }
    }

    public String getValue(final String key) {
        String sql = "select value from keystore where key = ?";
        try {
            return jdbc.select(sql, (rs -> {
                if (rs.next()) {
                    return rs.getString(1);
                } else {
                    throw new NoSuchElementException(key);
                }
            }), key);
        } catch (final SQLException e) {
            String message = "keystore get";
            throw new CriticalException(message, e);
        }
    }

    /*
     * if key/value exists and value matches existing value then update to
     * newValue; if no key/value exists, insert newValue. Example,
     * data_grid_state is NEW change it to INITIALIZE, if no such key then
     * insert and set it to INITIALIZE
     */
    public boolean changeValue(final String key, final String value,
            final String newValue) throws SQLException {
        SqlFunction<String, Boolean> sqlFunc = (dummy -> {
            try (Connection conn = jdbc.getConnection()) {
                conn.setAutoCommit(false);

                String sql = "select value from keystore where key = ?";
                Optional<String> existingValue = jdbc.select(conn, sql, (rs -> {
                    Optional<String> o = Optional.empty();
                    if (rs.next()) {
                        o = Optional.of(rs.getString(1));
                    }
                    return o;
                }), key);
                boolean r = false;
                if (existingValue.isPresent()) {
                    if (existingValue.get().equals(value)) {
                        sql = spaceit("update keystore set value = ?",
                                "where key = ? and value = ?");
                        if (jdbc.update(conn, sql, newValue, key, value) == 1) {
                            r = true;
                        } else {
                            r = false;
                        }
                    }
                } else {
                    sql = "insert into keystore values (?,?)";
                    if (jdbc.update(conn, sql, key, newValue) == 1) {
                        r = true;
                    } else {
                        r = false;
                    }
                }
                conn.commit();
                return r;
            }
        });
        // execute with retry
        return jdbc.execute(sqlFunc, "");
    }

    public boolean contains(final String key) throws SQLException {
        String sql = "select value from keystore where key = ?";
        return jdbc.select(sql, (rs -> {
            return rs.next();
        }), key);
    }

    public List<String> getKeys(final String value) throws SQLException {
        String sql = "select key from keystore where value = ?";
        return jdbc.select(sql, (rs -> {
            List<String> keys = new ArrayList<>();
            while (rs.next()) {
                keys.add(rs.getString(1));
            }
            return keys;
        }));
    }
}
