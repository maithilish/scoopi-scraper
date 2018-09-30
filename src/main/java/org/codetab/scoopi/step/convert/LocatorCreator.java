package org.codetab.scoopi.step.convert;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.validState;

import java.util.List;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IDataDefDefs;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.factory.LocatorGroupFactory;
import org.codetab.scoopi.model.factory.PayloadFactory;
import org.codetab.scoopi.step.base.BaseConverter;
import org.codetab.scoopi.system.ErrorLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocatorCreator extends BaseConverter {

    static final Logger LOGGER = LoggerFactory.getLogger(LocatorCreator.class);

    @Inject
    private LocatorGroupFactory locatorGroupFactory;
    @Inject
    private PayloadFactory payloadFactory;
    @Inject
    private IDataDefDefs dataDefDefs;
    @Inject
    private ErrorLogger errorLogger;

    private List<LocatorGroup> locatorGroups;

    @Override
    public boolean process() {
        try {
            String locatorName = getPayload().getJobInfo().getName();
            String dataDefName = getPayload().getJobInfo().getDataDef();
            DataDef dataDef = dataDefDefs.getDataDef(dataDefName);
            locatorGroups = locatorGroupFactory.createLocatorGroups(dataDef,
                    data.getMembers(), locatorName);
            setOutput(locatorGroups);
            setConsistent(true);
            return true;
        } catch (DataDefNotFoundException e) {
            String message = "unable to create locator from link";
            throw new StepRunException(message, e);
        }
    }

    @Override
    public boolean handover() {
        validState(nonNull(getOutput()), "output is null");
        validState(isConsistent(), "step inconsistent");
        validState(nonNull(locatorGroups), "locatorGroups is null");

        String stepName = "start"; //$NON-NLS-1$
        String seederClzName = null;
        try {
            seederClzName = configService.getConfig("scoopi.seederClass"); //$NON-NLS-1$
        } catch (ConfigNotFoundException e) {
            throw new StepRunException("unable to handover", e);
        }

        List<Payload> payloads = payloadFactory
                .createSeedPayloads(locatorGroups, stepName, seederClzName);
        for (Payload payload : payloads) {
            try {
                taskMediator.pushPayload(payload);
            } catch (InterruptedException e) {
                String message = String.join(" ", "handover link locators,",
                        payload.toString());
                errorLogger.log(marker, CAT.INTERNAL, message, e);
            }
        }
        return true;
    }
}
