package org.codetab.scoopi.model.helper;

import static java.util.Objects.isNull;
import static org.codetab.scoopi.util.Util.spaceit;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IItemDef;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.Filter;
import org.codetab.scoopi.model.Item;

public class FilterHelper {

    @Inject
    private IItemDef itemDef;

    public boolean filter(final Item item, final String dataDef) {
        boolean requireFilter = false;
        for (Axis axis : item.getAxes()) {
            String itemName = axis.getItemName();
            Optional<List<Filter>> filters =
                    itemDef.getFilter(dataDef, itemName);
            if (filters.isPresent()) {
                if (requireFilter(axis, filters.get())) {
                    requireFilter = true;
                }
            }
        }
        return requireFilter;
    }

    private boolean requireFilter(final Axis axis, final List<Filter> filters) {
        for (Filter filter : filters) {
            String value = null;
            if (filter.getType().equals("value")) {
                value = axis.getValue();
            }
            if (filter.getType().equals("match")) {
                value = axis.getMatch();
            }
            if (isNull(value)) {
                return false;
            }
            String pattern = filter.getPattern();
            if (value.equals(pattern)) {
                return true;
            }
            try {
                if (Pattern.matches(pattern, value)) {
                    return true;
                }
            } catch (PatternSyntaxException e) {
                String message = spaceit("unable to filter", pattern);
                throw new StepRunException(message, e);
            }
        }
        return false;
    }

}
