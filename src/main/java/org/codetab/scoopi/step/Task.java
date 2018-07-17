package org.codetab.scoopi.step;

import javax.inject.Inject;

import org.codetab.scoopi.exception.StepPersistenceException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.messages.Messages;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.shared.StatService;
import org.codetab.scoopi.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer.Context;

public class Task implements Runnable {

    static final Logger LOGGER = LoggerFactory.getLogger(Task.class);

    @Inject
    private StatService statService;
    @Inject
    private MetricsHelper metricsHelper;

    private IStep step;

    public void setStep(final IStep step) {
        this.step = step;
    }

    public IStep getStep() {
        return step;
    }

    @Override
    public void run() {
        try {
            Context taskTimer = metricsHelper.getTimer(step, "task", "time");
            Marker marker = step.getMarker();
            String label = getLabel();
            String stepType = step.getStepName();

            step.initialize();

            LOGGER.trace(marker, Messages.getString("Task.0"), //$NON-NLS-1$
                    label, stepType);

            step.load();
            step.process();
            step.store();
            step.handover();

            LOGGER.trace(marker, Messages.getString("Task.1"), //$NON-NLS-1$
                    label, stepType);

            taskTimer.stop();

        } catch (StepRunException | StepPersistenceException e) {
            String label = getLabel();
            String message =
                    Util.join("[", step.getStepName(), "] ", e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            LOGGER.error("[{}] {}", label, message); //$NON-NLS-1$
            LOGGER.debug("[{}] [{}]", label, step.getStepName(), e); //$NON-NLS-1$
            statService.log(CAT.ERROR, label, message, e);
            countError();
        } catch (Exception e) {
            String label = getLabel();
            String message =
                    Util.join("[", step.getStepName(), "] ", e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            LOGGER.error("[{}] {}", label, message); //$NON-NLS-1$
            LOGGER.debug("[{}] [{}]", label, step.getStepName(), e); //$NON-NLS-1$
            statService.log(CAT.INTERNAL, label, message, e);
            countError();
        }
    }

    private String getLabel() {
        return step.getLabel();
    }

    private void countError() {
        Counter counter = metricsHelper.getCounter(this, "system", "error");
        counter.inc();
    }
}
