package org.codetab.scoopi.step;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.exception.JobRunException;
import org.codetab.scoopi.exception.StepPersistenceException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.ERROR;
import org.codetab.scoopi.model.TaskInfo;
import org.codetab.scoopi.step.mediator.JobMediator;

import com.codahale.metrics.Timer.Context;

public class Task implements Runnable {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private Errors errors;
    @Inject
    private MetricsHelper metricsHelper;
    @Inject
    private TaskInfo taskInfo;
    @Inject
    private JobMediator jobMediator;

    private IStep step;

    public void setStep(final IStep step) {
        this.step = step;
    }

    public IStep getStep() {
        return step;
    }

    @Override
    public void run() {

        Marker jobMarker = step.getJobMarker();
        Marker jobAbortedMarker = step.getJobAbortedMarker();
        String stepLabel = step.getLabel();
        taskInfo.setJobInfo(step.getJobInfo());

        try {
            Context taskTimer =
                    metricsHelper.getTimer(step, "task", "time").time();

            step.setup();
            step.initialize();

            LOG.trace(jobMarker, "execute {}", stepLabel);

            step.load();
            step.process();
            step.store();
            step.handover();

            LOG.trace(jobMarker, "finish {}", stepLabel);

            taskTimer.stop();
        } catch (JobRunException e) {
            try {
                long jobId = step.getJobInfo().getId();
                jobMediator.resetTakenJob(jobId);
            } catch (Exception e1) {
                errors.inc();
                LOG.error(jobAbortedMarker, "{} [{}]",
                        step.getLabeled(e.getMessage()), ERROR.DATAERROR, e1);
            }
        } catch (StepRunException | StepPersistenceException e) {
            errors.inc();
            LOG.error(jobAbortedMarker, "{} [{}]",
                    step.getLabeled(e.getMessage()), ERROR.DATAERROR, e);
        } catch (Exception e) {
            errors.inc();
            LOG.error(jobAbortedMarker, "{} [{}]",
                    step.getLabeled(e.getMessage()), ERROR.DATAERROR, e);
        }
    }
}
