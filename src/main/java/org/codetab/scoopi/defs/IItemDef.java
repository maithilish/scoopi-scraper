package org.codetab.scoopi.defs;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.Range;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.Filter;
import org.codetab.scoopi.model.Query;

public interface IItemDef {

    void init(Object dataDefs) throws DefNotFoundException;

    Data getDataTemplate(String dataDef);

    Query getQuery(String dataDef);

    // item attributes
    Optional<Query> getItemQuery(String dataDef, String itemName);

    // default indexRange 1-1
    Range<Integer> getIndexRange(String dataDef, String itemName);

    Optional<List<String>> getBreakAfter(String dataDef, String itemName);

    Optional<List<String>> getPrefix(String dataDef, String itemName);

    Optional<List<Filter>> getFilter(String dataDef, String itemName);

    Optional<String> getLinkGroup(String dataDef, String itemName);
}
