package org.codetab.scoopi.store.cluster.ignite.dao;

import static org.codetab.scoopi.util.Util.spaceit;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import org.apache.commons.lang3.SerializationUtils;
import org.codetab.scoopi.model.Payload;

public class JobDao {

    @Inject
    private Jdbc jdbc;
    @Inject
    private DaoHelper daoHelper;

    public boolean init() {
        return jdbc.init();
    }

    public boolean close() {
        return jdbc.close();
    }

    public boolean createTables() throws SQLException {
        final String sql =
                spaceit("create table IF NOT EXISTS job (id int primary key,",
                        "state varchar(10), node varchar(128),payload binary)",
                        "WITH \"template=REPLICATED,atomicity=ATOMIC\"");
        return jdbc.update(sql) == 0;
    }

    public boolean putJob(final Payload payload) throws SQLException {
        final long id = payload.getJobInfo().getId();
        final byte[] bytes = daoHelper.serialize(payload);
        final String sql = "insert into job (id, state, payload) values(?,?,?)";
        if (jdbc.update(sql, id, "NEW", bytes) == 1) {
            return true;
        } else {
            String message =
                    spaceit("put job", payload.getJobInfo().toString());
            throw new IllegalStateException(message);
        }
    }

    public Payload takeJob(final String nodeId) throws SQLException {
        SqlFunction<String, Payload> sqlFunc = (fnodeId -> {
            try (Connection conn = jdbc.getConnection()) {
                conn.setAutoCommit(false);
                String sql = spaceit(
                        "SELECT id, payload FROM job where state = 'NEW'",
                        "ORDER BY id asc ");
                Map<String, Object> map = jdbc.select(conn, sql, (rs -> {
                    if (rs.next()) {
                        Map<String, Object> p = new HashMap<>();
                        p.put("id", rs.getInt(1));
                        p.put("payload", rs.getObject(2));
                        return p;
                    } else {
                        throw new NoSuchElementException(
                                "no pending job to take");
                    }
                }));
                int id = (int) map.get("id");

                sql = spaceit("update job set state = 'TAKEN', node = ?",
                        " where state = 'NEW' and id = ?");
                if (jdbc.update(conn, sql, fnodeId, id) == 1) {
                    conn.commit();
                } else {
                    throw new IllegalStateException(
                            "multiple nodes try to take same job");
                }
                return (Payload) SerializationUtils
                        .deserialize((byte[]) map.get("payload"));
            }
        });
        // execute with retry
        return jdbc.execute(sqlFunc, nodeId);
    }

    public boolean markFinished(final long id) throws SQLException {
        final String sql = "update job set state = 'FINISHED' where id = ?";
        if (jdbc.update(sql, id) == 1) {
            return true;
        } else {
            String message = spaceit("mark finish", String.valueOf(id));
            throw new IllegalStateException(message);
        }
    }

    public int getJobCount(final String state) throws SQLException {
        final String sql = "select count(*) from job where state = ?";
        return jdbc.select(sql, (rs -> {
            rs.next();
            return rs.getInt(1);
        }), state);
    }

    public int getJobTakenCount(final String nodeId) throws SQLException {
        final String sql =
                "select count(*) from job where state = 'TAKEN' and node = ?";
        return jdbc.select(sql, (rs -> {
            rs.next();
            return rs.getInt(1);
        }), nodeId);

    }

    public int getPendingJobCount() throws SQLException {
        final String sql =
                "select count(*) from job where state not in ('FINISHED')";
        return jdbc.select(sql, (rs -> {
            rs.next();
            return rs.getInt(1);
        }));
    }
}
