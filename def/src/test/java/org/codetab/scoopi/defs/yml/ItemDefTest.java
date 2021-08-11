package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codetab.scoopi.util.Util.dashit;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.Range;
import org.assertj.core.util.Lists;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.Filter;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Query;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ItemDefTest {

    @InjectMocks
    private ItemDef itemDef;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ItemDefData data;

    private ObjectFactory of;

    private String dataDef;

    private String itemName;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        of = new ObjectFactory();
        dataDef = "foo";
        itemName = "bar";
    }

    @Test
    public void testGetQuery() {
        Query query = of.createQuery();
        when(data.getQueryMap().get(dataDef).copy()).thenReturn(query);

        Query actual = itemDef.getQuery(dataDef);

        assertThat(actual).isSameAs(query);
    }

    @Test
    public void testGetDataTemplate() {
        Data dataCopy = of.createData(dataDef);
        when(data.getDataTemplateMap().get(dataDef).copy())
                .thenReturn(dataCopy);

        Data actual = itemDef.getDataTemplate(dataDef);

        assertThat(actual).isSameAs(dataCopy);
    }

    @Test
    public void testGetItemQuery() {
        String key = dashit(dataDef, itemName);
        Query query = of.createQuery();
        query.setQuery("k", "v");

        when(data.getItemAttributeMap().get(key).getQuery()).thenReturn(query)
                .thenReturn(null);

        Optional<Query> actual = itemDef.getItemQuery(dataDef, itemName);

        assertThat(actual.get()).isNotSameAs(query);
        assertThat(actual.get()).isEqualTo(query);

        actual = itemDef.getItemQuery(dataDef, itemName);

        assertThat(actual).isNotPresent();
    }

    @Test
    public void testGetIndexRange() {
        String key = dashit(dataDef, itemName);
        Range<Integer> indexRange = Range.between(10, 20);

        when(data.getItemAttributeMap().get(key).getIndexRange())
                .thenReturn(indexRange);

        Range<Integer> actual = itemDef.getIndexRange(dataDef, itemName);

        assertThat(actual).isEqualTo(indexRange);
    }

    @Test
    public void testGetBreakAfter() {
        String key = dashit(dataDef, itemName);

        List<String> breakAfters = Lists.newArrayList("foo", "bar");

        when(data.getItemAttributeMap().get(key).getBreakAfter())
                .thenReturn(breakAfters).thenReturn(null);

        List<String> actual = itemDef.getBreakAfter(dataDef, itemName).get();

        assertThat(actual).hasSize(2);
        assertThat(actual).isEqualTo(breakAfters);
        assertThat(actual).isNotSameAs(breakAfters);
        assertThat(actual.getClass().getSimpleName())
                .isEqualTo("UnmodifiableRandomAccessList");

        // null
        assertThat(itemDef.getBreakAfter(dataDef, itemName)).isNotPresent();
    }

    @Test
    public void testGetFilter() {
        String key = dashit(dataDef, itemName);
        Filter filter = of.createFilter("foo", "bar");
        List<Filter> filters = Lists.newArrayList(filter);

        when(data.getItemAttributeMap().get(key).getFilter())
                .thenReturn(filters).thenReturn(null);

        List<Filter> actual = itemDef.getFilter(dataDef, itemName).get();

        assertThat(actual).hasSize(1);
        assertThat(actual).isEqualTo(filters);
        assertThat(actual).isNotSameAs(filters);
        assertThat(actual.getClass().getSimpleName())
                .isEqualTo("UnmodifiableRandomAccessList");

        // null
        assertThat(itemDef.getFilter(dataDef, itemName)).isNotPresent();
    }

    @Test
    public void testGetPrefix() {
        String key = dashit(dataDef, itemName);

        List<String> prefixes = Lists.newArrayList("foo", "bar");

        when(data.getItemAttributeMap().get(key).getPrefix())
                .thenReturn(prefixes).thenReturn(null);

        List<String> actual = itemDef.getPrefix(dataDef, itemName).get();

        assertThat(actual).hasSize(2);
        assertThat(actual).isEqualTo(prefixes);
        assertThat(actual).isNotSameAs(prefixes);
        assertThat(actual.getClass().getSimpleName())
                .isEqualTo("UnmodifiableRandomAccessList");

        // null
        assertThat(itemDef.getPrefix(dataDef, itemName)).isNotPresent();
    }

    @Test
    public void testGetLinkGroup() {
        String key = dashit(dataDef, itemName);
        String linkGroup = "foo";

        when(data.getItemAttributeMap().get(key).getLinkGroup())
                .thenReturn(linkGroup).thenReturn(null);

        Optional<String> actual = itemDef.getLinkGroup(dataDef, itemName);

        assertThat(actual.get()).isEqualTo(linkGroup);

        actual = itemDef.getLinkGroup(dataDef, itemName);

        assertThat(actual).isNotPresent();
    }

    @Test
    public void testGetLinkBreakOn() {
        String key = dashit(dataDef, itemName);

        List<String> linkBreakOn = Lists.newArrayList("foo", "bar");

        when(data.getItemAttributeMap().get(key).getLinkBreakOn())
                .thenReturn(linkBreakOn).thenReturn(null);

        List<String> actual = itemDef.getLinkBreakOn(dataDef, itemName).get();

        assertThat(actual).hasSize(2);
        assertThat(actual).isEqualTo(linkBreakOn);
        assertThat(actual).isNotSameAs(linkBreakOn);
        assertThat(actual.getClass().getSimpleName())
                .isEqualTo("UnmodifiableRandomAccessList");

        // null
        assertThat(itemDef.getLinkBreakOn(dataDef, itemName)).isNotPresent();
    }

}
