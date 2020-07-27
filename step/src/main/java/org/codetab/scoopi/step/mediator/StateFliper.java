package org.codetab.scoopi.step.mediator;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.step.pool.TaskPoolService;
import org.codetab.scoopi.store.IPayloadStore;
import org.codetab.scoopi.store.IShutdown;

import com.google.inject.Singleton;

@Singleton
public class StateFliper {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private IShutdown shutdown;
    @Inject
    private TaskPoolService poolService;
    @Inject
    private IPayloadStore payloadStore;

    private AtomicReference<TMState> tmState =
            new AtomicReference<>(TMState.READY);

    private Semaphore jobToTaskQueueLock = new Semaphore(1, true);
    private Semaphore taskQueueToPoolLock = new Semaphore(1, true);

    public void cancel() {
        shutdown.cancel();
    }

    // FIXME - clusterfix - if example is htmlunit and scoopi.defs.defaultSteps
    // is jsoupDefault then cluster fails to shutdown

    public void tryTMShutdown() {

        boolean payloadStoreDone = payloadStore.isDone();
        boolean poolServiceDone = poolService.isDone();

        if (payloadStoreDone && poolServiceDone) {
            if (shutdown.isCancelled()) {
                tmState.set(TMState.SHUTDOWN);
                LOG.debug("cancel requrested, ignore pending jobs");
                return;
            }
            LOG.info(
                    "payloadStore and poolService done, try taskMediator shutdown");

            shutdown.setDone();

            boolean allNodesDone = shutdown.allNodesDone();
            boolean jobStoreDone = shutdown.jobStoreDone();

            logShutdownStateConditions(payloadStoreDone, poolServiceDone,
                    allNodesDone, jobStoreDone);

            if (allNodesDone && jobStoreDone) {

                try {
                    LOG.debug(
                            "shutdown conditions are met, but may be false positive");
                    LOG.debug(
                            "get shutdown conditions again after acquiring jobToTaskQueueLock and taskQueueToPoolLock");

                    jobToTaskQueueLock.acquireUninterruptibly();
                    taskQueueToPoolLock.acquireUninterruptibly();

                    payloadStoreDone = payloadStore.isDone();
                    poolServiceDone = poolService.isDone();
                    allNodesDone = shutdown.allNodesDone();
                    jobStoreDone = shutdown.jobStoreDone();

                    logShutdownStateConditions(payloadStoreDone,
                            poolServiceDone, allNodesDone, jobStoreDone);

                    if (payloadStoreDone && poolServiceDone && allNodesDone
                            && jobStoreDone) {
                        tmState.set(TMState.SHUTDOWN);
                        LOG.info(
                                "shutdown conditions are met, taskMediator state changed to {}",
                                TMState.SHUTDOWN);
                    }
                } finally {
                    taskQueueToPoolLock.release();
                    jobToTaskQueueLock.release();
                }
            } else {
                tmState.set(TMState.READY);
                LOG.debug(
                        "taskMediator state reset to READY, not all conditions true");
            }
        }
    }

    public TMState getTMState() {
        return tmState.get();
    }

    public void setTMState(final TMState state) {
        this.tmState.set(state);
    }

    public boolean isTMState(final TMState other) {
        return tmState.get().equals(other);
    }

    public void acquireJobToTaskQueueLock(final int timeout,
            final TimeUnit timeoutUnit) throws InterruptedException {
        jobToTaskQueueLock.tryAcquire(timeout, timeoutUnit);
    }

    public void releaseJobToTaskQueueLock() {
        jobToTaskQueueLock.release();
    }

    public void acquireTaskQueueToPoolLock(final int timeout,
            final TimeUnit timeoutUnit) throws InterruptedException {
        taskQueueToPoolLock.tryAcquire(timeout, timeoutUnit);
    }

    public void releaseTaskQueueToPoolLock() {
        taskQueueToPoolLock.release();
    }

    private void logShutdownStateConditions(final boolean payloadStoreDone,
            final boolean poolServiceDone, final boolean allNodesDone,
            final boolean jobStoreDone) {
        LOG.debug("shutdown state conditions [{}: {}, {}: {}, {}: {}, {}: {}]",
                "payloadStore done", payloadStoreDone, "poolService done",
                poolServiceDone, "all nodes done", allNodesDone,
                "jobStore done", jobStoreDone);
    }
}
