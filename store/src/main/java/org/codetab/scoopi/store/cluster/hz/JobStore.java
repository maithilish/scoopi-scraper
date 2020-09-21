package org.codetab.scoopi.store.cluster.hz;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.Validate.notNull;
import static org.codetab.scoopi.util.Util.spaceit;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.exception.JobStateException;
import org.codetab.scoopi.exception.TransactionException;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.ClusterJob;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.store.ICluster;
import org.codetab.scoopi.store.cluster.IClusterJobStore;

import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.flakeidgen.FlakeIdGenerator;
import com.hazelcast.map.IMap;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;
import com.hazelcast.transaction.TransactionalMap;

@Singleton
public class JobStore implements IClusterJobStore {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private Configs configs;
    @Inject
    private ICluster cluster;
    @Inject
    private ObjectFactory objFactory;
    @Inject
    private CrashCleaner crashCleaner;
    @Inject
    private MetricsHelper metricsHelper;

    private HazelcastInstance hz;

    private IMap<Long, ClusterJob> jobsMap;
    private IMap<Long, ClusterJob> takenJobsMap;
    private IMap<String, String> keyStoreMap;

    private String memberId;
    private FlakeIdGenerator jobIdGenerator;

    private int jobTakeLimit;
    private int jobTakeTimeout;
    private Semaphore jobTakeThrottle;

    private TransactionOptions txOptions;
    private Random random;

    @Override
    public void open() {
        try {
            hz = (HazelcastInstance) cluster.getInstance();
            memberId = configs.getConfig("scoopi.cluster.memberId");
            jobTakeLimit = configs.getInt("scoopi.job.takeLimit", "4");
            jobTakeTimeout = configs.getInt("scoopi.job.takeTimeout", "1000");
            jobIdGenerator = hz.getFlakeIdGenerator("job_id_seq");

            txOptions = (TransactionOptions) cluster.getTxOptions(configs);

            // create distributed collections
            jobsMap = hz.getMap(DsName.JOBS_MAP.toString());
            takenJobsMap = hz.getMap(DsName.TAKEN_JOBS_MAP.toString());
            keyStoreMap = hz.getMap(DsName.KEYSTORE_MAP.toString());

            jobTakeThrottle = new Semaphore(jobTakeLimit);
            random = new Random();
        } catch (NumberFormatException | ConfigNotFoundException e) {
            throw new CriticalException(e);
        }
    }

    @Override
    public void close() {
        // ScoopiEngine stops cluster
        final long divisor = 1000000;
        Timer t = metricsHelper.getTimer(this, "job", "put", "time");
        LOG.debug("{}", metricsHelper.printSnapshot("job put time",
                t.getCount(), t.getSnapshot(), divisor));

        t = metricsHelper.getTimer(this, "job", "take", "time");
        LOG.debug("{}", metricsHelper.printSnapshot("job take time",
                t.getCount(), t.getSnapshot(), divisor));
    }

    /*
     * For the payload, it creates ClusterJob containing job taken status and
     * the node, and adds it to txJobsMap. If map already contains ClusterJob
     * for the jobId then duplicate exception is thrown else payload is added to
     * txPayloadsMap.
     */
    @Override
    public boolean putJob(final Payload payload)
            throws InterruptedException, TransactionException {
        notNull(payload, "payload must not be null");

        Context timer =
                metricsHelper.getTimer(this, "job", "put", "time").time();

        long jobId = payload.getJobInfo().getId();
        ClusterJob cluserJob = objFactory.createClusterJob(jobId);

        TransactionContext tx = hz.newTransactionContext(txOptions);

        try {
            tx.beginTransaction();
            TransactionalMap<Long, ClusterJob> txJobsMap =
                    tx.getMap(DsName.JOBS_MAP.toString());
            TransactionalMap<Long, Payload> txPayloadsMap =
                    tx.getMap(DsName.PAYLOADS_MAP.toString());

            if (txJobsMap.containsKey(jobId)) {
                throw new JobStateException(
                        spaceit("duplicate job", String.valueOf(jobId)));
            } else {
                txJobsMap.set(jobId, cluserJob);
                txPayloadsMap.set(jobId, payload);
                LOG.debug("put payload {}", jobId);
            }
            tx.commitTransaction();
            return true;
        } catch (Exception e) {
            tx.rollbackTransaction();
            String message =
                    spaceit("put job", payload.getJobInfo().getLabel());
            throw new TransactionException(message, e);
        } finally {
            timer.stop();
        }
    }

