package org.codetab.scoopi.pool;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.util.concurrent.Uninterruptibles;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * <p>
 * Clients can submit tasks by pool name and check whether tasks are done. It
 * creates and holds a map of pools (ExecutorService).
 * @author Maithilish
 *
 */
@ThreadSafe
public abstract class PoolService {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private Pools pools;

    /**
     * futures - shared state variable.
     */
    @GuardedBy("this")
    private List<NamedFuture> futures;

    /**
     * <p>
     * Constructor.
     */
    protected PoolService() {
        futures = new ArrayList<>();
    }

    /**
     * <p>
     * Submit task to a pool.
     * @param poolName
     *            pool name, not null
     * @param task
     *            runnable task, not null
     * @return true if future returned by pool is successfully added to list of
     *         futures.
     * @throws RejectedExecutionException
     *             if task is not scheduled for execution.
     */
    public boolean submit(final String poolName, final Runnable task)
            throws RejectedExecutionException {

        notNull(poolName, "poolName must not be null");
        notNull(task, "task must not be null");
        ExecutorService pool = pools.getPool(poolName, this);
        Future<?> f = pool.submit(task);
        NamedFuture nf = new NamedFuture(poolName, f);
        synchronized (this) {
            return futures.add(nf);
        }
    }

    /**
     * <p>
     * Is all tasks are completed.
     * <p>
     * Remove completed futures and check futures list is empty. Calling method
     * has to ensure synchronization.
     * @return true if all tasks are done
     */
    public final boolean isDone() {
        synchronized (this) {
            futures.removeIf(NamedFuture::isDone);
            return futures.isEmpty();
        }
    }

    /**
     * <p>
     * Gracefully wait for completion.
     * <p>
     * <ul>
     * <li>A. loop until all tasks are done.
     * <li>B. shutdown all pool. [synchronized block]
     * <li>C. wait for all running tasks to complete.
     * </ul>
     * <p>
     * Even though, A breaks after all tasks are completed, between A and B,
     * tasks may get submitted. Hence, B is in synchronized block, so that no
     * further tasks are accepted by pools. C waits for leftover tasks to
     * finish.
     * <p>
     */
    public void waitForFinish() {
        final long spinSleep = 1000;
        while (!isDone()) {
            Uninterruptibles.sleepUninterruptibly(spinSleep,
                    TimeUnit.MILLISECONDS);
        }

        pools.shutdownAll();

        while (!pools.isAllTerminated()) {
            Uninterruptibles.sleepUninterruptibly(spinSleep,
                    TimeUnit.MILLISECONDS);
        }
        LOG.info("pools shutdown complete");
    }
}
