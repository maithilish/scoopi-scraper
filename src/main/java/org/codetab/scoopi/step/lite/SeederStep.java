package org.codetab.scoopi.step.lite;

import static java.util.Objects.nonNull;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.step.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeederStep extends Step {

    static final Logger LOGGER = LoggerFactory.getLogger(SeederStep.class);

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
        setData(getPayload().getData());
        setConsistent(true);
        return true;
    }

    @Override
    public boolean handover() {

        Validate.validState(nonNull(getData()), "data is null");
        Validate.validState(isConsistent(), "step inconsistent");

        LOGGER.info("handover");
        String taskGroup = getPayload().getJobInfo().getGroup();
        String stepName = getPayload().getStepInfo().getStepName();
        for (String taskName : taskProvider.getTaskNames(taskGroup)) {
            try {
                String dataDefName = taskProvider.getFieldValue(taskGroup,
                        taskName, "dataDef");
                StepInfo nextStep =
                        taskProvider.getNextStep(taskGroup, taskName, stepName);
                JobInfo jobInfo = factory.createJobInfo(taskMediator.getJobId(),
                        "locator", taskGroup, taskName, dataDefName);
                Payload nextStepPayload =
                        factory.createPayload(jobInfo, nextStep, getData());
                taskMediator.pushPayload(nextStepPayload);
            } catch (DefNotFoundException | InterruptedException e) {
            }

        }
        return true;
    }

}
