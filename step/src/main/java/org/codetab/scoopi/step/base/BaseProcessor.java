package org.codetab.scoopi.step.base;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.validState;
import static org.codetab.scoopi.util.Util.spaceit;

import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.step.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseProcessor extends Step {

    /**
     * logger.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(BaseProcessor.class);

    protected Data data;

    @Override
    public void initialize() {
        validState(nonNull(getPayload()), "payload is null");
        validState(nonNull(getPayload().getData()), "payload data is null");

        Object pData = getPayload().getData();
        if (pData instanceof Data) {
            data = (Data) pData;
        } else {
            String message = spaceit("payload data type is not Data but",
                    pData.getClass().getName());
            throw new StepRunException(message);
        }
    }

    @Override
    public void load() {
    }

    @Override
    public void store() {
    }
}
