package org.codetab.scoopi;

import javax.inject.Inject;

import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.messages.Messages;
import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.shared.StatService;
import org.codetab.scoopi.step.TaskMediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScoopiEngine {

    static final Logger LOGGER = LoggerFactory.getLogger(ScoopiEngine.class);

    @Inject
    private StatService activityService;
    @Inject
    private ScoopiSystem gSystem;
    @Inject
    private TaskMediator taskMediator;

    /*
     * single thread env throws CriticalException and terminates the app and
     * multi thread env may also throw CriticalException but they terminates
     * just the executing thread
     *
     */
    public void start() {
        LOGGER.info(Messages.getString("ScoopiEngine.0")); //$NON-NLS-1$
        activityService.start();
        try {
            // single thread env
            gSystem.initSystem();
            gSystem.pushInitialPayload();
            LOGGER.info(Messages.getString("ScoopiEngine.1")); //$NON-NLS-1$
            gSystem.waitForHeapDump();

            LOGGER.info(Messages.getString("ScoopiEngine.2")); //$NON-NLS-1$

            taskMediator.start();
            taskMediator.waitToFinish();

            gSystem.waitForHeapDump();
            LOGGER.info(Messages.getString("ScoopiEngine.3")); //$NON-NLS-1$

        } catch (CriticalException e) {
            LOGGER.error("{}", e.getMessage()); //$NON-NLS-1$
            LOGGER.warn(Messages.getString("ScoopiEngine.5")); //$NON-NLS-1$
            LOGGER.debug("{}", e); //$NON-NLS-1$
            activityService.log(CAT.FATAL, e.getMessage(), e);
        }
        gSystem.stopMetricsServer();
        activityService.end();
        LOGGER.info(Messages.getString("ScoopiEngine.7")); //$NON-NLS-1$
    }

}
