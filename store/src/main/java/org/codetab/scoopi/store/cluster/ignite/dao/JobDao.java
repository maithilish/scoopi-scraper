package org.codetab.scoopi.store.cluster.ignite.dao;

import static org.codetab.scoopi.util.Util.spaceit;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.SerializationUtils;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.model.Payload;
import org.jdbi.v3.core.Jdbi;

public class JobDao {

    @Inject
    private Configs configs;
    @Inject
    private DaoHelper daoHelper;

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

    public boolean createJobTable() {

        jdbi.useHandle(handle -> handle.execute(spaceit(
                "create table IF NOT EXISTS job (id int primary key,",
                "state varchar(10), node varchar(32),payload binary)")));
        return true;
    }

    // TODO id auto increment
    public boolean putJob(final Payload payload) {
        final long id = payload.getJobInfo().getId();
        final byte[] bytes = daoHelper.serialize(payload);
        jdbi.useHandle(handle -> handle
                .createUpdate(
                        "insert into job (id, state, payload) values(?,?,?)")
                .bind(0, id).bind(1, "NEW").bind(2, bytes).execute());
        return true;
    }

    // TODO set id in jobInfo
    public Payload takeJob() {
        final Map<String, Object> job = jdbi.inTransaction(handle -> {
            final Optional<Map<String, Object>> j = handle.createQuery(
                    "SELECT id, payload FROM job where state = 'NEW' ORDER BY id asc ")
                    .mapToMap().findFirst();
            if (j.isPresent()) {
                final int id = (int) j.get().get("id");
                handle.execute("update job set state = 'TAKEN' where id = ?",
                        id);
                return j.get();
            } else {
                throw new NoSuchElementException("no pending job to take");
            }
        });
        // deserialize outside the transaction
        return SerializationUtils.deserialize((byte[]) job.get("payload"));
    }

    public boolean markFinished(final long id) {
        jdbi.useHandle(handle -> handle
                .execute("update job set state = 'FINISHED' where id = ?", id));
        return true;
    }

    public int getJobsCount(final String state) {
        return jdbi.withHandle(handle -> handle
                .select("select count(*) from job where state = ?", state)
                .mapTo(Integer.class).one());
    }

    public int getPendingJobsCount() {
        return jdbi.withHandle(handle -> handle.select(
                "select count(*) from job where state not in ('FINISHED')")
                .mapTo(Integer.class).one());
    }

}
