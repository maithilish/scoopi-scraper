package org.codetab.scoopi.defs.yml;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.codetab.scoopi.model.DataDef;

public class DataDefDefData implements Serializable {

    private static final long serialVersionUID = 2668306606378181174L;

    private Map<String, DataDef> dataDefMap;
    private List<DataDef> definedDataDefs;

    public DataDefDefData() {
    }

    public Map<String, DataDef> getDataDefMap() {
        return dataDefMap;
    }

    public void setDataDefMap(final Map<String, DataDef> dataDefMap) {
        this.dataDefMap = dataDefMap;
    }

    public List<DataDef> getDefinedDataDefs() {
        return definedDataDefs;
    }

    public void setDefinedDataDefs(final List<DataDef> definedDataDefs) {
        this.definedDataDefs = definedDataDefs;
    }
}
