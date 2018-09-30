package org.codetab.scoopi.model.factory;

import javax.inject.Inject;

import org.codetab.scoopi.defs.yml.DataDefDefs;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data factory
 * @author Maithilish
 *
 */
public class DataFactory {

    static final Logger LOGGER = LoggerFactory.getLogger(DataFactory.class);

    @Inject
    private DataDefDefs dataDefDefs;

    @Inject
    private DataFactory() {
    }

    public Data createData(final String dataDef, final Long documentId,
            final String label) throws DataDefNotFoundException {
        Data data = dataDefDefs.getDataTemplate(dataDef);
        data.setName(label);
        data.setDataDef(dataDef);
        data.setDataDefId(dataDefDefs.getDataDefId(dataDef));
        data.setDocumentId(documentId);
        return data;
    }
}
