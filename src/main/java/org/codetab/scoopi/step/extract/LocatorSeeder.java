package org.codetab.scoopi.step.extract;

import static org.apache.commons.lang3.Validate.validState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.helper.ThreadSleep;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.factory.PayloadFactory;
import org.codetab.scoopi.step.base.BaseSeeder;
import org.codetab.scoopi.system.ErrorLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Meter;
import com.google.common.collect.Lists;

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
    @Inject
    private ThreadSleep threadSleep;
    @Inject
    private PayloadFactory payloadFactory;
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

        Meter meter = metricsHelper.getMeter(this, "locator", "seeded");

        LOGGER.debug("push locators to taskpool");
        String group = locatorGroup.getGroup();

        for (Locator locator : locatorGroup.getLocators()) {
            // create and push first task payload for each locator
            // so that loader fetch only one document for each locator
            Optional<String> firstTask = taskDefs.getFirstTaskName(group);
            if (firstTask.isPresent()) {
                ArrayList<String> firstTaskName =
                        Lists.newArrayList(firstTask.get());
                List<Payload> payloads =
                        payloadFactory.createPayloads(group, firstTaskName,
                                getStepInfo(), locator.getName(), locator);
                if (payloads.size() == 1) {
                    for (Payload payload : payloads) {
                        try {
                            taskMediator.pushPayload(payload);
                            meter.mark();
                        } catch (InterruptedException e) {
                            String message = String.join(" ",
                                    "handover locator,", payload.toString());
                            errorLogger.log(CAT.INTERNAL, message, e);
                        }
                    }
                } else {
                    String message = String.join(" ", "unable to seed locator:",
                            locator.getName(),
                            ", expected one payload for taskGroup:", group,
                            "task:", firstTask.get(), "but got:",
                            String.valueOf(payloads.size()));
                    errorLogger.log(CAT.ERROR, message);
                }
            } else {
                String message = String.join(" ",
                        "unable to get first task for locator group:", group);
                errorLogger.log(CAT.ERROR, message);
            }
            threadSleep.sleep(SLEEP_MILLIS);
        }
        LOGGER.debug("locator group: {}, locators: {}, queued to taskpool",
                locatorGroup.getGroup(), locatorGroup.getLocators().size());
        return true;
    }

}
