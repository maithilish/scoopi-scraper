package org.codetab.scoopi.model.helper;

import static org.codetab.scoopi.util.Util.spaceit;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
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
        String axisValue = axis.getValue();
        String axisMatch = axis.getMatch();
        for (Filter filter : filters) {
            String value = axisValue;
            if (filter.getType().equals("match")) {
                value = axisMatch;
            }
            String pattern = filter.getPattern();
            if (pattern.equals(" ") && StringUtils.isBlank(value)) {
                return true;
            }
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
