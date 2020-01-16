package org.codetab.scoopi.store.cluster.hz;

import static org.apache.commons.lang3.Validate.notNull;
import static org.codetab.scoopi.util.Util.spaceit;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.model.ClusterPayload;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.store.ICluster;
import org.codetab.scoopi.store.cluster.IClusterJobStore;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.TransactionalMap;
import com.hazelcast.flakeidgen.FlakeIdGenerator;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;
import com.hazelcast.transaction.TransactionOptions.TransactionType;

@Singleton
public class JobStore implements IClusterJobStore {

    @Inject
    private Configs configs;
    @Inject
    private ICluster cluster;
    @Inject
    private ObjectFactory objFactory;

    private HazelcastInstance hz;

    private Map<Long, ClusterPayload> jobMap;
    private Map<String, String> keyStoreMap;

    private String memberId;
    private int jobTakeLimit;
    private FlakeIdGenerator jobIdGenerator;

    private TransactionOptions options = new TransactionOptions()
            .setTransactionType(TransactionType.TWO_PHASE);

    @Override
    public boolean open() {
        try {
            hz = (HazelcastInstance) cluster.getInstance();

            memberId = configs.getConfig("scoopi.cluster.memberId");
            jobTakeLimit = Integer
                    .parseInt(configs.getConfig("scoopi.cluster.jobTakeLimit"));
            jobIdGenerator = hz.getFlakeIdGenerator("job_id_seq");

            // create distributed collections
            jobMap = hz.getMap("job");
            keyStoreMap = hz.getMap("keyStore");
            return true;
        } catch (NumberFormatException | ConfigNotFoundException e) {
            throw new CriticalException(e);
        }
    }

    @Override
    public boolean close() {
        // ScoopiEngine stops cluster
        return true;
    }

    @Override
    public boolean putJob(final Payload payload) throws InterruptedException {
        notNull(payload, "payload must not be null");

        long jobId = payload.getJobInfo().getId();
        ClusterPayload cluserPayload =
                objFactory.createClusterPayload(payload, jobId);

        if (Objects.isNull(jobMap.put(jobId, cluserPayload))) {
            return true;
        } else {
            throw new IllegalStateException(
                    spaceit("duplicate job", String.valueOf(jobId)));
        }
    }

    @Override
    public Payload takeJob() throws InterruptedException {
        Optional<ClusterPayload> opt =
                jobMap.values().stream().filter(p -> !p.isTaken()).findFirst();
        long jobId = -1;
        if (opt.isPresent()) {
            jobId = opt.get().getJobId();
        } else {
            throw new NoSuchElementException("jobs queue is empty");
        }

        TransactionContext tx = hz.newTransactionContext(options);

        try {
            tx.beginTransaction();
            TransactionalMap<Long, ClusterPayload> txJobMap = tx.getMap("job");

            ClusterPayload cPayload = txJobMap.getForUpdate(jobId);
            if (cPayload.isTaken()) {
                throw new IllegalStateException(
                        "job already taken by another node");
            } else {
                cPayload.setTaken(true);
                cPayload.setMemberId(memberId);
                txJobMap.set(jobId, cPayload);
                tx.commitTransaction();
                return cPayload.getPayload();
            }
        } catch (Exception e) {
            tx.rollbackTransaction();
            throw e;
        }
    }

    /**
     * Push payloads in batch and mark job id as finished. If job id is -1 then
     * it is not marked.
     * @throws InterruptedException
     */
    @Override
    public boolean putJobs(final List<Payload> payloads, final long jobId)
            throws InterruptedException {

        TransactionContext tx = hz.newTransactionContext(options);

        try {
            tx.beginTransaction();
            TransactionalMap<Long, ClusterPayload> txJobMap = tx.getMap("job");

            for (Payload payload : payloads) {
                long jId = payload.getJobInfo().getId();
                ClusterPayload cluserPayload =
                        objFactory.createClusterPayload(payload, jId);
                if (Objects.nonNull(txJobMap.put(jId, cluserPayload))) {
                    throw new IllegalStateException(
                            spaceit("rollback batch put jobs, duplicate job:",
                                    String.valueOf(jId)));
                }
            }
            // remove old payload
            if (Objects.isNull(txJobMap.remove(jobId))) {
                throw new IllegalStateException(
                        "rollback batch put jobs, parent job already removed by another node");
            }

            tx.commitTransaction();
            return true;
        } catch (Exception e) {
            tx.rollbackTransaction();
            throw e;
        }
    }

    @Override
    public boolean markFinished(final long jobId) {

        TransactionContext tx = hz.newTransactionContext(options);

        try {
            tx.beginTransaction();
            TransactionalMap<Long, ClusterPayload> txJobMap = tx.getMap("job");

            if (Objects.isNull(txJobMap.remove(jobId))) {
                throw new IllegalStateException(
                        spaceit("rollback mark finish, no such job:",
                                String.valueOf(jobId)));
            }
            tx.commitTransaction();
            return true;
        } catch (Exception e) {
            tx.rollbackTransaction();
            throw e;
        }
    }

    @Override
    public boolean changeStateToInitialize() {
        String key = "data_grid_state";
        TransactionContext tx = hz.newTransactionContext();
        try {
            tx.beginTransaction();
            TransactionalMap<String, String> txKeyStoreMap =
                    tx.getMap("keyStore");
            boolean stateChange = false;
            if (txKeyStoreMap.containsKey(key)) {
                if (txKeyStoreMap.get(key).equals(State.NEW.toString())) {
                    txKeyStoreMap.put(key, State.INITIALIZE.toString());
                    stateChange = true;
                }
            } else {
                txKeyStoreMap.put(key, State.INITIALIZE.toString());
                stateChange = true;
            }
            tx.commitTransaction();
            return stateChange;
        } catch (Exception e) {
            tx.rollbackTransaction();
            throw e;
        }
    }

    @Override
    public int getJobCount() {
        return jobMap.size();
    }

    @Override
    public boolean isDone() {
        return jobMap.size() == 0;
    }

    @Override
    public State getState() {
        return State.valueOf(keyStoreMap.get("data_grid_state"));
    }

    @Override
    public void setState(final State state) {
        keyStoreMap.put("data_grid_state", state.toString());
    }

    @Override
    public String getNodeId() {
        return memberId;
    }

    @Override
    public int getJobTakenCount() {
        return (int) jobMap.values().stream().filter(p -> p.isTaken()).count();
    }

    @Override
    public int getJobTakeLimit() {
        return jobTakeLimit;
    }

    @Override
    public long getJobIdSeq() {
        return jobIdGenerator.newId();
    }
}
