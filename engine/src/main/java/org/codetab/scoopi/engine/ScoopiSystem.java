package org.codetab.scoopi.engine;

import static org.codetab.scoopi.util.Util.LINE;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.helper.SystemHelper;
import org.codetab.scoopi.helper.ThreadSleep;
import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.metrics.IMetricsServer;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.metrics.SystemStat;
import org.codetab.scoopi.metrics.serialize.Serializer;
import org.codetab.scoopi.plugin.appender.AppenderMediator;
import org.codetab.scoopi.plugin.pool.AppenderPoolService;
import org.codetab.scoopi.stat.ShutdownHook;
import org.codetab.scoopi.stat.Stats;
import org.codetab.scoopi.store.ICluster;
import org.codetab.scoopi.store.IShutdown;
import org.codetab.scoopi.store.cluster.hz.CrashCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScoopiSystem {

    static final Logger LOGGER = LoggerFactory.getLogger(ScoopiSystem.class);

    @Inject
    private Configs configs;
    @Inject
    private CrashCleaner crashCleaner;
    @Inject
    private IMetricsServer metricsServer;
    @Inject
    private MetricsHelper metricsHelper;
    @Inject
    private Stats stats;
    @Inject
    private ICluster cluster;
    @Inject
    private IShutdown shutdown;
    @Inject
    private ErrorLogger errorLogger;
    @Inject
    private AppenderPoolService appenderPoolService;
    @Inject
    private AppenderMediator appenderMediator;

    @Inject
    private ShutdownHook shutdownHook;
    @Inject
    private Runtime runTime;

    @Inject
    private SystemStat systemStat;
    @Inject
    private SystemHelper systemHelper;
    @Inject
    private ThreadSleep threadSleep;

    private Serializer metricsSerializer;

    public boolean startStats() {
        stats.start();
        return true;
    }

    public boolean stopStats() {
        stats.stop();
        return true;
    }

    /*
     * bootstrap starts cluster and any other init is done here
     */
    public void initCluster() {
        configs.setProperty("scoopi.cluster.memberId", cluster.getMemberId());
    }

    public boolean stopCluster() {
        shutdown.setTerminate();
        shutdown.tryTerminate();
        return true;
    }

    public void initClusterListeners() {
        if (configs.isCluster()) {
            crashCleaner.init();
        }
    }

    public boolean startErrorLogger() {
        errorLogger.start();
        return true;
    }

    public boolean addShutdownHook() {
        runTime.addShutdownHook(shutdownHook);
        return true;
    }

    public boolean startMetrics() {
        Map<String, byte[]> metricsMap = cluster.getMetricsHolder();

        metricsHelper.initMetrics();
        metricsHelper.registerGuage(systemStat, this, "system", "stats");

        // start and schedule metrics json serializer
        int period = Integer.parseInt(
                configs.getConfig("scoopi.metrics.serializer.period", "5"));
        metricsSerializer = metricsHelper
                .startJsonSerializer(cluster.getMemberId(), metricsMap, period);

        if (configs.isMetricsServerEnabled()) {
            metricsServer.setMetricsJsonData(metricsMap);
            metricsServer.start();
        }
        return true;
    }

    public boolean stopMetrics() {
        metricsSerializer.stop();
        if (configs.isMetricsServerEnabled()) {
            int period = Integer.parseInt(
                    configs.getConfig("scoopi.metrics.serializer.period", "5"));
            threadSleep.sleep(period, TimeUnit.SECONDS);
            metricsServer.stop();
        }
        return true;
    }

    public void waitForFinish() {
        appenderMediator.closeAll();
        appenderPoolService.waitForFinish();
    }

    public void waitForInput() {
        String wait = "false"; //$NON-NLS-1$
        try {
            wait = configs.getConfig("scoopi.wait"); //$NON-NLS-1$
        } catch (final ConfigNotFoundException e) {
        }
        if (wait.equalsIgnoreCase("true")) { //$NON-NLS-1$
            systemHelper.gc();
            systemHelper.printToConsole("%s%s", //$NON-NLS-1$
                    "wait to acquire heapdump", LINE);
            systemHelper.printToConsole("%s", //$NON-NLS-1$
                    "Press enter to continue ...");
            try {
                systemHelper.readLine();
            } catch (final IOException e) {
                throw new CriticalException(e);
            }
        }
    }

}
