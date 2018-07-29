package org.codetab.scoopi.pool;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.messages.Messages;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.shared.ConfigService;
import org.codetab.scoopi.util.MarkerUtil;
import org.codetab.scoopi.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

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
    private ConfigService configService;
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

        Validate.notNull(poolName, Messages.getString("Pools.0")); //$NON-NLS-1$
        Validate.notNull(task, Messages.getString("Pools.1")); //$NON-NLS-1$

        ExecutorService pool = getPool(poolName);
        Future<?> f = pool.submit(task);
        NamedFuture nf = new NamedFuture(poolName, f);
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
        Marker marker = MarkerUtil.getMarker("POOL_STATE"); //$NON-NLS-1$
        while (!isDone()) {
            LOGGER.info(marker, "{}", getPoolState()); //$NON-NLS-1$
            try {
                Thread.sleep(SLEEP_MILLIS);
            } catch (InterruptedException e) {
                LOGGER.warn(Messages.getString("Pools.3")); //$NON-NLS-1$
            }
        }

        shutdownAll();

        while (!isAllTerminated()) {
            try {
                Thread.sleep(SLEEP_MILLIS);
            } catch (InterruptedException e) {
                LOGGER.warn(Messages.getString("Pools.3")); //$NON-NLS-1$
            }
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
            String key = "scoopi.poolsize." + poolName; //$NON-NLS-1$
            try {
                String ps = configService.getConfig(key);
                poolSize = Integer.valueOf(ps);
            } catch (NumberFormatException | ConfigNotFoundException e) {
                LOGGER.warn(Messages.getString("Pools.2"), //$NON-NLS-1$
                        key, POOL_SIZE);
            }
            executor = Executors.newFixedThreadPool(poolSize);
            LOGGER.info(Messages.getString("Pools.4"), poolName, //$NON-NLS-1$
                    poolSize);
            executorsMap.put(poolName, executor);
            PoolStat poolStat = di.instance(PoolStat.class);
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

    /**
     * <p>
     * Get pool state summary.
     * <p>
     *
     * <pre>
     * seeder : [Running, pool size = 1, active threads = 0, queued tasks = 0, completed tasks = 1]
     * parser : [Running, pool size = 3, active threads = 2, queued tasks = 0, completed tasks = 1]
     * loader : [Running, pool size = 1, active threads = 0, queued tasks = 0, completed tasks = 1]
     * </pre>
     *
     * @return pool wise summary of state.
     */
    private String getPoolState() {
        final int padLength = 10;
        StringBuilder sb = new StringBuilder();
        for (String key : executorsMap.keySet()) {
            sb.append(StringUtils.rightPad(key, padLength));
            sb.append("  ["); //$NON-NLS-1$
            sb.append(StringUtils
                    .substringAfter(executorsMap.get(key).toString(), "[")); //$NON-NLS-1$
            sb.append(Util.LINE);

        }
        return sb.toString();
    }
}
