package org.codetab.scoopi.model.helper;

import java.io.IOException;

import javax.inject.Inject;

import org.codetab.scoopi.defs.yml.DataDefDefs;
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
    private DataDefDefs dataDefDefs;

    @Inject
    private DataHelper() {
    }

    public Data getDataTemplate(final String dataDefName, final Long documentId,
            final String label) throws DataDefNotFoundException,
            ClassNotFoundException, IOException {
        Data data = dataDefDefs.getDataTemplate(dataDefName);
        data.setName(label);
        data.setDataDefId(dataDefDefs.getDataDefId(dataDefName));
        data.setDocumentId(documentId);
        return data;
    }
}
