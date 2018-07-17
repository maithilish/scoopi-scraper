package org.codetab.scoopi.step;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.defs.ITaskProvider;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.shared.ConfigService;
import org.codetab.scoopi.shared.StatService;
import org.codetab.scoopi.shared.StepService;
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

    private Object data;
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
    protected ITaskProvider taskProvider;
    @Inject
    protected TaskMediator taskMediator;

    @Override
    public boolean handover() {
        try {
            Validate.validState((data != null), "data is null");
            Validate.validState(isConsistent(), "step inconsistent");

            String group = getPayload().getJobInfo().getGroup();
            String stepName = getPayload().getStepInfo().getStepName();
            String taskName = getPayload().getJobInfo().getTask();

            if (!getPayload().getStepInfo().getNextStepName()
                    .equalsIgnoreCase("end")) {
                StepInfo nextStep =
                        taskProvider.getNextStep(group, taskName, stepName);
                Payload nextStepPayload = new Payload();
                nextStepPayload.setData(data);
                nextStepPayload.setStepInfo(nextStep);
                nextStepPayload.setJobInfo(getPayload().getJobInfo());
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
    public Object getData() {
        return data;
    }

    @Override
    public void setData(final Object data) {
        this.data = data;
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
        return consistent;
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
     * get label with []
     */
    @Override
    public String getLabel() {
        String lable = payload.getJobInfo().getLabel();
        return Util.join("[", lable, "] ");
    }

    @Override
    public String getLabeled(final String message) {
        return Util.join(getLabel(), message); // $NON-NLS-1$ //$NON-NLS-2$
    }

}
