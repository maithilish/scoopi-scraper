package org.codetab.scoopi.engine;

import static org.codetab.scoopi.util.Util.LINE;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.helper.SystemHelper;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.metrics.IMetricsServer;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.metrics.SystemStat;
import org.codetab.scoopi.metrics.serialize.Serializer;
import org.codetab.scoopi.plugin.appender.AppenderMediator;
import org.codetab.scoopi.plugin.pool.AppenderPoolService;
import org.codetab.scoopi.stat.ShutdownHook;
import org.codetab.scoopi.stat.Stats;
import org.codetab.scoopi.step.extract.JobSeeder;
import org.codetab.scoopi.store.IBarricade;
import org.codetab.scoopi.store.ICluster;
import org.codetab.scoopi.store.IJobStore;
import org.codetab.scoopi.store.IShutdown;
import org.codetab.scoopi.store.cluster.hz.CrashCleaner;

import com.google.common.util.concurrent.Uninterruptibles;

public class SystemModule {

    static final Logger LOG = LogManager.getLogger();

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
    private Errors errors;
    @Inject
    private AppenderPoolService appenderPoolService;
    @Inject
    private AppenderMediator appenderMediator;

    @Inject
    private JobSeeder jobSeeder;
    @Inject
    private IJobStore jobStore;
    @Inject
    private IBarricade jobSeedBrricade;

    @Inject
    private ShutdownHook shutdownHook;
    @Inject
    private Runtime runTime;

    @Inject
    private SystemStat systemStat;
    @Inject
    private SystemHelper systemHelper;

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

        final int timeoutDefault = 60;
        int clusterShutdownTimeout = configs
                .getInt("scoopi.cluster.shutdown.timeout", timeoutDefault);
        TimeUnit timeUnit = TimeUnit.valueOf(configs
                .getConfig("scoopi.cluster.shutdown.timeoutUnit", "SECONDS")
                .toUpperCase());

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            shutdown.setTerminate();
            shutdown.terminate();
        });

        try {
            future.get(clusterShutdownTimeout, timeUnit);
            return true;
        } catch (Exception e) {
            LOG.error("failed to shutdown cluster {}", e);
            return false;
        }
    }

    public void initClusterListeners() {
        if (configs.isCluster()) {
            crashCleaner.init();
        }
    }

    public boolean startErrorLogger() {
        errors.start();
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
        // FIXME is null
        metricsSerializer.stop();
        if (configs.isMetricsServerEnabled()) {
            int period =
                    configs.getInt("scoopi.metrics.serializer.period", "5");
            Uninterruptibles.sleepUninterruptibly(period, TimeUnit.SECONDS);
            metricsServer.stop();
        }
        return true;
    }

    public void seedJobs() {
        jobSeedBrricade.setup("jobSeedBarricade");

        // block all nodes except one
        jobSeedBrricade.await();

        if (jobSeedBrricade.isAllowed()) {
            // seeder node
            jobSeeder.clearDanglingJobs();
            jobSeeder.seedLocatorGroups();
            CompletableFuture.runAsync(() -> {
                jobSeeder.awaitForSeedDone();
                jobStore.setState(IJobStore.State.READY);
                jobSeedBrricade.finish();
            });
        } else {
            LOG.info("jobs are already seeded by another node");
        }
    }

    public void waitForFinish() {
        appenderMediator.closeAll();
        appenderPoolService.waitForFinish();
    }

    public void setCleanShutdown() {
        shutdownHook.setCleanShutdown(true);
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
