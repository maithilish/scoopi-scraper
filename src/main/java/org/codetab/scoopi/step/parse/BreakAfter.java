package org.codetab.scoopi.step.parse;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IItemDef;

public class BreakAfter {

    @Inject
    private IItemDef itemDef;

    public boolean check(final String dataDef, final String itemName,
            final String value) {
        Optional<List<String>> breakAfters =
                itemDef.getBreakAfter(dataDef, itemName);
        if (breakAfters.isPresent() && breakAfters.get().contains(value)) {
            return true;
        }
        return false;
    }

}
