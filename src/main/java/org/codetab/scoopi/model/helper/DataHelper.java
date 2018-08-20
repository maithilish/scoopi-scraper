package org.codetab.scoopi.model.helper;

import java.io.IOException;

import javax.inject.Inject;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.codetab.scoopi.defs.yml.DataDefProvider;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper routines to handle documents.
 * @author Maithilish
 *
 */
public class DataHelper {

    static final Logger LOGGER = LoggerFactory.getLogger(Data.class);

    @Inject
    private DataDefProvider dataDefProvider;

    @Inject
    private DataHelper() {
    }

    public Data getDataTemplate(final String dataDefName, final Long documentId,
            final String label) throws DataDefNotFoundException,
            ClassNotFoundException, IOException {
        Data data = dataDefProvider.getDataTemplate(dataDefName);
        data.setName(label);
        data.setDataDefId(dataDefProvider.getDataDefId(dataDefName));
        data.setDocumentId(documentId);
        return data;
    }

    public ScriptEngine getScriptEngine() {
        ScriptEngineManager scriptEngineMgr = new ScriptEngineManager();
        return scriptEngineMgr.getEngineByName("JavaScript"); //$NON-NLS-1$
    }
}
