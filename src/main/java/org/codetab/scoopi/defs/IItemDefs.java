package org.codetab.scoopi.defs;

import java.util.List;
import java.util.Map;

import org.codetab.scoopi.model.DataDef;

public interface IItemDefs {

    List<String> getItemNames(DataDef dataDef, String itemName);

    Map<String, String> getQueries(DataDef dataDef, String itemName,
            String fieldName);
}
