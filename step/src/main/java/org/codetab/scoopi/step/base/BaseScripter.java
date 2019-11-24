package org.codetab.scoopi.step.base;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.validState;

import org.codetab.scoopi.step.Step;

public abstract class BaseScripter extends Step {

    protected Object input;

    @Override
    public boolean initialize() {
        validState(nonNull(getPayload()), "payload is null");
        validState(nonNull(getPayload().getData()), "payload data is null");
        input = getPayload().getData();
        return true;
    }

    @Override
    public boolean load() {
        return false;
    }

    @Override
    public boolean store() {
        return false;
    }

}
