package org.codetab.scoopi;

import static org.codetab.scoopi.util.Util.LINE;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.codetab.scoopi.defs.ILocatorDefs;
import org.codetab.scoopi.defs.yml.Defs;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.helper.SystemHelper;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.metrics.MetricsServer;
import org.codetab.scoopi.metrics.SystemStat;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.helper.LocatorGroupHelper;
import org.codetab.scoopi.plugin.appender.AppenderMediator;
import org.codetab.scoopi.pool.AppenderPoolService;
import org.codetab.scoopi.step.TaskMediator;
import org.codetab.scoopi.system.ConfigService;
import org.codetab.scoopi.system.ErrorLogger;
import org.codetab.scoopi.system.ShutdownHook;
import org.codetab.scoopi.system.Stats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScoopiSystem {

    static final Logger LOGGER = LoggerFactory.getLogger(ScoopiSystem.class);

    @Inject
    private ConfigService configService;
    @Inject
    private Defs defs;
    @Inject
    private TaskMediator taskMediator;
    @Inject
    private ILocatorDefs locatorDefs;
    @Inject
    private MetricsServer metricsServer;
    @Inject
    private MetricsHelper metricsHelper;
    @Inject
    private Stats stats;
    @Inject
    private ErrorLogger errorLogger;
    @Inject
    private LocatorGroupHelper locatorGroupHelper;
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

    public boolean initConfigService(final String defaultConfigFile,
            final String userConfigFile) {
        configService.init(userConfigFile, defaultConfigFile);
        LOGGER.info(getModeInfo());
        LOGGER.info("rundate {}", configService.getRunDate());
        return true;
    }

    public boolean initDefs() {
        defs.init();
        defs.initDefProviders();
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

    /*
     *
     */
    public boolean seedLocatorGroups() {
        LOGGER.info("seed defined locator groups");
        String stepName = "start"; //$NON-NLS-1$
        String seederClzName = null;
        try {
            seederClzName = configService.getConfig("scoopi.seederClass"); //$NON-NLS-1$
        } catch (ConfigNotFoundException e) {
            String message = "unable seed locator group";
            throw new CriticalException(message, e);
        }

        List<LocatorGroup> locatorGroups = locatorDefs.getLocatorGroups();
        List<Payload> payloads = locatorGroupHelper
                .createSeedPayloads(locatorGroups, stepName, seederClzName);
        for (Payload payload : payloads) {
            try {
                taskMediator.pushPayload(payload);
            } catch (InterruptedException e) {
                String group = payload.getJobInfo().getGroup();
                String message =
                        String.join(" ", "seed locator group: ", group);
                errorLogger.log(CAT.INTERNAL, message, e);
            }
        }
        return true;
    }

    /**
     * Get user defined properties file name. The properties file to be is used
     * as user defined properties is set either through environment variable or
     * system property.
     * <p>
     * <ul>
     * <li>if system property [scoopi.propertyFile] is set then its value is
     * used</li>
     * <li>else if system property [scoopi.mode=dev] is set then
     * scoopi-dev.properties file is used</li>
     * <li>else environment variable [scoopi_property_file] is set then its
     * value is used</li>
     * <li>when none of above is set, then default file scoopi.properties file
     * is used</li>
     * </ul>
     * </p>
     *
     * @return
     */
    public String getPropertyFileName() {
        String fileName = null;

        String system = System.getProperty("scoopi.propertyFile"); //$NON-NLS-1$
        if (system != null) {
            fileName = system;
        }

        if (fileName == null) {
            String mode = System.getProperty("scoopi.mode", "prod");
            if (StringUtils.equalsIgnoreCase(mode, "dev")) {
                fileName = "scoopi-dev.properties";
            }
        }

        if (fileName == null) {
            fileName = System.getenv("scoopi_property_file"); //$NON-NLS-1$
        }

        // default nothing is set then production property file
        if (fileName == null) {
            fileName = "scoopi.properties"; //$NON-NLS-1$
        }
        return fileName;
    }

    public String getModeInfo() {
        String modeInfo = "mode: production";
        if (configService.isTestMode()) {
            modeInfo = "mode: test";
        }
        if (configService.isDevMode()) {
            modeInfo = "mode: dev";
        }
        return modeInfo;
    }

    public void waitForHeapDump() {
        String wait = "false"; //$NON-NLS-1$
        try {
            wait = configService.getConfig("scoopi.waitForHeapDump"); //$NON-NLS-1$
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

    public void waitForFinish() {
        appenderMediator.closeAll();
        appenderPoolService.waitForFinish();
    }
}
