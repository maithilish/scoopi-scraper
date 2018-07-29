package org.codetab.scoopi;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.codetab.scoopi.defs.ILocatorProvider;
import org.codetab.scoopi.defs.yml.DefsProvider;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.helper.SystemHelper;
import org.codetab.scoopi.messages.Messages;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.metrics.MetricsServer;
import org.codetab.scoopi.metrics.SystemStat;
import org.codetab.scoopi.misc.ShutdownHook;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.model.ModelFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
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
    private DefsProvider defsProvider;
    // @Inject
    // private DataDefService dataDefService;
    @Inject
    private TaskMediator taskMediator;
    @Inject
    private ILocatorProvider locatorProvider;
    @Inject
    private MetricsServer metricsServer;
    @Inject
    private MetricsHelper metricsHelper;
    @Inject
    private StatService statService;

    @Inject
    private ShutdownHook shutdownHook;
    @Inject
    private Runtime runTime;

    @Inject
    private SystemStat systemStat;
    @Inject
    private SystemHelper systemHelper;
    @Inject
    private ModelFactory factory;

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

    public boolean initDefsProvider() {
        defsProvider.init();
        defsProvider.initProviders();
        return true;
    }

    public boolean initDataDefService() {
        // dataDefService.init();
        // int dataDefsCount = dataDefService.getCount();
        // LOGGER.info(Messages.getString("ScoopiSystem.2"), dataDefsCount);
        // //$NON-NLS-1$
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
    public boolean pushInitialPayload() {
        LOGGER.info(Messages.getString("ScoopiSystem.3")); //$NON-NLS-1$
        try {
            String stepName = "start";
            String seederClassName =
                    configService.getConfig("scoopi.seederClass"); //$NON-NLS-1$
            String undefined = "undefined";
            List<LocatorGroup> lGroups = locatorProvider.getLocatorGroups();
            for (LocatorGroup lGroup : lGroups) {
                // for init payload, only stepName, className and taskGroup are
                // set. Next and previous steps, taskName, dataDef are undefined
                StepInfo stepInfo = factory.createStepInfo(stepName, undefined,
                        undefined, seederClassName);
                JobInfo jobInfo = factory.createJobInfo(0, undefined,
                        lGroup.getGroup(), undefined, undefined);
                Payload payload =
                        factory.createPayload(jobInfo, stepInfo, lGroup);
                try {
                    taskMediator.pushPayload(payload);
                } catch (InterruptedException e) {
                    String msg =
                            Util.join(Messages.getString("ScoopiSystem.10"),
                                    lGroup.toString());
                    statService.log(CAT.INTERNAL, msg);
                }
            }
        } catch (ConfigNotFoundException e) {
            throw new CriticalException(Messages.getString("ScoopiSystem.4"), //$NON-NLS-1$
                    e);
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
