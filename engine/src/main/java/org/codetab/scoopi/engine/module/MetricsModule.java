package org.codetab.scoopi.engine.module;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.metrics.IMetricsServer;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.metrics.SystemStat;
import org.codetab.scoopi.metrics.serialize.Serializer;
import org.codetab.scoopi.status.ScoopiStatus;
import org.codetab.scoopi.store.ICluster;

import com.google.common.util.concurrent.Uninterruptibles;

public class MetricsModule {

    @Inject
    private Configs configs;
    @Inject
    private ICluster cluster;
    @Inject
    private IMetricsServer metricsServer;
    @Inject
    private MetricsHelper metricsHelper;
    @Inject
    private SystemStat systemStat;
    @Inject
    private ScoopiStatus scoopiStatus;
    @Inject
    private Errors errors;

    private Serializer metricsSerializer;

    public void startMetrics() {
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
    }

    public void stopMetrics() {
        // FIXME is null
        metricsSerializer.stop();
        if (configs.isMetricsServerEnabled()) {
            int period =
                    configs.getInt("scoopi.metrics.serializer.period", "5");
            Uninterruptibles.sleepUninterruptibly(period, TimeUnit.SECONDS);
            metricsServer.stop();
        }
    }

    public void startStats() {
        scoopiStatus.start();
    }

    public void stopStats() {
        scoopiStatus.stop();
    }

    public void startErrors() {
        errors.start();
    }
}