    /**
     * Push payloads in batch and mark job id as finished. If job id is -1 then
     * it is not marked.
     * @throws InterruptedException
     * @throws TransactionException
     */
    @Override
    public boolean putJobs(final List<Payload> payloads, final long jobId)
            throws InterruptedException, TransactionException {

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
            if (txTakenJobsMap.containsKey(jobId)) {
                txTakenJobsMap.delete(jobId);
                txPayloadsMap.delete(jobId);
            } else {
                throw new JobStateException(
                        "rollback batch put jobs, parent job already removed by another node");
            }

            for (Payload payload : payloads) {
                long newJobId = payload.getJobInfo().getId();
                ClusterJob cluserJob = objFactory.createClusterJob(newJobId);

                if (txJobsMap.containsKey(newJobId)) {
                    throw new JobStateException(
                            spaceit("rollback batch put jobs, duplicate job",
                                    String.valueOf(newJobId)));
                } else {
                    txJobsMap.set(newJobId, cluserJob);
                    txPayloadsMap.set(newJobId, payload);
                    LOG.debug("put payload {}", jobId);
                }
            }

            tx.commitTransaction();

            if (jobTakeThrottle.availablePermits() < jobTakeLimit) {
                jobTakeThrottle.release();
            }

            return true;
        } catch (JobStateException e) {
            tx.rollbackTransaction();
            throw e;
        } catch (Exception e) {
            tx.rollbackTransaction();
            String message =
                    spaceit("put jobs of job id", String.valueOf(jobId));
            throw new TransactionException(message, e);
        }
    }

    @Override
    public Payload takeJob() throws InterruptedException, TransactionException,
            TimeoutException {

        Context timer =
                metricsHelper.getTimer(this, "job", "take", "time").time();

        // TODO - move to config
        final int listLimit = 5;
        // List<Long> pendingJobs = jobsMap.values().stream()
        // .filter(cj -> !cj.isTaken()).limit(listLimit)
        // .map(ClusterJob::getJobId).collect(Collectors.toList());

        List<ClusterJob> pendingJobs =
                jobsMap.values().stream().filter(cj -> !cj.isTaken())
                        .limit(listLimit).collect(Collectors.toList());

        long jobId = -1;
        if (pendingJobs.isEmpty()) {
            timer.stop();
            throw new NoSuchElementException("jobs queue is empty");
        } else {
            int randomIndex = random.nextInt(pendingJobs.size());
            jobId = pendingJobs.get(randomIndex).getJobId();
        }

        boolean acquired = jobTakeThrottle.tryAcquire(jobTakeTimeout,
                TimeUnit.MILLISECONDS);
        if (!acquired) {
            timer.stop();
            throw new TimeoutException(
                    "jobs taken limit exceeded, unable to acquire permit");
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
                txTakenJobsMap.set(jobId, cJob);
                Payload payload = txPayloadsMap.get(jobId);
                if (isNull(payload)) {
                    throw new IllegalStateException(spaceit(
                            "payload not found jobid", String.valueOf(jobId)));
                }
                tx.commitTransaction();
                LOG.debug("job taken {}", cJob.getJobId());
                return payload;
            } else {
                throw new JobStateException("job taken by another node");
            }
        } catch (Exception e) {
            tx.rollbackTransaction();
            jobTakeThrottle.release();
            String message = e.getMessage();
            throw new TransactionException(message, e);
        } finally {
            timer.stop();
        }
    }

    @Override
    public boolean markFinished(final long jobId) throws TransactionException {

        TransactionContext tx = hz.newTransactionContext(txOptions);

        try {
            tx.beginTransaction();
            TransactionalMap<Long, ClusterJob> txTakenJobsMap =
                    tx.getMap(DsName.TAKEN_JOBS_MAP.toString());
            TransactionalMap<Long, Payload> txPayloadsMap =
                    tx.getMap(DsName.PAYLOADS_MAP.toString());

            try {
                txPayloadsMap.delete(jobId);
            } catch (Exception e) {
                // ignore if no payload for the job or any other error
            }
            if (Objects.isNull(txTakenJobsMap.remove(jobId))) {
                throw new JobStateException(
                        spaceit("rollback mark finish, no such job:",
                                String.valueOf(jobId)));
            }
            tx.commitTransaction();
            if (jobTakeThrottle.availablePermits() < jobTakeLimit) {
                jobTakeThrottle.release();
            }
            return true;
        } catch (JobStateException e) {
            tx.rollbackTransaction();
            throw e;
        } catch (Exception e) {
            tx.rollbackTransaction();
            String message = spaceit("mark finish job", String.valueOf(jobId));
            throw new TransactionException(message, e);
        }
    }

    /**
     * Methods such as markFinish or takeJobs which removes taken job from map
     * may fail when a member crashes. Call this method when such method throws
     * TransactionException to remove the taken job and add it back to jobs map.
     */
    @Override
    public boolean resetTakenJob(final long jobId) throws TransactionException {

        TransactionContext tx = hz.newTransactionContext(txOptions);
        try {
            tx.beginTransaction();
            TransactionalMap<Long, ClusterJob> txJobsMap =
                    tx.getMap(DsName.JOBS_MAP.toString());
            TransactionalMap<Long, ClusterJob> txTakenJobsMap =
                    tx.getMap(DsName.TAKEN_JOBS_MAP.toString());

            LOG.debug("reset taken job {}", jobId);
            ClusterJob cJob = txTakenJobsMap.remove(jobId);
            cJob.setTaken(false);
            cJob.setMemberId(null);
            txJobsMap.set(jobId, cJob);
            tx.commitTransaction();

            if (jobTakeThrottle.availablePermits() < jobTakeLimit) {
                jobTakeThrottle.release();
            }

            return true;
        } catch (Exception e) {
            // TODO look at this if data error in cluster
            // this method is called when an node crashes and tx ex is thrown,
            // but if this also throws tx ex, then reset and also termination
            // fails
            tx.rollbackTransaction();
            String message = spaceit("reset taken job", String.valueOf(jobId));
            throw new TransactionException(message, e);
        }
    }

    @Override
    public boolean isDone() {
        return jobsMap.isEmpty() && takenJobsMap.isEmpty();
    }

    @Override
    public void setState(final State state) {
        keyStoreMap.put(DsName.DATA_GRID_STATE.toString(), state.toString());
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
