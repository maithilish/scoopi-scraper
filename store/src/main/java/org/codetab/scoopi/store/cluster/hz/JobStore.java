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
import com.hazelcast.collection.IList;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.flakeidgen.FlakeIdGenerator;
import com.hazelcast.map.IMap;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;
import com.hazelcast.transaction.TransactionalList;
import com.hazelcast.transaction.TransactionalMap;

/**
 * Hazelcast implementation of JobStore.
 * @author m
 *
 */
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

    private IList<ClusterJob> jobsList;
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
            jobsList = hz.getList(DsName.JOBS_LIST.toString());
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
        // ScoopiEngine stops cluster, just log metrics
        final long divisor = 1_000_000;

        Timer t = metricsHelper.getTimer(this, "job", "put", "time");
        LOG.debug("{}", metricsHelper.printSnapshot("job put time",
                t.getCount(), t.getSnapshot(), divisor));
        t = metricsHelper.getTimer(this, "job", "take", "time");
        LOG.debug("{}", metricsHelper.printSnapshot("job take time",
                t.getCount(), t.getSnapshot(), divisor));
    }

    /*
     * If txPayloadsMap contains jobId then throws duplicate job, otherwise
     * create ClusterJob (job taken status and node), push it to txJobsList and
     * put payload to txPayloadsMap.
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
            TransactionalList<ClusterJob> txJobsList =
                    tx.getList(DsName.JOBS_LIST.toString());
            TransactionalMap<Long, Payload> txPayloadsMap =
                    tx.getMap(DsName.PAYLOADS_MAP.toString());

            if (txPayloadsMap.containsKey(jobId)) {
                throw new JobStateException(
                        spaceit("duplicate job", String.valueOf(jobId)));
            } else {
                txJobsList.add(cluserJob);
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
     * Push cluster jobs and payloads in batch and mark job id as finished. If
     * job id is -1 then it is not marked.
     * @throws InterruptedException
     * @throws TransactionException
     */
    @Override
    public boolean putJobs(final List<Payload> payloads, final long jobId)
            throws InterruptedException, TransactionException {

        TransactionContext tx = hz.newTransactionContext(txOptions);

        try {
            tx.beginTransaction();
            TransactionalList<ClusterJob> txJobsList =
                    tx.getList(DsName.JOBS_LIST.toString());
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
                if (txPayloadsMap.containsKey(newJobId)) {
                    throw new JobStateException(
                            spaceit("rollback batch put jobs, duplicate job",
                                    String.valueOf(newJobId)));
                } else {
                    ClusterJob cluserJob =
                            objFactory.createClusterJob(newJobId);
                    txJobsList.add(cluserJob);
                    txPayloadsMap.set(newJobId, payload);
                    LOG.debug("batch put payload {}", newJobId);
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

    /**
     * Tries to acquire take permit and if succeeds, then take ClusterJob from
     * jobs queue and update its status to taken, add it takenMap and return the
     * payload taken from payloadsM.
     */
    @Override
    public Payload takeJob() throws InterruptedException, TransactionException,
            TimeoutException {

        Context timer =
                metricsHelper.getTimer(this, "job", "take", "time").time();

        boolean acquired = jobTakeThrottle.tryAcquire(jobTakeTimeout,
                TimeUnit.MILLISECONDS);
        if (!acquired) {
            timer.stop();
            throw new TimeoutException(
                    "jobs taken limit exceeded, unable to acquire permit");
        }

        int size = jobsList.size();
        if (size == 0) {
            timer.stop();
            jobTakeThrottle.release();
            throw new NoSuchElementException("jobs queue is empty");
        }

        TransactionContext tx = hz.newTransactionContext(txOptions);
        try {
            int index = getLifoIndex(size);
            ClusterJob cJob = jobsList.get(index);

            tx.beginTransaction();
            TransactionalList<ClusterJob> txJobsList =
                    tx.getList(DsName.JOBS_LIST.toString());
            TransactionalMap<Long, ClusterJob> txTakenJobsMap =
                    tx.getMap(DsName.TAKEN_JOBS_MAP.toString());
            TransactionalMap<Long, Payload> txPayloadsMap =
                    tx.getMap(DsName.PAYLOADS_MAP.toString());

            long jobId = cJob.getJobId();
            if (!txJobsList.remove(cJob)) {
                throw new IllegalStateException(
                        spaceit("unable to find and remove job",
                                String.valueOf(jobId)));
            } else {
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
            }

        } catch (Exception e) {
            try {
                tx.rollbackTransaction();
            } catch (IllegalStateException e1) {
                LOG.error("tx rollback, {}", e1.getMessage());
            }
            jobTakeThrottle.release();
            String message = e.getMessage();
            throw new TransactionException(message, e);
        } finally {
            timer.stop();
        }
    }

    private int getLifoIndex(final int size) {
        final int windowSize = 10;
        int offset;
        if (size > windowSize) {
            offset = random.nextInt(windowSize);
        } else {
            offset = random.nextInt(size);
        }
        int index = size - offset - 1;
        if (index < 0) {
            index = 0;
        }
        return index;
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
     * may fail when a member crashes. Call this method (in while loop until
     * true) when such methods throw TransactionException to remove the taken
     * job and add it back to jobs map.
     */
    @Override
    public boolean resetTakenJob(final long jobId) {

        /*
         * this method is called when an node crashes and tx ex is thrown, if
         * this also throws txEx, then reset fails that leads to termination
         * failure or data error. Fix is to call this method in while loop till
         * it returns true. Revisit this method if termination hangs and change
         * unlimited retries to limited.
         */

        TransactionContext tx = hz.newTransactionContext(txOptions);
        try {
            tx.beginTransaction();
            TransactionalList<ClusterJob> txJobsList =
                    tx.getList(DsName.JOBS_LIST.toString());
            TransactionalMap<Long, ClusterJob> txTakenJobsMap =
                    tx.getMap(DsName.TAKEN_JOBS_MAP.toString());

            LOG.debug("reset taken job {}", jobId);
            ClusterJob cJob = txTakenJobsMap.remove(jobId);
            cJob.setTaken(false);
            cJob.setMemberId(null);
            txJobsList.add(cJob);
            tx.commitTransaction();

            if (jobTakeThrottle.availablePermits() < jobTakeLimit) {
                jobTakeThrottle.release();
            }

            return true;
        } catch (Exception e) {
            tx.rollbackTransaction();
            LOG.debug("reset taken job {} failed, {} ", jobId, e);
            return false;
        }
    }

    @Override
    public boolean isDone() {
        return jobsList.isEmpty() && takenJobsMap.isEmpty();
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
