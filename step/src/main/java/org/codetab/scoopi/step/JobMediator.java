package org.codetab.scoopi.step;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.log.Log.CAT;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.store.IJobStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class JobMediator {

    static final Logger LOGGER = LoggerFactory.getLogger(JobMediator.class);

    static final int WAIT_MILLIS = 1000;

    @Inject
    private TaskMediator taskMediator;
    @Inject
    private IJobStore jobStore;
    @Inject
    private ErrorLogger errorLogger;

    private JobRunnerThread jobRunner = new JobRunnerThread();

    private AtomicBoolean done = new AtomicBoolean(false);
    private AtomicBoolean jobSeeder = new AtomicBoolean(false);

    private CountDownLatch seedDoneSignal;

    /**
     * cluster: init connection, create tables. For first node, change jobStore
     * state from NEW to INITIALIZE and set seedJobs to true; for others,
     * seedJobs is false.
     * <p>
     * solo: set state to READY.
     */
    public void init() {
        jobStore.open();
        if (jobStore.changeStateToInitialize()) {
            jobSeeder.set(true);
        } else {
            jobSeeder.set(false);
        }
    }

    public void start() {
        jobRunner.start();
    }

    public void waitForFinish() {
        try {
            jobRunner.join();
            jobStore.close();
        } catch (final InterruptedException e) {
            final String message = "wait for finish interrupted";
            errorLogger.log(CAT.INTERNAL, message, e);
        }
    }

    public boolean pushPayload(final Payload payload)
            throws InterruptedException {
        notNull(payload, "payload must not be null");
        return jobStore.putJob(payload);
    }

    public boolean pushPayloads(final List<Payload> payloads, final long jobId)
            throws InterruptedException {
        notNull(payloads, "payloads must not be null");
        return jobStore.putJobs(payloads, jobId);
    }

    private void initiateJob()
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, InterruptedException {
        try {
            try {
                Payload payload = jobStore.takeJob();
                while (jobStore.getJobTakenCount() > jobStore
                        .getJobTakeLimit()) {
                    LOGGER.debug("wait... jobs taken > q size: {}",
                            jobStore.getJobTakeLimit());
                    Thread.sleep(WAIT_MILLIS);
                }
                taskMediator.pushPayload(payload);
                if (jobStore.isDone()) {
                    done.set(true);
                }
            } catch (IllegalStateException e) {
                LOGGER.debug("retry... multiple nodes try to take same job");
                Thread.sleep(WAIT_MILLIS);
            }
        } catch (NoSuchElementException e) {
            done.set(true);
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

    public void markJobFinished(final long id) {
        jobStore.markFinished(id);
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
                synchronized (this) {
                    if (taskMediator.isDone()) {
                        break;
                    }
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
}
