package org.codetab.scoopi.step.base;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.validState;

import org.codetab.scoopi.step.Step;

public abstract class BaseScripter extends Step {

    protected Object input;

    @Override
    public void initialize() {
        validState(nonNull(getPayload()), "payload is null");
        validState(nonNull(getPayload().getData()), "payload data is null");
        input = getPayload().getData();
    }

    @Override
    public void load() {
    }

    @Override
    public void store() {
    }
}
