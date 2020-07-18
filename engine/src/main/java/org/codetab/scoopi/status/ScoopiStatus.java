package org.codetab.scoopi.status;

import java.util.LongSummaryStatistics;
import java.util.Timer;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.metrics.SystemStat;

@Singleton
public class ScoopiStatus {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private Timer timer;
    @Inject
    private StopWatch stopWatch;
    @Inject
    private MemoryTask memoryTask;
    @Inject
    private LongSummaryStatistics totalMem;
    @Inject
    private LongSummaryStatistics freeMem;
    @Inject
    private SystemStat systemStat;
    @Inject
    private Errors errors;

    @Inject
    private ScoopiStatus() {
    }

    public void start() {
        stopWatch.start();

        final long memoryPollFrequency = 5000;
        timer.schedule(memoryTask, 0, memoryPollFrequency);
    }

    public void stop() {
        timer.cancel();
        stopWatch.stop();
    }

    public void collectMemStats() {
        freeMem.accept(systemStat.getFreeMemory());
        totalMem.accept(systemStat.getTotalMemory());
    }

    public void outputStats(final boolean cancelled) {
        LOG.info("{}", "");
        LOG.info("{}", "--- Summary ---");
        if (cancelled) {
            LOG.info("Status: scoopi run cancelled");
        } else {
            long errorCount = errors.getCount();
            if (errorCount == 0) {
                LOG.info("Status: normal shutdown, scoopi run successful");
            } else {
                LOG.info("Status: normal shutdown but with errors: {}",
                        errorCount);
                LOG.info("see logs/error.log for details");
            }
        }
        LOG.info("Time taken: {}", stopWatch);
    }

    public void outputMemStats() {
        LOG.info("{}", "");
        LOG.info("{}", "--- Memory Usage ---");
        LOG.info("Max   : {}", systemStat.getMaxMemory());
        LOG.info("Total : Avg {} High {} Low {}", (long) totalMem.getAverage(),
                totalMem.getMax(), totalMem.getMin());
        LOG.info("Free : Avg {} High {} Low {}", (long) freeMem.getAverage(),
                freeMem.getMax(), freeMem.getMin());
    }
}
