package org.codetab.scoopi.step.base;

import java.util.Date;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IDataDefDef;
import org.codetab.scoopi.defs.IItemDef;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.Data;

/**
 * Data factory
 * @author Maithilish
 *
 */
public class DataFactory {

    @Inject
    private IDataDefDef dataDefDef;
    @Inject
    private IItemDef itemDef;

    @Inject
    private DataFactory() {
    }

    public Data createData(final String dataDef, final Long documentId,
            final String label, final Date runDateTime)
            throws DataDefNotFoundException {
        Data data = itemDef.getDataTemplate(dataDef);
        data.setName(label);
        data.setDataDef(dataDef);
        data.setDataDefId(dataDefDef.getDataDefId(dataDef));
        data.setDocumentId(documentId);
        data.setRunDate(runDateTime);
        return data;
    }
}
