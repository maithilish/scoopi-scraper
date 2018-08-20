package org.codetab.scoopi.defs.yml;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IQueryProvider;
import org.codetab.scoopi.defs.yml.cache.QueryCache;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.AxisName;

public class QueryProvider implements IQueryProvider {

    @Inject
    private QueryCache queryCache;

    @Override
    public String getQuery(final String dataDef, final AxisName axisName,
            final String queryType) throws DataDefNotFoundException {
        return queryCache.getQuery(dataDef, axisName, queryType);
    }
}
