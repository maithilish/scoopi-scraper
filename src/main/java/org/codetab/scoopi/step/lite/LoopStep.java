package org.codetab.scoopi.step.lite;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.validState;

import javax.inject.Inject;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.step.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoopStep extends Step {

    static final Logger LOGGER = LoggerFactory.getLogger(LoopStep.class);

    /**
     * model factory
     */
    @Inject
    private ObjectFactory factory;

    @Override
    public boolean initialize() {
        LOGGER.info("initialize");
        return true;
    }

    @Override
    public boolean load() {
        return true;
    }

    @Override
    public boolean store() {
        return true;
    }

    @Override
    public boolean process() {
        setOutput(getPayload().getData());
        setConsistent(true);
        return true;
    }

    @Override
    public boolean handover() {
        try {
            validState(nonNull(getOutput()), "data is null");
            validState(isConsistent(), "step inconsistent");

            String group = "lite1";
            String stepName = "step1";
            String taskName = "simpleTask";

            if (!getPayload().getStepInfo().getNextStepName()
                    .equalsIgnoreCase("end")) {
                StepInfo nextStep =
                        taskDefs.getNextStep(group, taskName, stepName);
                JobInfo jobInfo = factory.createJobInfo(0, "acme", group,
                        taskName, getJobInfo().getDataDef());
                Payload nextStepPayload =
                        factory.createPayload(jobInfo, nextStep, getOutput());
                taskMediator.pushPayload(nextStepPayload);
                LOGGER.info("handover to step: " + nextStep.getStepName());
            }
        } catch (DefNotFoundException | InterruptedException
                | IllegalStateException e) {
            throw new StepRunException("unable to handover", e);
        }
        return true;
    }

}
