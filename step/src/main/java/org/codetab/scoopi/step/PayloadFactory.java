package org.codetab.scoopi.step;

import static org.codetab.scoopi.util.Util.spaceit;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.log.Log.CAT;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PayloadFactory {

    static final Logger LOGGER = LoggerFactory.getLogger(PayloadFactory.class);

    @Inject
    private ITaskDef taskDef;
    @Inject
    private TaskMediator taskMediator;
    @Inject
    private ObjectFactory objectFactory;
    @Inject
    private ErrorLogger errorLogger;

    public List<Payload> createSeedPayloads(
            final List<LocatorGroup> locatorGroups, final String stepName,
            final String seederClzName) {
        List<Payload> payloads = new ArrayList<>();
        for (LocatorGroup locatorGroup : locatorGroups) {
            // for init payload, only stepName, className and taskGroup are
            // set. Next and previous steps, taskName, dataDef are undefined
            String undefined = "undefined";
            StepInfo stepInfo = objectFactory.createStepInfo(stepName,
                    undefined, undefined, seederClzName);
            JobInfo jobInfo = objectFactory.createJobInfo(0, undefined,
                    locatorGroup.getGroup(), undefined, undefined, undefined);
            Payload payload = objectFactory.createPayload(jobInfo, stepInfo,
                    locatorGroup);
            payloads.add(payload);
        }
        return payloads;
    }

    public List<Payload> createPayloads(final String taskGroup,
            final List<String> taskNames, final StepInfo stepInfo,
            final String jobName, final Object payloadData) {
        List<Payload> payloads = new ArrayList<>();
        for (String taskName : taskNames) {
            try {
                StepInfo thisStep = stepInfo;
                /*
                 * if stepName is start then it means this stepInfo is not fully
                 * constructed. We have to get proper stepInfo which has
                 * previous=start which normally is the first step. To get that
                 * we use getNextStep() which returns step which has
                 * previous=start. We use it to get proper this step and not the
                 * next step!
                 */
                if (stepInfo.getStepName().equalsIgnoreCase("start")) {
                    thisStep = taskDef.getNextStep(taskGroup, taskName,
                            stepInfo.getStepName());
                }
                if (!thisStep.getNextStepName().equalsIgnoreCase("end")) {
                    String stepsName =
                            taskDef.getStepsName(taskGroup, taskName);
                    String dataDefName = taskDef.getFieldValue(taskGroup,
                            taskName, "dataDef");
                    StepInfo nextStep = taskDef.getNextStep(taskGroup, taskName,
                            thisStep.getStepName());
                    JobInfo jobInfo = objectFactory.createJobInfo(
                            taskMediator.getJobId(), jobName, taskGroup,
                            taskName, stepsName, dataDefName);
                    Payload nextStepPayload = objectFactory
                            .createPayload(jobInfo, nextStep, payloadData);
                    payloads.add(nextStepPayload);
                }
            } catch (DefNotFoundException e) {
                String message = spaceit(
                        "unable to create payload for taskGroup:taskName ",
                        taskGroup + ":" + taskName);
                errorLogger.log(CAT.ERROR, message, e);
            }
        }
        return payloads;
    }
}
