package org.codetab.scoopi.step.parse;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IItemDef;

public class BreakAfter {

    @Inject
    private IItemDef itemDef;

    public boolean check(final Optional<List<String>> breakAfters,
            final String value) {
        return breakAfters.get().contains(value);
    }

    public Optional<List<String>> getBreakAfters(final String dataDef,
            final String itemName) {
        return itemDef.getBreakAfter(dataDef, itemName);
    }
}
