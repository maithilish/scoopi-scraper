package org.codetab.scoopi.step.mediator;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.JobStateException;
import org.codetab.scoopi.exception.TransactionException;
import org.codetab.scoopi.helper.Snooze;
import org.codetab.scoopi.model.ERROR;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.store.IJobStore;

@Singleton
public class JobRunner extends Thread {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private Configs configs;
    @Inject
    private TaskMediator taskMediator;
    @Inject
    private IJobStore jobStore;
    @Inject
    private StateFliper stateFliper;
    @Inject
    private Snooze snooze;

    private AtomicBoolean cancelled = new AtomicBoolean(false);

    private final int aquireLockTimeout = 50;

    public void cancel() {
        cancelled.set(true);
        stateFliper.cancel();
    }

    @Override
    public void run() {

        int jobTakeRetryDelay =
                configs.getInt("scoopi.job.takeRetryDelay", "50");

        LOG.debug("take jobs from jobStore and initiate task");

        while (true) {
            if (stateFliper.isTMState(TMState.TERMINATED)
                    || stateFliper.isTMState(TMState.SHUTDOWN)
                    || cancelled.get()) {
                break;
            }

            try {
                jobStore.resetCrashedJobs();

                stateFliper.acquireJobToTaskQueueLock(aquireLockTimeout,
                        TimeUnit.MILLISECONDS);

                // job store Semaphore throttles take job
                Payload payload = jobStore.takeJob();
                taskMediator.pushPayload(payload);

            } catch (NoSuchElementException e) {
                // SPINNER
                LOG.debug("{}, retry", e.getMessage());
                snooze.sleepUninterruptibly(jobTakeRetryDelay);
            } catch (JobStateException | IllegalStateException
                    | InterruptedException | TransactionException
                    | TimeoutException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                if (e instanceof IllegalStateException) {
                    LOG.error("unable to initiate job [{}]", ERROR.INTERNAL, e);
                }
                LOG.debug("task mediator state {}", stateFliper.getTMState());
                LOG.debug("retry initiate job, {}", e.getMessage());
            } finally {
                stateFliper.releaseJobToTaskQueueLock();
            }
        }
    }
}
