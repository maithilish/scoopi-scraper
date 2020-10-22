package org.codetab.scoopi.step.extract;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.validState;
import static org.codetab.scoopi.util.Util.spaceit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.exception.JobStateException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.exception.TransactionException;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.model.ERROR;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.step.base.BaseSeeder;
import org.codetab.scoopi.step.base.PayloadFactory;
import org.codetab.scoopi.step.mediator.JobMediator;

import com.codahale.metrics.Meter;
import com.google.common.collect.Lists;

/**
 * Creates seeder tasks and handover them to queue.
 * @author Maithilish
 *
 */

public final class LocatorSeeder extends BaseSeeder {

    private static final Logger LOG = LogManager.getLogger();

    /**
     * list of locator don't name the next as locators as it hides field.
     * (checkstyle)
     */
    private LocatorGroup locatorGroup;
    @Inject
    private PayloadFactory payloadFactory;
    @Inject
    private Errors errors;
    @Inject
    private JobMediator jobMediator;
    @Inject
    private JobSeeder jobSeeder;

    /**
     * <p>
     * Initialise list of locators
     */
    @Override
    public void initialize() {
        final Object pData = getPayload().getData();
        if (pData instanceof LocatorGroup) {
            locatorGroup = (LocatorGroup) pData;
            setOutput(pData);
        } else {
            final String message =
                    spaceit("payload data is not instance of locator, but",
                            pData.getClass().getName());
            throw new StepRunException(message);
        }
        final Meter meter = metricsHelper.getMeter(this, "locator", "provided");
        meter.mark(locatorGroup.getLocators().size());
    }

    /**
     * <p>
     * Submit tasks to queue.
     */
    @Override
    public void handover() {
        validState(nonNull(getOutput()), "output is not set");

        final Meter meter = metricsHelper.getMeter(this, "locator", "seeded");

        final String group = locatorGroup.getGroup();
        LOG.debug("push {} locators from locatorGroup {}",
                locatorGroup.getLocators().size(), group);
        LOG.debug("is locator group defined by defs : {}",
                locatorGroup.isByDef());

        int seedRetryTimes =
                configs.getInt("scoopi.seeder.seedRetryTimes", "3");

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
                        for (int r = 1; r <= seedRetryTimes; r++) {
                            LOG.debug("seed {}, try # {}",
                                    payload.getJobInfo().getLabel(), r);
                            boolean logError = false;
                            if (r < seedRetryTimes) {
                                // retry on error else break
                                if (pushPayload(meter, payload, logError)) {
                                    break;
                                }
                            } else {
                                // on last try if error, log it and continue
                                logError = true;
                                pushPayload(meter, payload, logError);
                            }
                        }
                    }
                } else {
                    final String message =
                            spaceit("seed locator:", locator.getName(),
                                    ", expected one payload for taskGroup:",
                                    group, "task:", firstTask.get(), "but got:",
                                    String.valueOf(payloads.size()));
                    errors.inc();
                    LOG.error("{} [{}]", message, ERROR.INTERNAL);
                }

            } else {
                errors.inc();
                LOG.error(
                        "seed locator, get first task for locator group: {} [{}]",
                        group, ERROR.INTERNAL);
            }
        }
        LOG.debug("locator group: {}, locators: {}, queued to taskpool",
                locatorGroup.getGroup(), locatorGroup.getLocators().size());
        if (locatorGroup.isByDef()) {
            jobSeeder.countDownSeedLatch();
        }
    }

    private boolean pushPayload(final Meter meter, final Payload payload,
            final boolean logError) {
        try {
            String pushedTo = null;
            String locatorSource = null;
            if (locatorGroup.isByDef()) {
                // if seed, push to JM (local or cluster)
                pushedTo = "JM";
                locatorSource = "locator by def";

                payload.getJobInfo().setId(jobMediator.getJobIdSequence());
                jobMediator.pushJob(payload);
            } else {
                // if from parse link, push to TM (local). JobId
                // of parent job is reused
                pushedTo = "TM";
                locatorSource = "locator by link";

                final long linkJobId = getPayload().getJobInfo().getId();
                payload.getJobInfo().setId(linkJobId);
                taskMediator.pushPayload(payload);
            }

            LOG.debug("push [{}] to {}, {} jobId {}", locatorSource, pushedTo,
                    payload.getJobInfo().getLabel(),
                    payload.getJobInfo().getId());

            meter.mark();
            return true;
        } catch (InterruptedException | JobStateException
                | TransactionException e) {
            if (logError) {
                // log error for this payload and continue
                errors.inc();
                LOG.error("push locator,{} [{}]", payload, ERROR.INTERNAL, e);
            }
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }
}
