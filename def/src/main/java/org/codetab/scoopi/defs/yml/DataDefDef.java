package org.codetab.scoopi.defs.yml;

import static java.util.Objects.nonNull;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.defs.IDataDefDef;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.DataDef;

@Singleton
public class DataDefDef implements IDataDefDef {

    @Inject
    private DataDefDefs dataDefDefs;
    @Inject
    private DataDefDefData data;

    // FIXME - persistence fix, remove this after
    @Override
    public void updateDataDefs(final List<DataDef> dataDefs) {
        try {
            dataDefDefs.setDefs(dataDefs);
            data.setDataDefMap(dataDefDefs.toMap(dataDefs));
        } catch (IOException e) {
            throw new CriticalException("unable to update datadefs", e);
        }
    }

    @Override
    public Long getDataDefId(final String name)
            throws DataDefNotFoundException {
        DataDef dataDef = data.getDataDefMap().get(name);
        if (nonNull(dataDef)) {
            return dataDef.getId();
        } else {
            throw new DataDefNotFoundException(name);
        }
    }

    @Override
    public List<DataDef> getDefinedDataDefs() {
        return data.getDefinedDataDefs();
    }

}
