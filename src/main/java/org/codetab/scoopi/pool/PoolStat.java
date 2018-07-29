package org.codetab.scoopi.pool;

import java.util.concurrent.ThreadPoolExecutor;

public class PoolStat {

    private ThreadPoolExecutor threadPool;

    protected void setThreadPool(final ThreadPoolExecutor threadPool) {
        this.threadPool = threadPool;
    }

    public int getActiveCount() {
        return threadPool.getActiveCount();
    }

    public int getPoolSize() {
        return threadPool.getPoolSize();
    }

    public long getCompletedTaskCount() {
        return threadPool.getCompletedTaskCount();
    }

    public long getTaskCount() {
        return threadPool.getTaskCount();
    }
}
