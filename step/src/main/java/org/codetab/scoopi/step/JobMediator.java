package org.codetab.scoopi.step;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.JobStateException;
import org.codetab.scoopi.exception.TransactionException;
import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.log.Log.CAT;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.store.IJobStore;
import org.codetab.scoopi.store.IShutdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class JobMediator {

    static final Logger LOGGER = LoggerFactory.getLogger(JobMediator.class);

    @Inject
    private Configs configs;
    @Inject
    private TaskMediator taskMediator;
    @Inject
    private IJobStore jobStore;
    @Inject
    private IShutdown shutdown;
    @Inject
    private ErrorLogger errorLogger;

    private JobRunnerThread jobRunner = new JobRunnerThread();
    private AtomicBoolean jobSeeder = new AtomicBoolean(false);
    private CountDownLatch seedDoneSignal;

    private int takeFailWait; // job take fail, retry wait
    private int takeLimitWait; // job take limit crossed, retry wait
    private int jobPushInterval; // wait between jobs push to TM

    /**
     * cluster: init connection, create tables. For first node, change jobStore
     * state from NEW to INITIALIZE and set seedJobs to true; for others,
     * seedJobs is false.
     * <p>
     * solo: set state to READY.
     */
    public void init() {
        takeFailWait = Integer
                .parseInt(configs.getConfig("scoopi.job.takeFailWait", "1000"));
        takeLimitWait = Integer
                .parseInt(configs.getConfig("scoopi.job.takeLimitWait", "500"));
        jobPushInterval = Integer
                .parseInt(configs.getConfig("scoopi.job.pushInterval", "0"));
        shutdown.init();
        jobStore.open();
        try {
            if (jobStore.changeStateToInitialize()) {
                jobSeeder.set(true);
            } else {
                jobSeeder.set(false);
            }
        } catch (TransactionException e) {
            jobSeeder.set(false);
            LOGGER.warn("{}", e);
        }
    }

    public void start() {
        jobRunner.start();
    }

    public void waitForFinish() {
        try {
            taskMediator.waitForFinish();
            jobRunner.join();
            jobStore.close();
            shutdown.setTerminate();
        } catch (final InterruptedException e) {
            final String message = "wait for finish interrupted";
            errorLogger.log(CAT.INTERNAL, message, e);
        }
    }

    public void pushPayload(final Payload payload)
            throws InterruptedException, TransactionException {
        notNull(payload, "payload must not be null");
        jobStore.putJob(payload);
    }

    public void pushPayloads(final List<Payload> payloads, final long jobId)
            throws InterruptedException, TransactionException {
        notNull(payloads, "payloads must not be null");
        try {
            jobStore.putJobs(payloads, jobId);
        } catch (JobStateException e) {
            LOGGER.debug("{}", e.getLocalizedMessage());
        } catch (TransactionException e) {
            jobStore.resetTakenJob(jobId);
        }
    }

    public void setSeedDoneSignal(final int size) {
        this.seedDoneSignal = new CountDownLatch(size);
        LOGGER.debug("job seed countdown latch set: " + size);
    }

    public void countDownSeedDone() {
        seedDoneSignal.countDown();
        LOGGER.debug("job seed latch countdown");
    }

    public boolean isJobSeeder() {
        return jobSeeder.get();
    }

    public long getJobIdSequence() {
        return jobStore.getJobIdSeq();
    }

    public void markJobFinished(final long jobId) throws TransactionException {
        try {
            jobStore.markFinished(jobId);
        } catch (JobStateException e) {
            LOGGER.debug("{}", e.getLocalizedMessage());
        } catch (TransactionException e) {
            jobStore.resetTakenJob(jobId);
        }
    }

    class JobRunnerThread extends Thread {

        @Override
        public void run() {
            /*
             * wait and initiate jobs only after seeding is done, LocatorSeeder
             * count downs the latch
             */
            try {
                LOGGER.debug("wait on job countdown latch");
                seedDoneSignal.await();
            } catch (final InterruptedException e) {
                errorLogger.log(CAT.ERROR, "unable finish seeding", e);
            }
            if (jobSeeder.get()) {
                jobStore.setState(IJobStore.State.READY);
            }

            LOGGER.debug("take jobs from cluster and initiate task");

            while (true) {
                if (taskMediator.isState(TMState.TERMINATED)) {
                    break;
                }

                try {
                    initiateJob();
                } catch (ClassNotFoundException | InstantiationException
                        | IllegalAccessException | InterruptedException e) {
                    final String message = "unable to initiate job";
                    errorLogger.log(CAT.ERROR, message, e);
                }
            }
        }
    }

    private boolean initiateJob()
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, InterruptedException {

        try {

            int takeLimit = jobStore.getJobTakeLimit();
            while (true) {
                int takenCount = jobStore.getJobTakenByMemberCount();
                if (takenCount <= takeLimit)
                    break;
                LOGGER.debug("wait... jobs taken {} limit: {}", takenCount,
                        takeLimit);
                Thread.sleep(takeLimitWait);
            }

            if (taskMediator.isState(TMState.SHUTDOWN)) {
                return false;
            }

            if (taskMediator.isState(TMState.DONE)
                    && taskMediator.tryShutdown()) {
                LOGGER.info("task mediator state change {}",
                        taskMediator.getState());
                return false;
            }

            jobStore.resetCrashedJobs();
            Payload payload = jobStore.takeJob();

            // wait before push to TM
            if (jobPushInterval > 0) {
                Thread.sleep(jobPushInterval);
            }
            taskMediator.pushPayload(payload);
            return true;
        } catch (JobStateException | NoSuchElementException
                | IllegalStateException | TransactionException e) {
            if (e instanceof IllegalStateException) {
                errorLogger.log(CAT.INTERNAL, "unable to initiate job", e);
            }
            LOGGER.debug("task mediator state {}", taskMediator.getState());
            LOGGER.debug("retry initiate job after {} ms, {}", takeFailWait,
                    e.getLocalizedMessage());
            Thread.sleep(takeFailWait);
            return false;
        }
    }
}
