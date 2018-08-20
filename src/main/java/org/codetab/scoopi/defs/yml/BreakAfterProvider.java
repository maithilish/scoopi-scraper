package org.codetab.scoopi.defs.yml;

import java.util.List;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IBreakAfterProvider;
import org.codetab.scoopi.defs.yml.cache.BreakAfterCache;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.Axis;

public class BreakAfterProvider implements IBreakAfterProvider {

    @Inject
    private BreakAfterCache breakAfterCache;

    @Override
    public List<String> getBreakAfters(final String dataDef, final Axis axis,
            final String memberName) throws DataDefNotFoundException {
        return breakAfterCache.getBreakAfters(dataDef, axis, memberName);
    }
}
