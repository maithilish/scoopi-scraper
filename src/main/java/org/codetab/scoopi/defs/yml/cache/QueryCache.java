package org.codetab.scoopi.defs.yml.cache;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.defs.IDataDefProvider;
import org.codetab.scoopi.defs.yml.helper.QueryHelper;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;

@Singleton
public class QueryCache {

    @Inject
    private IDataDefProvider dataDefProvider;
    @Inject
    private QueryHelper queryHelper;

    private Map<String, String> queryCache = new HashMap<>();

    public String getQuery(final String dataDef, final AxisName axisName,
            final String queryType) throws DataDefNotFoundException {
        String key = String.join("-", dataDef, axisName.toString(), queryType);
        if (!queryCache.containsKey(key)) {
            DataDef dDef = dataDefProvider.getDataDef(dataDef);
            String query = queryHelper.getQuery(dDef, axisName, queryType);
            queryCache.put(key, query);
        }
        return queryCache.get(key);
    }
}
