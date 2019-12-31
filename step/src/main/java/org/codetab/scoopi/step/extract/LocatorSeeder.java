package org.codetab.scoopi.step.extract;

import static org.apache.commons.lang3.Validate.validState;
import static org.codetab.scoopi.util.Util.spaceit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.helper.ThreadSleep;
import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.log.Log.CAT;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.step.JobMediator;
import org.codetab.scoopi.step.PayloadFactory;
import org.codetab.scoopi.step.base.BaseSeeder;
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
    @Inject
    private JobMediator jobMediator;

    /**
     * <p>
     * Initialise list of locators
     */
    @Override
    public boolean initialize() {
        final Object pData = getPayload().getData();
        if (pData instanceof LocatorGroup) {
            locatorGroup = (LocatorGroup) pData;
            setOutput(pData);
            setConsistent(true);
        } else {
            final String message =
                    spaceit("payload data is not instance of locator, but",
                            pData.getClass().getName());
            throw new StepRunException(message);
        }
        final Meter meter = metricsHelper.getMeter(this, "locator", "provided");
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

        final Meter meter = metricsHelper.getMeter(this, "locator", "seeded");

        LOGGER.debug("push locators to taskpool");
        final String group = locatorGroup.getGroup();

        for (final Locator locator : locatorGroup.getLocators()) {
            // create and push first task payload for each locator
            // so that loader fetch only one document for each locator
            final Optional<String> firstTask = taskDef.getFirstTaskName(group);
            if (firstTask.isPresent()) {
                final ArrayList<String> firstTaskName =
                        Lists.newArrayList(firstTask.get());
                final List<Payload> payloads =
                        payloadFactory.createPayloads(group, firstTaskName,
                                getStepInfo(), locator.getName(), locator);
                if (payloads.size() == 1) {
                    for (final Payload payload : payloads) {
                        try {
                            if (locatorGroup.isByDef()) {
                                // if seed, push to JM (local or cluster)
                                payload.getJobInfo()
                                        .setId(jobMediator.getJobIdSequence());
                                jobMediator.pushPayload(payload);
                            } else {
                                // if from parse link, push to TM (local). JobId
                                // of parent job is reused
                                final long linkJobId =
                                        getPayload().getJobInfo().getId();
                                payload.getJobInfo().setId(linkJobId);
                                taskMediator.pushPayload(payload);
                            }
                            meter.mark();
                        } catch (final InterruptedException e) {
                            final String message = spaceit("handover locator,",
                                    payload.toString());
                            errorLogger.log(CAT.INTERNAL, message, e);
                        }
                    }
                } else {
                    final String message = spaceit("unable to seed locator:",
                            locator.getName(),
                            ", expected one payload for taskGroup:", group,
                            "task:", firstTask.get(), "but got:",
                            String.valueOf(payloads.size()));
                    errorLogger.log(CAT.ERROR, message);
                }
            } else {
                final String message = spaceit(
                        "unable to get first task for locator group:", group);
                errorLogger.log(CAT.ERROR, message);
            }
            threadSleep.sleep(SLEEP_MILLIS);
        }
        if (locatorGroup.isByDef()) {
            jobMediator.countDownSeedDone();
        }
        LOGGER.debug("locator group: {}, locators: {}, queued to taskpool",
                locatorGroup.getGroup(), locatorGroup.getLocators().size());
        return true;
    }

}
