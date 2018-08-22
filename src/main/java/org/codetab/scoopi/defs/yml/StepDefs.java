package org.codetab.scoopi.defs.yml;

import javax.inject.Singleton;

import org.codetab.scoopi.defs.IStepDefs;

import com.fasterxml.jackson.databind.JsonNode;

@Singleton
public class StepDefs implements IStepDefs {

    private JsonNode defs;

    /**
     * on first invoke sets defs and subsequent invocations do nothing.
     * @param defs
     */
    public void init(final JsonNode stepDefs) {
        if (this.defs == null) {
            this.defs = stepDefs;
        }
    }

}
