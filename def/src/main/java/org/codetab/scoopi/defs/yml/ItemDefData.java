package org.codetab.scoopi.defs.yml;

import java.util.List;
import java.util.Map;

import org.codetab.scoopi.defs.IDefData;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.Query;

public class ItemDefData implements IDefData {

    private static final long serialVersionUID = 6497738060823923870L;

    private Map<String, List<Axis>> itemAxisMap;
    private Map<String, List<Axis>> dimAxisMap;
    private Map<String, List<Axis>> factAxisMap;
    private Map<String, Query> queryMap;
    private Map<String, ItemAttribute> itemAttributeMap;
    private Map<String, Data> dataTemplateMap;

    public Map<String, List<Axis>> getItemAxisMap() {
        return itemAxisMap;
    }

    public void setItemAxisMap(final Map<String, List<Axis>> itemAxisMap) {
        this.itemAxisMap = itemAxisMap;
    }

    public Map<String, List<Axis>> getDimAxisMap() {
        return dimAxisMap;
    }

    public void setDimAxisMap(final Map<String, List<Axis>> dimAxisMap) {
        this.dimAxisMap = dimAxisMap;
    }

    public Map<String, List<Axis>> getFactAxisMap() {
        return factAxisMap;
    }

    public void setFactAxisMap(final Map<String, List<Axis>> factAxisMap) {
        this.factAxisMap = factAxisMap;
    }

    public Map<String, Query> getQueryMap() {
        return queryMap;
    }

    public void setQueryMap(final Map<String, Query> queryMap) {
        this.queryMap = queryMap;
    }

    public Map<String, ItemAttribute> getItemAttributeMap() {
        return itemAttributeMap;
    }

    public void setItemAttributeMap(
            final Map<String, ItemAttribute> itemAttributeMap) {
        this.itemAttributeMap = itemAttributeMap;
    }

    public Map<String, Data> getDataTemplateMap() {
        return dataTemplateMap;
    }

    public void setDataTemplateMap(final Map<String, Data> dataTemplateMap) {
        this.dataTemplateMap = dataTemplateMap;
    }
}
