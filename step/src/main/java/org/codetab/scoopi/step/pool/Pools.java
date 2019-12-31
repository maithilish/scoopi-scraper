package org.codetab.scoopi.step.pool;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.helper.ThreadSleep;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.metrics.PoolStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public abstract class Pools {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Pools.class);

    /**
     * default pool size.
     */
    private static final int POOL_SIZE = 4;
    /**
     * sleep in ms to check whether tasks are done.
     */
    private static final int SLEEP_MILLIS = 1000;

    /**
     * config service.
     */
    @Inject
    private Configs configs;
    /**
     * helper - metrics
     */
    @Inject
    private MetricsHelper metricsHelper;
    /**
     * DI
     */
    @Inject
    private DInjector di;
    /**
     * pool map - shared state variable.
     */
    @GuardedBy("this")
    private Map<String, ExecutorService> executorsMap;

    /**
     * futures - shared state variable.
     */
    @GuardedBy("this")
    private List<NamedFuture> futures;

    @Inject
    private ThreadSleep threadSleep;

    /**
     * <p>
     * Constructor.
     */
    protected Pools() {
        executorsMap = new HashMap<>();
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
    public synchronized boolean submit(final String poolName,
            final Runnable task) throws RejectedExecutionException {

        notNull(poolName, "poolName must not be null");
        notNull(task, "task must not be null");

        final ExecutorService pool = getPool(poolName);
        final Future<?> f = pool.submit(task);
        final NamedFuture nf = new NamedFuture(poolName, f);
        return futures.add(nf);
    }

    /**
     * <p>
     * Is all tasks are completed.
     * @return true if all tasks are done
     */
    public final synchronized boolean isDone() {
        return getNotDone() == 0;
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
        while (!isDone()) {
            threadSleep.sleep(SLEEP_MILLIS);
        }

        shutdownAll();

        while (!isAllTerminated()) {
            threadSleep.sleep(SLEEP_MILLIS);
        }
        LOGGER.info("pools shutdown complete");
    }

    /**
     * <p>
     * Shutdown all executors. Calling method has to ensure synchronization.
     */
    public synchronized void shutdownAll() {
        executorsMap.values().stream().forEach(ExecutorService::shutdown);
    }

    public boolean isPoolFree(final String poolName) {
        final ThreadPoolExecutor pool = (ThreadPoolExecutor) getPool(poolName);
        System.out.println(poolName + " q: " + pool.getQueue().size() + " "
                + pool.getActiveCount() + " " + pool.getPoolSize() + " "
                + pool.getCorePoolSize());
        return pool.getQueue().size() <= pool.getPoolSize();
    }

    /**
     * <p>
     * If poolName is found in executorsMap the ExecutorService is returned.
     * Otherwise, new FixedThreadPool is created and added to map and returned.
     * Default pool size is 4 and can be override with
     * scoopi.poolsize.<poolName> config.
     * @param poolName
     *            pool to return
     * @return executerService
     */
    private ExecutorService getPool(final String poolName) {
        ExecutorService executor = executorsMap.get(poolName);
        if (executor == null) {
            int poolSize = POOL_SIZE;
            final String key = "scoopi.poolsize." + poolName; //$NON-NLS-1$
            try {
                final String ps = configs.getConfig(key);
                poolSize = Integer.valueOf(ps);
            } catch (NumberFormatException | ConfigNotFoundException e) {
                LOGGER.warn(
                        "pool size not defined for pool: {}, defaults to {}",
                        key, POOL_SIZE);
            }
            executor = Executors.newFixedThreadPool(poolSize);
            LOGGER.info("create executor pool: {}, size: {}", poolName, //$NON-NLS-1$
                    poolSize);
            executorsMap.put(poolName, executor);
            final PoolStat poolStat = di.instance(PoolStat.class);
            poolStat.setThreadPool((ThreadPoolExecutor) executor);
            metricsHelper.registerGuage(poolStat, this, "pool", poolName);
        }

        return executor;
    }

    /**
     * <p>
     * Remove all futures that are completed and then return count of not done.
     * Calling method has to ensure synchronization.
     * @return count of not done tasks.
     */
    private long getNotDone() {
        futures.removeIf(NamedFuture::isDone);
        return futures.size();
    }

    /**
     * <p>
     * Is all executors are terminated.
     * @return true if all are terminated.
     */
    private boolean isAllTerminated() {
        return executorsMap.values().stream()
                .allMatch(ExecutorService::isTerminated);
    }

}
