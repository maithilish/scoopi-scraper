package org.codetab.scoopi.defs.yml;

import static java.util.Objects.nonNull;
import static org.codetab.scoopi.util.Util.dashit;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.Range;
import org.codetab.scoopi.defs.IItemDef;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.Filter;
import org.codetab.scoopi.model.Query;

@Singleton
public class ItemDef implements IItemDef {

    @Inject
    private ItemDefData data;

    @Override
    public Query getQuery(final String dataDef) {
        return data.getQueryMap().get(dataDef).copy();
    }

    @Override
    public Data getDataTemplate(final String dataDef) {
        return data.getDataTemplateMap().get(dataDef).copy();
    }

    @Override
    public Optional<Query> getItemQuery(final String dataDef,
            final String itemName) {
        String key = dashit(dataDef, itemName);
        Query query = data.getItemAttributeMap().get(key).getQuery();
        if (nonNull(query)) {
            return Optional.ofNullable(query.copy());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Range<Integer> getIndexRange(final String dataDef,
            final String itemName) {
        String key = dashit(dataDef, itemName);
        // range is immutable so no copy
        // default indexRange 1-1 so not optional
        return data.getItemAttributeMap().get(key).getIndexRange();
    }

    @Override
    public Optional<List<String>> getBreakAfter(final String dataDef,
            final String itemName) {
        String key = dashit(dataDef, itemName);
        List<String> list = data.getItemAttributeMap().get(key).getBreakAfter();
        if (nonNull(list)) {
            list = Collections.unmodifiableList(list);
        }
        return Optional.ofNullable(list);
    }

    @Override
    public Optional<List<Filter>> getFilter(final String dataDef,
            final String itemName) {
        String key = dashit(dataDef, itemName);
        List<Filter> list = data.getItemAttributeMap().get(key).getFilter();
        if (nonNull(list)) {
            list = Collections.unmodifiableList(list);
        }
        return Optional.ofNullable(list);
    }

    @Override
    public Optional<List<String>> getPrefix(final String dataDef,
            final String itemName) {
        String key = dashit(dataDef, itemName);
        List<String> list = data.getItemAttributeMap().get(key).getPrefix();
        if (nonNull(list)) {
            list = Collections.unmodifiableList(list);
        }
        return Optional.ofNullable(list);
    }

    @Override
    public Optional<String> getLinkGroup(final String dataDef,
            final String itemName) {
        String key = dashit(dataDef, itemName);
        return Optional
                .ofNullable(data.getItemAttributeMap().get(key).getLinkGroup());
    }

    @Override
    public Optional<List<String>> getLinkBreakOn(final String dataDef,
            final String itemName) {
        String key = dashit(dataDef, itemName);
        List<String> list =
                data.getItemAttributeMap().get(key).getLinkBreakOn();
        if (nonNull(list)) {
            list = Collections.unmodifiableList(list);
        }
        return Optional.ofNullable(list);
    }

}
