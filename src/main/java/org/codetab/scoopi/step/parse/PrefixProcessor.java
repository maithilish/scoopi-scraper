package org.codetab.scoopi.step.parse;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IAxisDefs;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;

public class PrefixProcessor {

    @Inject
    private IAxisDefs axisDefs;

    public Optional<List<String>> getPrefixes(final DataDef dataDef,
            final AxisName axisName) {
        return axisDefs.getPrefixes(dataDef, axisName);
    }

    public String prefixValue(final String value, final List<String> prefixes) {
        notNull(value, "value must not be null");
        notNull(prefixes, "prefixes must not be null");

        // prefixes is unmodifiable, create new list
        List<String> list = new ArrayList<>(prefixes);
        Collections.reverse(list);
        list.add(value);

        return String.join("", list);
    }
}
