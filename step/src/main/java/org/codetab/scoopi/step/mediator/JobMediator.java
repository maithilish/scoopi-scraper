package org.codetab.scoopi.step.mediator;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.exception.JobStateException;
import org.codetab.scoopi.exception.TransactionException;
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
            LOG.info("TM wait for finish");
            taskMediator.waitForFinish();
            LOG.info("Job runner join");
            jobRunner.join();
            LOG.info("monitor stop");
            monitor.stop();
            LOG.info("jobstore close");
            jobStore.close();
            LOG.info("shutdown set terminate");
            shutdown.setTerminate();
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
            throws InterruptedException, TransactionException {
        notNull(payloads, "payloads must not be null");
        try {
            jobStore.putJobs(payloads, jobId);
        } catch (JobStateException e) {
            LOG.debug("{}", e.getLocalizedMessage());
        } catch (TransactionException e) {
            jobStore.resetTakenJob(jobId);
        }
    }

    public long getJobIdSequence() {
        return jobStore.getJobIdSeq();
    }

    public void markJobFinished(final long jobId) throws TransactionException {
        try {
            jobStore.markFinished(jobId);
        } catch (JobStateException e) {
            LOG.debug("{}", e.getLocalizedMessage());
        } catch (TransactionException e) {
            jobStore.resetTakenJob(jobId);
        }
    }

    public void resetTakenJob(final long jobId) throws TransactionException {
        jobStore.resetTakenJob(jobId);
    }
}
