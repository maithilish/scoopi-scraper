package org.codetab.scoopi;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.codetab.scoopi.defs.ILocatorDefs;
import org.codetab.scoopi.defs.yml.Defs;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.helper.SystemHelper;
import org.codetab.scoopi.messages.Messages;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.metrics.MetricsServer;
import org.codetab.scoopi.metrics.SystemStat;
import org.codetab.scoopi.misc.ShutdownHook;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.helper.LocatorGroupHelper;
import org.codetab.scoopi.shared.ConfigService;
import org.codetab.scoopi.shared.StatService;
import org.codetab.scoopi.step.TaskMediator;
import org.codetab.scoopi.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScoopiSystem {

    static final Logger LOGGER = LoggerFactory.getLogger(ScoopiSystem.class);

    @Inject
    private ConfigService configService;
    @Inject
    private Defs defs;
    // @Inject
    // private DataDefService dataDefService;
    @Inject
    private TaskMediator taskMediator;
    @Inject
    private ILocatorDefs locatorDefs;
    @Inject
    private MetricsServer metricsServer;
    @Inject
    private MetricsHelper metricsHelper;
    @Inject
    private StatService statService;
    @Inject
    private LocatorGroupHelper locatorGroupHelper;

    @Inject
    private ShutdownHook shutdownHook;
    @Inject
    private Runtime runTime;

    @Inject
    private SystemStat systemStat;
    @Inject
    private SystemHelper systemHelper;

    public boolean startStatService() {
        statService.start();
        return true;
    }

    public boolean stopStatService() {
        statService.stop();
        return true;
    }

    public boolean addShutdownHook() {
        LOGGER.info(Messages.getString("ScoopiSystem.0")); //$NON-NLS-1$
        runTime.addShutdownHook(shutdownHook);
        return true;
    }

    public boolean initConfigService(final String defaultConfigFile,
            final String userConfigFile) {
        configService.init(userConfigFile, defaultConfigFile);
        LOGGER.info(getModeInfo());
        LOGGER.info(Messages.getString("ScoopiSystem.1"), //$NON-NLS-1$
                configService.getRunDate());
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
        String message = "seed defined locator groups";
        LOGGER.info(message); // $NON-NLS-1$
        String stepName = "start"; //$NON-NLS-1$
        String seederClzName = null;
        try {
            seederClzName = configService.getConfig("scoopi.seederClass"); //$NON-NLS-1$
        } catch (ConfigNotFoundException e) {
            throw new CriticalException(message, e);
        }
        List<LocatorGroup> locatorGroups = locatorDefs.getLocatorGroups();
        List<Payload> payloads = locatorGroupHelper
                .createSeedPayloads(locatorGroups, stepName, seederClzName);
        for (Payload payload : payloads) {
            try {
                taskMediator.pushPayload(payload);
            } catch (InterruptedException e) {
                LOGGER.error("{}: {}", message, e.getMessage());
                LOGGER.debug("{}", message, e);
                statService.log(CAT.INTERNAL, message, e);
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
        String modeInfo = Messages.getString("ScoopiSystem.5"); //$NON-NLS-1$
        if (configService.isTestMode()) {
            modeInfo = Messages.getString("ScoopiSystem.6"); //$NON-NLS-1$
        }
        if (configService.isDevMode()) {
            modeInfo = Messages.getString("ScoopiSystem.7"); //$NON-NLS-1$
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
                    Messages.getString("ScoopiSystem.8"), //$NON-NLS-1$
                    Util.LINE);
            systemHelper.printToConsole("%s", //$NON-NLS-1$
                    Messages.getString("ScoopiSystem.9")); //$NON-NLS-1$
            systemHelper.readLine();
        }
    }
}
