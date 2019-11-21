package org.codetab.scoopi.step;

import javax.inject.Inject;

import org.codetab.scoopi.exception.StepPersistenceException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.log.Log.CAT;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.TaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.codahale.metrics.Timer.Context;

public class Task implements Runnable {

    static final Logger LOGGER = LoggerFactory.getLogger(Task.class);

    @Inject
    private ErrorLogger errorLogger;
    @Inject
    private MetricsHelper metricsHelper;
    @Inject
    private TaskInfo taskInfo;

    private IStep step;

    public void setStep(final IStep step) {
        this.step = step;
    }

    public IStep getStep() {
        return step;
    }

    @Override
    public void run() {

        Marker marker = step.getMarker();
        String stepLabel = step.getLabel();
        taskInfo.setJobInfo(step.getJobInfo());

        try {
            Context taskTimer =
                    metricsHelper.getTimer(step, "task", "time").time();

            step.setup();
            step.initialize();

            LOGGER.trace(marker, "execute {}", stepLabel);

            step.load();
            step.process();
            step.store();
            step.handover();

            LOGGER.trace(marker, "finish {}", stepLabel);

            taskTimer.stop();

        } catch (StepRunException | StepPersistenceException e) {
            String message = step.getLabeled(e.getMessage());
            errorLogger.log(marker, CAT.ERROR, message, e);
        } catch (Exception e) {
            String message = step.getLabeled(e.getMessage());
            errorLogger.log(marker, CAT.INTERNAL, message, e);
        }
    }
}
