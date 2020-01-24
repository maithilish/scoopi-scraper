package org.codetab.scoopi.step.process;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.validState;
import static org.codetab.scoopi.util.Util.spaceit;

import java.util.List;

import javax.inject.Inject;

import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.JobStateException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.step.PayloadFactory;
import org.codetab.scoopi.step.base.BaseProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocatorCreator extends BaseProcessor {

    static final Logger LOGGER = LoggerFactory.getLogger(LocatorCreator.class);

    @Inject
    private LocatorGroupFactory locatorGroupFactory;
    @Inject
    private PayloadFactory payloadFactory;

    private List<LocatorGroup> locatorGroups;

    private long jobId;

    @Override
    public boolean process() {
        final String locatorName = getPayload().getJobInfo().getName();
        final String dataDef = getPayload().getJobInfo().getDataDef();
        jobId = getPayload().getJobInfo().getId();
        locatorGroups = locatorGroupFactory.createLocatorGroups(dataDef,
                data.getItems(), locatorName);
        setOutput(locatorGroups);
        setConsistent(true);
        return true;
    }

    @Override
    public boolean handover() {
        validState(nonNull(getOutput()), "output is null");
        validState(isConsistent(), "step inconsistent");
        validState(nonNull(locatorGroups), "locatorGroups is null");

        final String stepName = "start"; //$NON-NLS-1$
        String seederClzName = null;
        try {
            seederClzName = configs.getConfig("scoopi.seeder.class"); //$NON-NLS-1$
        } catch (final ConfigNotFoundException e) {
            throw new StepRunException("unable to handover", e);
        }

        final List<Payload> payloads = payloadFactory
                .createSeedPayloads(locatorGroups, stepName, seederClzName);
        for (final Payload payload : payloads) {
            payload.getJobInfo().setId(jobMediator.getJobIdSequence());
        }

        // mark this job as finished and push new task jobs for this document
        try {
            jobMediator.pushPayloads(payloads, jobId);
        } catch (InterruptedException | JobStateException e) {
            final String message =
                    spaceit("handover link locators,", getPayload().toString());
            throw new StepRunException(message, e);
        }
        return true;
    }
}
