package org.codetab.scoopi.step;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.concurrent.CountDownLatch;

import javax.annotation.concurrent.GuardedBy;
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
    private static final long SLEEP_MILLIS = 1000;

    @Inject
    private TaskMediator taskMediator;
    @Inject
    private IJobStore jobStore;
    @Inject
    private ErrorLogger errorLogger;

    private JobRunnerThread jobRunner = new JobRunnerThread();

    @GuardedBy("this")
    private int reservations = 0;
    @GuardedBy("this")
    private boolean done = false;

    private CountDownLatch seedDoneSignal;
    private boolean seedJobs = false;

    /**
     * cluster: init connection, create tables. If first node, change jobStore
     * state from NEW to INITIALIZE and set seedJobs to true. For other nodes
     * seedJobs is false.
     * <p>
     * solo: set state to READY.
     */
    public void init() {
        jobStore.init();
        jobStore.createTables();
        if (jobStore.changeStateToInitialize()) {
            seedJobs = true;
        } else {
            seedJobs = false;
        }
    }

    public void start() {
        jobRunner.start();
    }

    public void waitForFinish() {
        try {
            jobRunner.join();
        } catch (InterruptedException e) {
            String message = "wait for finish interrupted";
            errorLogger.log(CAT.INTERNAL, message, e);
        }
    }

    public boolean pushPayload(final Payload payload)
            throws InterruptedException {
        notNull(payload, "payload must not be null");
        synchronized (this) {
            ++reservations;
        }
        jobStore.putJob(payload);
        return true;
    }

    private void initiateJob()
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, InterruptedException {
        Payload payload = jobStore.takeJob();
        synchronized (this) {
            --reservations;
        }
        taskMediator.pushPayload(payload);
        jobStore.markFinished(1);
        if (jobStore.isDone()) {
            synchronized (this) {
                done = true;
            }
            taskMediator.setJobsDone(true);
        }
    }

    public void setSeedDoneSignal(final int size) {
        this.seedDoneSignal = new CountDownLatch(size);
    }

    public void countDownSeedDone() {
        System.out.println("countdown called");
        seedDoneSignal.countDown();
    }

    public boolean isSeedJobs() {
        return seedJobs;
    }

    class JobRunnerThread extends Thread {

        @Override
        public void run() {
            /*
             * wait and initiate jobs only after seeding is done, LocatorSeeder
             * count downs the latch
             */
            try {
                seedDoneSignal.await();
            } catch (InterruptedException e) {
                errorLogger.log(CAT.ERROR, "unable finish seeding", e);
            }
            jobStore.setState(IJobStore.State.READY);

            while (true) {
                synchronized (this) {
                    if (done && reservations == 0) {
                        break;
                    }
                }
                LOGGER.debug("take jobs started");
                try {
                    if (reservations > 0) {
                        initiateJob();
                    } else {
                        LOGGER.info("sleep 1s");
                        Thread.sleep(SLEEP_MILLIS);
                    }
                } catch (ClassNotFoundException | InstantiationException
                        | IllegalAccessException | InterruptedException e) {
                    String message = "unable to initiate job";
                    errorLogger.log(CAT.ERROR, message, e);
                }
            }
        }
    }

}
