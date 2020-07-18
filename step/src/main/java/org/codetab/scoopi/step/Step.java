package org.codetab.scoopi.step;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.validState;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.JobStateException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.exception.TransactionException;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.step.mediator.JobMediator;
import org.codetab.scoopi.step.mediator.TaskMediator;

/**
 * @author maithilish
 *
 */
public abstract class Step implements IStep {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    protected Configs configs;
    @Inject
    protected MetricsHelper metricsHelper;
    @Inject
    protected ITaskDef taskDef;
    @Inject
    protected TaskMediator taskMediator;
    @Inject
    protected JobMediator jobMediator;
    @Inject
    protected ObjectFactory factory;

    private Object output;
    private Payload payload;
    private String stepLabel;
    protected Marker jobMarker;
    protected Marker jobAbortedMarker;

    @Override
    public void setup() {
        jobMarker = getJobInfo().getJobMarker();
        jobAbortedMarker = getJobInfo().getJobAbortedMarker();
    }

    @Override
    public void handover() {
        validState(nonNull(output), "output is null");

        try {
            final String group = getJobInfo().getGroup();
            final String stepName = getStepInfo().getStepName();
            final String taskName = getJobInfo().getTask();

            if (getStepInfo().getNextStepName().equalsIgnoreCase("end")) {
                long jobId = getJobInfo().getId();
                jobMediator.markJobFinished(jobId);
                LOG.info(jobMarker, "job: {} finished",
                        getJobInfo().getLabel());
            } else {
                final StepInfo nextStep =
                        taskDef.getNextStep(group, taskName, stepName);
                final Payload nextStepPayload =
                        factory.createPayload(getJobInfo(), nextStep, output);
                taskMediator.pushPayload(nextStepPayload);
                LOG.debug(jobMarker, "{} handover to step: {}", getLabel(),
                        nextStep.getStepName());
            }
        } catch (DefNotFoundException | InterruptedException | JobStateException
                | IllegalStateException | TransactionException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new StepRunException("unable to handover", e);
        }
    }

    @Override
    public Object getOutput() {
        return output;
    }

    @Override
    public void setOutput(final Object output) {
        this.output = output;
    }

    @Override
    public Payload getPayload() {
        return payload;
    }

    @Override
    public void setPayload(final Payload payload) {
        this.payload = payload;
    }

    @Override
    public JobInfo getJobInfo() {
        return payload.getJobInfo();
    }

    @Override
    public StepInfo getStepInfo() {
        return payload.getStepInfo();
    }

    @Override
    public String getStepName() {
        return payload.getStepInfo().getStepName();
    }

    @Override
    public Marker getJobMarker() {
        return jobMarker;
    }

    @Override
    public Marker getJobAbortedMarker() {
        return jobAbortedMarker;
    }

    /**
     * get label with step name
     */
    @Override
    public String getLabel() {
        if (isNull(stepLabel)) {
            stepLabel = String.join("", "step: ", getStepName(), ", job: [",
                    payload.getJobInfo().getLabel(), "]");
        }
        return stepLabel;
    }

    @Override
    public String getLabeled(final String message) {
        return String.join(", ", getLabel(), message);
    }

}
