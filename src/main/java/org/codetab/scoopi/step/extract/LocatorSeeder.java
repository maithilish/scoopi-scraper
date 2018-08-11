package org.codetab.scoopi.step.extract;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.helper.ThreadSleep;
import org.codetab.scoopi.messages.Messages;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.ModelFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.step.base.BaseSeeder;
import org.codetab.scoopi.util.MarkerUtil;
import org.codetab.scoopi.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

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
    private ModelFactory factory;

    /**
     * <p>
     * Initialise list of locators (or forked locators to load test).
     */
    @Override
    public boolean initialize() {
        Object pData = getPayload().getData();
        if (pData instanceof LocatorGroup) {
            locatorGroup = (LocatorGroup) pData;
            setData(pData);
            setConsistent(true);
        } else {
            String message = Util.join(Messages.getString("BaseLoader.28"), //$NON-NLS-1$
                    pData.getClass().getName());
            throw new StepRunException(message);
        }
        // fork for load test
        // List<Locator> forkedLocators =
        // locatorHelper.forkLocators(locatorList);
        // if (forkedLocators.size() > 0) {
        // locatorList = forkedLocators;
        // }
        Meter meter = metricsHelper.getMeter(this, "locator", "provided");
        meter.mark(locatorGroup.getLocators().size());

        logState(Messages.getString("LocatorSeeder.2")); //$NON-NLS-1$
        return true;
    }

    /**
     * <p>
     * Submit tasks to queue.
     */
    @Override
    public boolean handover() {
        Validate.validState(isConsistent(), "step inconsistent");

        LOGGER.info(Messages.getString("LocatorSeeder.5")); //$NON-NLS-1$
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
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            threadSleep.sleep(SLEEP_MILLIS);
        }
        LOGGER.info(Messages.getString("LocatorSeeder.6"), //$NON-NLS-1$
                locatorGroup.getLocators().size(), count);
        return true;
    }

    private List<Payload> getPayloads(final String taskGroup,
            final Locator locator) {
        List<Payload> payloads = new ArrayList<>();
        for (String taskName : taskProvider.getTaskNames(taskGroup)) {
            try {
                StepInfo thisStep = getStepInfo();
                /*
                 * !!! if stepName is start then StepInfo is not fully
                 * constructed and get proper stepInfo where previous=start
                 */
                if (getStepInfo().getStepName().equalsIgnoreCase("start")) {
                    thisStep = taskProvider.getNextStep(taskGroup, taskName,
                            getStepName());
                }
                if (!thisStep.getNextStepName().equalsIgnoreCase("end")) {
                    String dataDefName = taskProvider.getFieldValue(taskGroup,
                            taskName, "dataDef");
                    StepInfo nextStep = taskProvider.getNextStep(taskGroup,
                            taskName, thisStep.getStepName());
                    JobInfo jobInfo = factory.createJobInfo(
                            taskMediator.getJobId(), locator.getName(),
                            taskGroup, taskName, dataDefName);
                    Payload nextStepPayload =
                            factory.createPayload(jobInfo, nextStep, locator);
                    payloads.add(nextStepPayload);
                }
            } catch (DefNotFoundException e) {
                // TODO Auto-generated catch block
                // don't throw Exception just log error
                e.printStackTrace();
            }
        }
        return payloads;
    }

    /**
     * <p>
     * Logs trace with marker.
     * @param message
     *            - message to log {@link String}
     */
    private void logState(final String message) {
        for (Locator locator : locatorGroup.getLocators()) {
            Marker marker =
                    MarkerUtil.getMarker(locator.getName(), locator.getGroup());
            LOGGER.trace(marker, "-- {} --{}{}", message, Util.LINE, locator); //$NON-NLS-1$
        }
    }
}
