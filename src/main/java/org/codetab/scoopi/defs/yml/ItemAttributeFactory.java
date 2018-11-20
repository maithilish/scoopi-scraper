package org.codetab.scoopi.defs.yml;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.Range;
import org.codetab.scoopi.model.Filter;
import org.codetab.scoopi.model.Query;

// TODO refactor as Builder
class ItemAttributeFactory {

    public ItemAttribute create(final String key, final Optional<Query> query,
            final Range<Integer> indexRange,
            final Optional<List<String>> breakAfter,
            final Optional<List<Filter>> filter,
            final Optional<List<String>> prefix,
            final Optional<String> linkGroup) {
        ItemAttribute itemAttribute = new ItemAttribute();
        itemAttribute.setKey(key);
        itemAttribute.setQuery(query);
        itemAttribute.setIndexRange(indexRange);
        itemAttribute.setBreakAfter(breakAfter);
        itemAttribute.setPrefix(prefix);
        itemAttribute.setFilter(filter);
        itemAttribute.setLinkGroup(linkGroup);
        return itemAttribute;
    }

}
