package org.codetab.scoopi.step.extract;

import static org.apache.commons.lang3.Validate.validState;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.helper.ThreadSleep;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.step.base.BaseSeeder;
import org.codetab.scoopi.system.ErrorLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Meter;

/**
 * Creates seeder tasks and handover them to queue.
 * @author Maithilish
 *
 */

public final class LocatorSeeder extends BaseSeeder {

    /**
     * logger.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(LocatorSeeder.class);

    /**
     * delay between task submit.
     */
    private static final long SLEEP_MILLIS = 1000;

    /**
     * list of locator don't name the next as locators as it hides field.
     * (checkstyle)
     */
    private LocatorGroup locatorGroup;

    /**
     * helper - thread sleep
     */
    @Inject
    private ThreadSleep threadSleep;

    /**
     * model factory
     */
    @Inject
    private ObjectFactory objectFactory;

    @Inject
    private ErrorLogger errorLogger;

    /**
     * <p>
     * Initialise list of locators
     */
    @Override
    public boolean initialize() {
        Object pData = getPayload().getData();
        if (pData instanceof LocatorGroup) {
            locatorGroup = (LocatorGroup) pData;
            setOutput(pData);
            setConsistent(true);
        } else {
            String message = String.join(" ",
                    "payload data is not instance of locator, but",
                    pData.getClass().getName());
            throw new StepRunException(message);
        }
        Meter meter = metricsHelper.getMeter(this, "locator", "provided");
        meter.mark(locatorGroup.getLocators().size());
        return true;
    }

    /**
     * <p>
     * Submit tasks to queue.
     */
    @Override
    public boolean handover() {
        validState(isConsistent(), "step inconsistent");

        LOGGER.info("push locators to taskpool");

        Meter meter = metricsHelper.getMeter(this, "locator", "seeded");
        int count = 0;
        for (Locator locator : locatorGroup.getLocators()) {
            List<Payload> payloads =
                    getPayloads(locatorGroup.getGroup(), locator);
            for (Payload payload : payloads) {
                try {
                    taskMediator.pushPayload(payload);
                    meter.mark();
                    count++;
                } catch (InterruptedException e) {
                    String message = String.join(" ", "handover locator,",
                            payload.toString());
                    errorLogger.log(CAT.INTERNAL, message, e);
                }
                threadSleep.sleep(SLEEP_MILLIS);
            }
        }
        LOGGER.info("locator group: {}, locators: {}, queued to taskpool: {}",
                locatorGroup.getGroup(), locatorGroup.getLocators().size(),
                count);
        return true;
    }

    // TODO extract it to helper class
    private List<Payload> getPayloads(final String taskGroup,
            final Locator locator) {
        List<Payload> payloads = new ArrayList<>();
        for (String taskName : taskDefs.getTaskNames(taskGroup)) {
            try {
                StepInfo thisStep = getStepInfo();
                /*
                 * !!! if stepName is start then StepInfo is not fully
                 * constructed and get proper stepInfo where previous=start
                 */
                if (getStepInfo().getStepName().equalsIgnoreCase("start")) {
                    thisStep = taskDefs.getNextStep(taskGroup, taskName,
                            getStepName());
                }
                if (!thisStep.getNextStepName().equalsIgnoreCase("end")) {
                    String stepsName =
                            taskDefs.getStepsName(taskGroup, taskName);
                    String dataDefName = taskDefs.getFieldValue(taskGroup,
                            taskName, "dataDef");
                    StepInfo nextStep = taskDefs.getNextStep(taskGroup,
                            taskName, thisStep.getStepName());
                    JobInfo jobInfo = objectFactory.createJobInfo(
                            taskMediator.getJobId(), locator.getName(),
                            taskGroup, taskName, stepsName, dataDefName);
                    Payload nextStepPayload = objectFactory
                            .createPayload(jobInfo, nextStep, locator);
                    payloads.add(nextStepPayload);
                }
            } catch (DefNotFoundException e) {
                String message = String.join(" ",
                        "unable to create payload for taskGroup:taskName ",
                        taskGroup + ":" + taskName);
                errorLogger.log(CAT.ERROR, message, e);
            }
        }
        return payloads;
    }
}
