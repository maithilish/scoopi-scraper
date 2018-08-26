package org.codetab.scoopi.step.base;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.validState;

import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.step.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseConverter extends Step {

    /**
     * logger.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(BaseConverter.class);

    protected Data data;

    @Override
    public boolean initialize() {
        validState(nonNull(getPayload()), "payload is null");
        validState(nonNull(getPayload().getData()), "payload data is null");

        Object pData = getPayload().getData();
        if (pData instanceof Data) {
            data = (Data) pData;
        } else {
            String message =
                    String.join(" ", "payload data type is not Data but",
                            pData.getClass().getName());
            throw new StepRunException(message);
        }
        return true;
    }

    @Override
    public boolean load() {
        return true;
    }

    @Override
    public boolean store() {
        return true;
    }
}
