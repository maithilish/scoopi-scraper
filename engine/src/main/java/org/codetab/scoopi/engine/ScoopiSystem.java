package org.codetab.scoopi.engine;

import static org.codetab.scoopi.util.Util.LINE;
import static org.codetab.scoopi.util.Util.spaceit;

import java.util.List;

import javax.inject.Inject;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.ILocatorDef;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.helper.SystemHelper;
import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.log.Log.CAT;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.metrics.MetricsServer;
import org.codetab.scoopi.metrics.SystemStat;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.plugin.appender.AppenderMediator;
import org.codetab.scoopi.plugin.pool.AppenderPoolService;
import org.codetab.scoopi.stat.ShutdownHook;
import org.codetab.scoopi.stat.Stats;
import org.codetab.scoopi.step.PayloadFactory;
import org.codetab.scoopi.step.TaskMediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScoopiSystem {

    static final Logger LOGGER = LoggerFactory.getLogger(ScoopiSystem.class);

    @Inject
    private Configs configs;
    @Inject
    private ILocatorDef locatorDef;
    @Inject
    private TaskMediator taskMediator;
    @Inject
    private MetricsServer metricsServer;
    @Inject
    private MetricsHelper metricsHelper;
    @Inject
    private Stats stats;
    @Inject
    private ErrorLogger errorLogger;
    @Inject
    private PayloadFactory payloadFactory;
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

    public boolean startStats() {
        stats.start();
        return true;
    }

    public boolean stopStats() {
        stats.stop();
        return true;
    }

    public boolean startErrorLogger() {
        errorLogger.start();
        return true;
    }

    public boolean addShutdownHook() {
        runTime.addShutdownHook(shutdownHook);
        return true;
    }

    public boolean startMetricsServer() {
        metricsServer.start();
        metricsHelper.initMetrics();
        metricsHelper.registerGuage(systemStat, this, "system", "stats");
        return true;
    }

    public boolean stopMetricsServer() {
        metricsServer.stop();
        return true;
    }

    public boolean seedLocatorGroups() {
        LOGGER.info("seed defined locator groups");
        String stepName = "start"; //$NON-NLS-1$
        String seederClzName = null;
        try {
            seederClzName = configs.getConfig("scoopi.seederClass"); //$NON-NLS-1$
        } catch (ConfigNotFoundException e) {
            String message = "unable seed locator group";
            throw new CriticalException(message, e);
        }

        List<LocatorGroup> locatorGroups = locatorDef.getLocatorGroups();
        List<Payload> payloads = payloadFactory
                .createSeedPayloads(locatorGroups, stepName, seederClzName);
        for (Payload payload : payloads) {
            try {
                taskMediator.pushPayload(payload);
            } catch (InterruptedException e) {
                String group = payload.getJobInfo().getGroup();
                String message = spaceit("seed locator group: ", group);
                errorLogger.log(CAT.INTERNAL, message, e);
            }
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
        } catch (ConfigNotFoundException e) {
        }
        if (wait.equalsIgnoreCase("true")) { //$NON-NLS-1$
            systemHelper.gc();
            systemHelper.printToConsole("%s%s", //$NON-NLS-1$
                    "wait to acquire heapdump", LINE);
            systemHelper.printToConsole("%s", //$NON-NLS-1$
                    "Press enter to continue ...");
            systemHelper.readLine();
        }
    }
}
