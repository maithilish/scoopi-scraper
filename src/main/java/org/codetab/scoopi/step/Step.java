package org.codetab.scoopi.step;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.validState;

import javax.inject.Inject;

import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.system.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

/**
 * @author maithilish
 *
 */
public abstract class Step implements IStep {

    static final Logger LOGGER = LoggerFactory.getLogger(Step.class);

    private Object output;
    private Payload payload;
    private boolean consistent = false;
    private String stepLabel;
    protected Marker marker;

    @Inject
    protected ConfigService configService;
    @Inject
    protected TaskFactory taskFactory;
    @Inject
    protected MetricsHelper metricsHelper;
    @Inject
    protected ITaskDef taskDef;
    @Inject
    protected TaskMediator taskMediator;
    @Inject
    protected ObjectFactory factory;

    @Override
    public boolean setup() {
        marker = getJobInfo().getMarker();
        return true;
    }

    @Override
    public boolean handover() {
        validState(nonNull(output), "output is null");
        validState(isConsistent(), "step inconsistent");

        try {
            String group = getJobInfo().getGroup();
            String stepName = getStepInfo().getStepName();
            String taskName = getJobInfo().getTask();

            if (getStepInfo().getNextStepName().equalsIgnoreCase("end")) {
                LOGGER.info(marker, "job: {} finished",
                        getJobInfo().getLabel());
            } else {
                StepInfo nextStep =
                        taskDef.getNextStep(group, taskName, stepName);
                Payload nextStepPayload =
                        factory.createPayload(getJobInfo(), nextStep, output);
                taskMediator.pushPayload(nextStepPayload);
                LOGGER.debug(marker, "{} handover to step: {}", getLabel(),
                        nextStep.getStepName());
            }
        } catch (DefNotFoundException | InterruptedException
                | IllegalStateException e) {
            throw new StepRunException("unable to handover", e);
        }
        return true;
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
    public boolean isConsistent() {
        return consistent && nonNull(output);
    }

    @Override
    public void setConsistent(final boolean consistent) {
        this.consistent = consistent;
    }

    @Override
    public Marker getMarker() {
        return marker;
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
