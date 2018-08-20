package org.codetab.scoopi.defs.yml.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.defs.IDataDefProvider;
import org.codetab.scoopi.defs.yml.helper.BreakAfterHelper;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.DataDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BreakAfterCache {

    static final Logger LOGGER = LoggerFactory.getLogger(BreakAfterCache.class);

    @Inject
    private IDataDefProvider dataDefProvider;
    @Inject
    private BreakAfterHelper breakAfterHelper;

    private Map<String, List<String>> breakAfterCache = new HashMap<>();

    public List<String> getBreakAfters(final String dataDef, final Axis axis,
            final String memberName) throws DataDefNotFoundException {
        String key = String.join("-", dataDef, axis.getName().toString(),
                memberName);
        if (!breakAfterCache.containsKey(key)) {
            DataDef dDef = dataDefProvider.getDataDef(dataDef);
            List<String> breakAfters =
                    breakAfterHelper.getBreakAfters(dDef, axis, memberName);
            breakAfterCache.put(key, breakAfters);
        }
        return breakAfterCache.get(key);
    }
}
