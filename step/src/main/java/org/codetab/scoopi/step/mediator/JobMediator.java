package org.codetab.scoopi.step.mediator;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.exception.JobStateException;
import org.codetab.scoopi.exception.TransactionException;
import org.codetab.scoopi.helper.Snooze;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.model.ERROR;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.store.IJobStore;
import org.codetab.scoopi.store.IShutdown;

@Singleton
public class JobMediator {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private JobRunner jobRunner;
    @Inject
    private TaskMediator taskMediator;
    @Inject
    private IJobStore jobStore;
    @Inject
    private IShutdown shutdown;
    @Inject
    private Monitor monitor;
    @Inject
    private Errors errors;
    @Inject
    private Snooze snooze;

    /**
     * cluster: init connection, create tables. For first node, change jobStore
     * state from NEW to INITIALIZE and set seedJobs to true; for others,
     * seedJobs is false.
     * <p>
     * solo: set state to READY.
     */
    public void init() {
        shutdown.init();
        jobStore.open();
    }

    public void start() {
        jobRunner.start();
        monitor.start();
    }

    public void cancel() {
        jobRunner.cancel();
    }

    public void waitForFinish() {
        try {
            LOG.debug("wait for finish");

            taskMediator.waitForFinish();
            LOG.debug("task mediator finished");

            jobRunner.join();
            LOG.debug("Job runner joined");

            monitor.stop();
            LOG.debug("monitor stopped");

            jobStore.close();
            LOG.debug("jobstore closed");

            shutdown.setTerminate();
            LOG.debug("shutdown terminate set");

        } catch (InterruptedException e) {
            errors.inc();
            LOG.error("wait for finish interrupted [{}]", ERROR.INTERNAL, e);
            Thread.currentThread().interrupt();
        }
    }

    public void pushJob(final Payload payload)
            throws InterruptedException, TransactionException {
        notNull(payload, "payload must not be null");
        jobStore.putJob(payload);
    }

    public void pushJobs(final List<Payload> payloads, final long jobId)
            throws InterruptedException {
        notNull(payloads, "payloads must not be null");
        try {
            jobStore.putJobs(payloads, jobId);
        } catch (JobStateException e) {
            LOG.debug("{}", e.getLocalizedMessage());
        } catch (TransactionException e) {
            final int retryWait = 50;
            while (!jobStore.resetTakenJob(jobId)) {
                snooze.sleepUninterruptibly(retryWait);
                LOG.debug("retry reset {}", jobId);
            }
        }
    }

    public long getJobIdSequence() {
        return jobStore.getJobIdSeq();
    }

    public void markJobFinished(final long jobId) {
        try {
            jobStore.markFinished(jobId);
        } catch (JobStateException e) {
            LOG.debug("{}", e.getLocalizedMessage());
        } catch (TransactionException e) {
            final int retryWait = 50;
            while (!jobStore.resetTakenJob(jobId)) {
                snooze.sleepUninterruptibly(retryWait);
                LOG.debug("retry reset {}", jobId);
            }
        }
    }

    public void resetTakenJob(final long jobId) {
        final int retryWait = 50;
        while (!jobStore.resetTakenJob(jobId)) {
            snooze.sleepUninterruptibly(retryWait);
            LOG.debug("retry reset {}", jobId);
        }
    }
}
