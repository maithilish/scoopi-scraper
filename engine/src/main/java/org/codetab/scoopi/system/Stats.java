package org.codetab.scoopi.system;

import java.util.LongSummaryStatistics;
import java.util.Timer;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.time.StopWatch;
import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.metrics.SystemStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class Stats {

    private static final Logger LOGGER = LoggerFactory.getLogger(Stats.class);

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
    private ErrorLogger errorLogger;

    @Inject
    private Stats() {
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

    public void outputStats() {
        LOGGER.info("{}", "--- Summary ---");
        long errorCount = errorLogger.getErrorCount();
        if (errorCount == 0) {
            LOGGER.info("scoopi run success");
        } else {
            LOGGER.info("scoopi run errors: {}", errorCount);
            LOGGER.info("see logs/error.log for details");
        }
        LOGGER.info("{}  {}", "time taken:", stopWatch);
    }

    public void outputMemStats() {
        LOGGER.info("{}", "--- Memory Usage ---");
        LOGGER.info("Max   : {}", systemStat.getMaxMemory());
        LOGGER.info("Total : Avg {} High {} Low {}",
                (long) totalMem.getAverage(), totalMem.getMax(),
                totalMem.getMin());
        LOGGER.info("Free : Avg {} High {} Low {}", (long) freeMem.getAverage(),
                freeMem.getMax(), freeMem.getMin());
    }
}
