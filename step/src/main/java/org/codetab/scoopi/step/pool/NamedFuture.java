package org.codetab.scoopi.step.pool;

import java.util.concurrent.Future;

import net.jcip.annotations.Immutable;

/**
 * <p>
 * Future holder with pool name.
 * @author Maithilish
 *
 */
@Immutable
public class NamedFuture {

    /**
     * pool to which future belongs.
     */
    private final String poolName;
    /**
     * future.
     */
    private final Future<?> future;

    /**
     * Constructor.
     * <p>
     * @param poolName
     *            pool which returned the future
     * @param future
     *            future
     */
    public NamedFuture(final String poolName, final Future<?> future) {
        this.poolName = poolName;
        this.future = future;
    }

    /**
     * <p>
     * Get pool name.
     * @return pool name
     */
    public String getPoolName() {
        return poolName;
    }

    /**
     * <p>
     * Get future.
     * @return future
     */
    public Future<?> getFuture() {
        return future;
    }

    /**
     * <p>
     * Is future completed.
     * @return true if future is done
     */
    public boolean isDone() {
        return future.isDone();
    }

}
