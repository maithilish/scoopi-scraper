package org.codetab.scoopi.model.helper;

import static java.util.Objects.isNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IAxisDefs;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.DataComponent;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.DataIterator;
import org.codetab.scoopi.model.Filter;
import org.codetab.scoopi.model.Member;

public class FilterHelper {

    @Inject
    private IAxisDefs axisDefs;

    public Map<AxisName, List<Filter>> getFilterMap(final DataDef dataDef) {
        return axisDefs.getFilterMap(dataDef);
    }

    public List<Member> getFilterMembers(final List<Member> members,
            final Map<AxisName, List<Filter>> filterMap) {
        List<Member> filterMembers = new ArrayList<>();
        for (Member member : members) {
            Stream<Axis> axisToFilter = member.getAxes().stream()
                    .filter(axis -> filterMap.containsKey(axis.getName()));
            axisToFilter.forEach(axis -> {
                List<Filter> filters = filterMap.get(axis.getName());
                if (requireFilter(axis, filters)) {
                    if (!filterMembers.contains(member)) {
                        filterMembers.add(member);
                    }
                }
            });
        }
        return filterMembers;
    }

    public void filter(final Data data, final List<Member> filterMembers) {
        DataIterator it = data.iterator();
        while (it.hasNext()) {
            DataComponent dc = it.next();
            if (dc instanceof Member) {
                if (filterMembers.contains(dc)) {
                    it.remove();
                }
            }
        }
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
                String message = String.join(" ", "unable to filter", pattern);
                throw new StepRunException(message, e);
            }
        }
        return false;
    }

}
