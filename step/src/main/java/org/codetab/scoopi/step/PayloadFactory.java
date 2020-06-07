package org.codetab.scoopi.step;

import static org.codetab.scoopi.util.Util.spaceit;

import java.io.IOException;
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
    private ObjectFactory objectFactory;
    @Inject
    private ErrorLogger errorLogger;

    public List<Payload> createSeedPayloads(
            final List<LocatorGroup> locatorGroups, final String stepName,
            final String seederClzName) {
        final List<Payload> payloads = new ArrayList<>();
        for (final LocatorGroup locatorGroup : locatorGroups) {
            // for init payload, only stepName, className and taskGroup are
            // set. Next and previous steps, taskName, dataDef are undefined
            final String undefined = "undefined";
            final StepInfo stepInfo = objectFactory.createStepInfo(stepName,
                    undefined, undefined, seederClzName);
            final JobInfo jobInfo = objectFactory.createJobInfo(undefined,
                    locatorGroup.getGroup(), undefined, undefined, undefined);
            final Payload payload = objectFactory.createPayload(jobInfo,
                    stepInfo, locatorGroup);
            payloads.add(payload);
        }
        return payloads;
    }

    public List<Payload> createPayloads(final String taskGroup,
            final List<String> taskNames, final StepInfo stepInfo,
            final String jobName, final Object payloadData) {
        final List<Payload> payloads = new ArrayList<>();
        for (final String taskName : taskNames) {
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
                    final String stepsName =
                            taskDef.getStepsName(taskGroup, taskName);
                    final String dataDefName = taskDef.getFieldValue(taskGroup,
                            taskName, "dataDef");
                    final StepInfo nextStep = taskDef.getNextStep(taskGroup,
                            taskName, thisStep.getStepName());
                    final JobInfo jobInfo = objectFactory.createJobInfo(jobName,
                            taskGroup, taskName, stepsName, dataDefName);
                    final Payload nextStepPayload = objectFactory
                            .createPayload(jobInfo, nextStep, payloadData);
                    payloads.add(nextStepPayload);
                }
            } catch (final DefNotFoundException | IOException e) {
                final String message = spaceit(
                        "unable to create payload for taskGroup:taskName ",
                        taskGroup + ":" + taskName);
                errorLogger.log(CAT.ERROR, message, e);
            }
        }
        return payloads;
    }
}
