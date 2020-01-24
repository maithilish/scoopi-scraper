package org.codetab.scoopi.store.cluster.hz;

import static java.util.Objects.isNull;
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
import org.codetab.scoopi.exception.JobStateException;
import org.codetab.scoopi.model.ClusterJob;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.store.ICluster;
import org.codetab.scoopi.store.cluster.IClusterJobStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.TransactionalMap;
import com.hazelcast.flakeidgen.FlakeIdGenerator;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;

@Singleton
public class JobStore implements IClusterJobStore {

    static final Logger LOGGER = LoggerFactory.getLogger(JobStore.class);

    @Inject
    private Configs configs;
    @Inject
    private ICluster cluster;
    @Inject
    private ObjectFactory objFactory;
    @Inject
    private CrashCleaner crashCleaner;

    private HazelcastInstance hz;

    private Map<Long, ClusterJob> jobsMap;
    private Map<Long, ClusterJob> takenJobsMap;
    private Map<String, String> keyStoreMap;

    private String memberId;
    private int jobTakeLimit;
    private FlakeIdGenerator jobIdGenerator;

    private TransactionOptions txOptions;

    @Override
    public void open() {
        try {
            hz = (HazelcastInstance) cluster.getInstance();
            memberId = configs.getConfig("scoopi.cluster.memberId");
            jobTakeLimit = Integer
                    .parseInt(configs.getConfig("scoopi.job.takeLimit", "4"));
            jobIdGenerator = hz.getFlakeIdGenerator("job_id_seq");

            txOptions = (TransactionOptions) cluster.getTxOptions(configs);

            // create distributed collections
            jobsMap = hz.getMap(DsName.JOBS_MAP.toString());
            takenJobsMap = hz.getMap(DsName.TAKEN_JOBS_MAP.toString());
            keyStoreMap = hz.getMap(DsName.KEYSTORE_MAP.toString());
        } catch (NumberFormatException | ConfigNotFoundException e) {
            throw new CriticalException(e);
        }
    }

    @Override
    public void close() {
        // ScoopiEngine stops cluster
    }

    @Override
    public boolean putJob(final Payload payload) throws InterruptedException {
        notNull(payload, "payload must not be null");

        long jobId = payload.getJobInfo().getId();
        ClusterJob cluserJob = objFactory.createClusterJob(jobId);

        TransactionContext tx = hz.newTransactionContext(txOptions);

        try {
            tx.beginTransaction();
            TransactionalMap<Long, ClusterJob> txJobsMap =
                    tx.getMap(DsName.JOBS_MAP.toString());
            TransactionalMap<Long, Payload> txPayloadsMap =
                    tx.getMap(DsName.PAYLOADS_MAP.toString());

            if (Objects.isNull(txJobsMap.put(jobId, cluserJob))) {
                txPayloadsMap.put(jobId, payload);
                LOGGER.debug("put payload {}", jobId);
            } else {
                throw new JobStateException(
                        spaceit("duplicate job", String.valueOf(jobId)));
            }
            tx.commitTransaction();
            return true;
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

        TransactionContext tx = hz.newTransactionContext(txOptions);

        try {
            tx.beginTransaction();
            TransactionalMap<Long, ClusterJob> txJobsMap =
                    tx.getMap(DsName.JOBS_MAP.toString());
            TransactionalMap<Long, ClusterJob> txTakenJobsMap =
                    tx.getMap(DsName.TAKEN_JOBS_MAP.toString());
            TransactionalMap<Long, Payload> txPayloadsMap =
                    tx.getMap(DsName.PAYLOADS_MAP.toString());

            // remove old job and payload
            if (Objects.nonNull(txTakenJobsMap.remove(jobId))) {
                txPayloadsMap.remove(jobId);
            } else {
                throw new JobStateException(
                        "rollback batch put jobs, parent job already removed by another node");
            }

            for (Payload payload : payloads) {
                long newJobId = payload.getJobInfo().getId();
                ClusterJob cluserJob = objFactory.createClusterJob(newJobId);
                if (Objects.isNull(txJobsMap.put(newJobId, cluserJob))) {
                    txPayloadsMap.put(newJobId, payload);
                } else {
                    throw new JobStateException(
                            spaceit("rollback batch put jobs, duplicate job",
                                    String.valueOf(newJobId)));
                }
            }

            tx.commitTransaction();
            return true;
        } catch (Exception e) {
            tx.rollbackTransaction();
            throw e;
        }
    }

    @Override
    public Payload takeJob() throws InterruptedException {
        Optional<ClusterJob> opt =
                jobsMap.values().stream().filter(p -> !p.isTaken()).findFirst();
        long jobId = -1;
        if (opt.isPresent()) {
            jobId = opt.get().getJobId();
        } else {
            throw new NoSuchElementException("jobs queue is empty");
        }

        TransactionContext tx = hz.newTransactionContext(txOptions);

        try {
            tx.beginTransaction();
            TransactionalMap<Long, ClusterJob> txJobsMap =
                    tx.getMap(DsName.JOBS_MAP.toString());
            TransactionalMap<Long, ClusterJob> txTakenJobsMap =
                    tx.getMap(DsName.TAKEN_JOBS_MAP.toString());
            TransactionalMap<Long, Payload> txPayloadsMap =
                    tx.getMap(DsName.PAYLOADS_MAP.toString());

            if (txJobsMap.containsKey(jobId)) {
                ClusterJob cJob = txJobsMap.remove(jobId);
                if (isNull(cJob)) {
                    throw new JobStateException("job removed by another node");
                }
                cJob.setTaken(true);
                cJob.setMemberId(memberId);
                txTakenJobsMap.put(jobId, cJob);
                Payload payload = txPayloadsMap.get(jobId);
                if (isNull(payload)) {
                    throw new IllegalStateException(spaceit(
                            "payload not found jobid", String.valueOf(jobId)));
                }
                tx.commitTransaction();
                LOGGER.debug("job taken {}", cJob.getJobId());
                return payload;
            } else {
                throw new JobStateException("job taken by another node");
            }
        } catch (Exception e) {
            tx.rollbackTransaction();
            throw e;
        }
    }

    @Override
    public boolean markFinished(final long jobId) {

        TransactionContext tx = hz.newTransactionContext(txOptions);

        try {
            tx.beginTransaction();

            TransactionalMap<Long, ClusterJob> txTakenJobsMap =
                    tx.getMap(DsName.TAKEN_JOBS_MAP.toString());
            TransactionalMap<Long, Payload> txPayloadsMap =
                    tx.getMap(DsName.PAYLOADS_MAP.toString());

            try {
                txPayloadsMap.remove(jobId);
            } catch (Exception e) {
                // ignore if no such job or any exception
            }
            if (Objects.isNull(txTakenJobsMap.remove(jobId))) {
                throw new JobStateException(
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
        return jobsMap.size();
    }

    @Override
    public boolean isDone() {
        return jobsMap.isEmpty() && takenJobsMap.isEmpty();
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
    public String getMemberId() {
        return memberId;
    }

    @Override
    public int getJobTakenCount() {
        return (int) takenJobsMap.values().stream().filter(p -> p.isTaken())
                .count();
    }

    @Override
    public int getJobTakenByMemberCount() {
        return (int) takenJobsMap.values().stream()
                .filter(p -> p.isTaken() && p.getMemberId().equals(memberId))
                .count();
    }

    @Override
    public int getJobTakeLimit() {
        return jobTakeLimit;
    }

    @Override
    public long getJobIdSeq() {
        return jobIdGenerator.newId();
    }

    @Override
    public void resetCrashedJobs() {
        crashCleaner.resetCrashedJobs();
    }
}
