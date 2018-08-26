package org.codetab.scoopi.step;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.defs.ITaskDefs;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.shared.ConfigService;
import org.codetab.scoopi.shared.StatService;
import org.codetab.scoopi.shared.StepService;
import org.codetab.scoopi.util.MarkerUtil;
import org.codetab.scoopi.util.Util;
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
    private Marker marker;
    private boolean consistent = false;

    @Inject
    protected ConfigService configService;
    @Inject
    protected StepService stepService;
    @Inject
    protected StatService activityService;
    @Inject
    protected MetricsHelper metricsHelper;
    @Inject
    protected ITaskDefs taskDefs;
    @Inject
    protected TaskMediator taskMediator;
    @Inject
    protected ObjectFactory factory;

    @Override
    public boolean handover() {
        Validate.validState(nonNull(output), "output is null");
        Validate.validState(isConsistent(), "step inconsistent");
        try {
            String group = getJobInfo().getGroup();
            String stepName = getStepInfo().getStepName();
            String taskName = getJobInfo().getTask();

            if (!getStepInfo().getNextStepName().equalsIgnoreCase("end")) {
                StepInfo nextStep =
                        taskDefs.getNextStep(group, taskName, stepName);
                Payload nextStepPayload =
                        factory.createPayload(getJobInfo(), nextStep, output);
                taskMediator.pushPayload(nextStepPayload);
                LOGGER.info("handover to step: " + nextStep.getStepName());
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
        if (isNull(marker)) {
            String name = getJobInfo().getName();
            String group = getJobInfo().getGroup();
            String task = getJobInfo().getTask();
            marker = MarkerUtil.getMarker(name, group, task);
        }
        return marker;
    }

    /**
     * get label with []
     */
    @Override
    public String getLabel() {
        String lable = payload.getJobInfo().getLabel();
        return Util.join("[", lable, "]");
    }

    @Override
    public String getLabeled(final String message) {
        return String.join(" ", getLabel(), message); // $NON-NLS-1$
    }

}
