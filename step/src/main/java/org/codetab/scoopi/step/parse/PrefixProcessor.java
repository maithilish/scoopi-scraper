package org.codetab.scoopi.step.parse;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IItemDef;

public class PrefixProcessor {

    @Inject
    private IItemDef itemDef;

    public Optional<List<String>> getPrefixes(final String dataDef,
            final String itemName) {
        return itemDef.getPrefix(dataDef, itemName);
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
