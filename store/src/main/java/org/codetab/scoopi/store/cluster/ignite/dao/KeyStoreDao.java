package org.codetab.scoopi.store.cluster.ignite.dao;

import static org.codetab.scoopi.util.Util.spaceit;

import java.util.List;

import javax.inject.Inject;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.jdbi.v3.core.Jdbi;

public class KeyStoreDao {

    @Inject
    private Configs configs;

    private Jdbi jdbi;

    public boolean initJdbi() {
        try {
            final String url =
                    configs.getConfig("scoopi.clusterStore.connectionUrl");
            jdbi = Jdbi.create(url);
        } catch (final ConfigNotFoundException e) {
            throw new CriticalException(e);
        }
        return true;
    }

    public boolean createKeyStoreTable() {
        jdbi.useHandle(handle -> handle.execute(
                spaceit("create table IF NOT EXISTS keystore (key varchar(128)",
                        "PRIMARY KEY not null, value varchar(128) not null)")));
        return true;
    }

    // insert or replace
    public boolean putValue(final String key, final String value) {
        jdbi.useHandle(handle -> handle
                .createUpdate(
                        "merge into keystore(key,value) KEY (key) values(?,?)")
                .bind(0, key).bind(1, value).execute());
        return true;
    }

    public String getValue(final String key) {
        return jdbi.withHandle(handle -> handle
                .select("select value from keystore where key = ?", key)
                .mapTo(String.class).one());
    }

    /*
     * if key/value exists and value matches existing value then update to
     * newValue; if no key/value exists, insert newValue. Example,
     * data_grid_state is NEW change it to INITIALIZE, if no such key then
     * insert and set it to INITIALIZE
     */
    public String changeValue(final String key, final String value,
            final String newValue) {
        return jdbi.inTransaction(handle -> {
            try {
                final String existingValue = handle
                        .select("select value from keystore where key = ?", key)
                        .mapTo(String.class).one();
                if (existingValue.equals(value)) {
                    handle.createUpdate(
                            "update keystore set value = ? where key = ?")
                            .bind(0, newValue).bind(1, key).execute();
                }
            } catch (final IllegalStateException e) {
                handle.execute("insert into keystore values (?,?)", key,
                        newValue);
            }
            return handle
                    .select("select value from keystore where key = ?", key)
                    .mapTo(String.class).one();
        });
    }

    public boolean contains(final String key) {
        try {
            jdbi.withHandle(handle -> handle
                    .select("select value from keystore where key = ?", key)
                    .mapTo(String.class).one());
            return true;
        } catch (final IllegalStateException e) {
            return false;
        }
    }

    public List<String> getKeys(final String value) {
        return jdbi.withHandle(handle -> handle
                .select("select key from keystore where value = ?", value)
                .mapTo(String.class).list());
    }
}
