package org.codetab.scoopi.pool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.metrics.PoolStat;

import net.jcip.annotations.GuardedBy;

public class Pools {

    private static final Logger LOG = LogManager.getLogger();

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

    protected Pools() {
        executorsMap = new ConcurrentHashMap<>();
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
    public ExecutorService getPool(final String poolName,
            final Object guageClz) {
        ExecutorService executor = executorsMap.get(poolName);
        if (executor == null) {
            final int defaultPoolSize = 4;
            final String key = "scoopi.poolsize." + poolName; //$NON-NLS-1$
            int poolSize = configs.getInt(key, defaultPoolSize);

            // lazy init with correct form of double-checked locking idiom
            synchronized (this) {
                if (executorsMap.containsKey(poolName)) {
                    executor = executorsMap.get(poolName);
                } else {
                    executor = Executors.newFixedThreadPool(poolSize);
                    LOG.info("create executor pool: {}, size: {}", poolName, //$NON-NLS-1$
                            poolSize);
                    // ConcurrentHashMap put is safe publish
                    executorsMap.put(poolName, executor);
                    final PoolStat poolStat = di.instance(PoolStat.class);
                    poolStat.setThreadPool((ThreadPoolExecutor) executor);
                    metricsHelper.registerGuage(poolStat, guageClz, "pool",
                            poolName);
                }
            }
        }
        return executor;
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
     * Is all executors are terminated.
     * @return true if all are terminated.
     */
    public boolean isAllTerminated() {
        return executorsMap.values().stream()
                .allMatch(ExecutorService::isTerminated);
    }
}
