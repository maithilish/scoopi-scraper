package org.codetab.scoopi.defs.yml;

import static java.util.Objects.nonNull;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.defs.IDataDefDef;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.Fingerprint;
import org.codetab.scoopi.model.helper.Fingerprints;

@Singleton
public class DataDefDef implements IDataDefDef {

    @Inject
    private DataDefDefData data;

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

    @Override
    public Fingerprint getFingerprint(final String dataDefName) {
        String dataDefJson = data.getDataDefMap().get(dataDefName).getDefJson();
        return Fingerprints.fingerprint(dataDefJson.getBytes());
    }

}
