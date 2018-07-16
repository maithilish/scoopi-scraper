package org.codetab.scoopi.defs.yml;

import javax.inject.Singleton;

import org.codetab.scoopi.defs.IDataDefProvider;

import com.fasterxml.jackson.databind.JsonNode;

@Singleton
public class DataDefProvider implements IDataDefProvider {

    private JsonNode defs;

    /**
     * on first invoke sets defs and subsequent invocations do nothing.
     * @param defs
     */
    public void init(final JsonNode dataDefDefs) {
        if (this.defs == null) {
            this.defs = dataDefDefs;
        }
    }

}
