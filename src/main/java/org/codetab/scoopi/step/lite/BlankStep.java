package org.codetab.scoopi.step.lite;

import org.codetab.scoopi.step.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlankStep extends Step {

    static final Logger LOGGER = LoggerFactory.getLogger(BlankStep.class);

    @Override
    public boolean initialize() {
        LOGGER.info("initialize step: " + getStepName());
        LOGGER.info(getPayload().getStepInfo().toString());
        LOGGER.info(getPayload().getJobInfo().toString());
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

    @Override
    public boolean process() {
        return true;
    }

}
