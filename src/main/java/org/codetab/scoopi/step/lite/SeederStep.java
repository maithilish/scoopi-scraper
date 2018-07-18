package org.codetab.scoopi.step.lite;

import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.step.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeederStep extends Step {

    static final Logger LOGGER = LoggerFactory.getLogger(SeederStep.class);

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
        setData("dummy");
        setConsistent(true);
        return true;
    }

    @Override
    public boolean handover() {

        Validate.validState((getData() != null), "data is null");
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
                JobInfo jobInfo = new JobInfo(taskMediator.getJobId(),
                        "locator", taskGroup, taskName, dataDefName);
                Payload nextStepPayload = new Payload();
                nextStepPayload.setData(getData());
                nextStepPayload.setStepInfo(nextStep);
                nextStepPayload.setJobInfo(jobInfo);
                taskMediator.pushPayload(nextStepPayload);
            } catch (DefNotFoundException | InterruptedException e) {
                // TODO Auto-generated catch block
                // don't throw Exception just log error
                e.printStackTrace();
            }

        }
        return true;
    }

}
