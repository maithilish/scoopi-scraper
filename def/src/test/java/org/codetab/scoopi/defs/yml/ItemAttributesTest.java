package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codetab.scoopi.util.Util.dashit;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.commons.lang3.Range;
import org.codetab.scoopi.model.Filter;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Query;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.Lists;

public class ItemAttributesTest {

    @InjectMocks
    private ItemAttributes itemAttributes;

    @Mock
    private Jacksons jacksons;
    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private IndexRangeFactory indexRangeFactory;

    private ObjectFactory of;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        of = new ObjectFactory();
    }

    @Test
    public void testGetItemQueryMap() {
        Map<String, JsonNode> itemMap = getItemMap();
        JsonNode jItem = itemMap.get("foo");
        JsonNode defs = new TextNode("defs");

        String selector = "selector";
        String nameSelector = "nameSelector";
        String script = "script";
        Query query = of.createQuery();

        when(jItem.path("selector").asText()).thenReturn(selector);
        when(jItem.path("nameSelector").asText()).thenReturn(nameSelector);
        when(jItem.path("script").asText()).thenReturn(script);
        when(objectFactory.createQuery()).thenReturn(query);

        Map<String, Optional<Query>> actual =
                itemAttributes.getItemQueryMap(defs, itemMap);

        assertThat(actual).hasSize(1);
        assertThat(actual.get("foo")).isPresent();

        Query actualQuery = actual.get("foo").get();

        assertThat(actualQuery.getQuery("selector")).isEqualTo(selector);
        assertThat(actualQuery.getQuery("nameSelector"))
                .isEqualTo(nameSelector);
        assertThat(actualQuery.getQuery("script")).isEqualTo(script);
    }

    @Test
    public void testGetItemQueryMapBlanks() {
        Map<String, JsonNode> itemMap = getItemMap();
        JsonNode jItem = itemMap.get("foo");
        JsonNode defs = new TextNode("defs");

        Query query = of.createQuery();

        when(jItem.path("selector").asText()).thenReturn("")
                .thenReturn("selector");
        when(jItem.path("nameSelector").asText()).thenReturn("");
        when(jItem.path("script").asText()).thenReturn("");
        when(objectFactory.createQuery()).thenReturn(query);

        when(objectFactory.createQuery()).thenReturn(query);

        // query is null
        Map<String, Optional<Query>> actual =
                itemAttributes.getItemQueryMap(defs, itemMap);

        assertThat(actual).hasSize(1);
        assertThat(actual.get("foo")).isNotPresent();

        // query with one key, value
        actual = itemAttributes.getItemQueryMap(defs, itemMap);

        assertThat(actual).hasSize(1);
        assertThat(actual.get("foo")).isPresent();

        Query actualQuery = actual.get("foo").get();

        assertThat(actualQuery.getQuery("selector")).isEqualTo("selector");
        assertThrows(NoSuchElementException.class,
                () -> actualQuery.getQuery("nameSelector"));
        assertThrows(NoSuchElementException.class,
                () -> actualQuery.getQuery("script"));
    }

    @Test
    public void testGetIndexRangeMap() {
        Map<String, JsonNode> itemMap = getItemMap();
        JsonNode jItem = itemMap.get("foo");
        JsonNode defs = new TextNode("defs");

        String indexRangeStr = "5-10";
        Range<Integer> indexRange = Range.between(5, 10);

        when(jItem.path("indexRange").asText()).thenReturn(indexRangeStr);
        when(indexRangeFactory.createRange(indexRangeStr))
                .thenReturn(indexRange);

        Map<String, Range<Integer>> actual =
                itemAttributes.getIndexRangeMap(defs, itemMap);

        assertThat(actual).hasSize(1);

        assertThat(actual.get("foo")).isEqualTo(indexRange);
    }

    @Test
    public void testGetIndexRangeMapForIndex() {
        Map<String, JsonNode> itemMap = getItemMap();
        JsonNode jItem = itemMap.get("foo");
        JsonNode defs = new TextNode("defs");

        String indexRangeStr = "";
        String indexStr = "5";
        Range<Integer> indexRangeBreakNotDefined =
                Range.between(5, Integer.MAX_VALUE);
        Range<Integer> indexRangeBreakDefined = Range.between(5, 5);
        Range<Integer> indexRangeDefault = Range.between(1, 1);

        when(jItem.path("indexRange").asText()).thenReturn(indexRangeStr);
        when(jItem.path("index").asText()).thenReturn(indexStr, indexStr)
                .thenReturn("");
        when(jItem.path("breakAfter")).thenReturn(new TextNode("bar"))
                .thenReturn(MissingNode.getInstance());
        when(indexRangeFactory.createRange(dashit(indexStr, indexStr)))
                .thenReturn(indexRangeBreakNotDefined);
        when(indexRangeFactory.createRange(dashit(indexStr, "")))
                .thenReturn(indexRangeBreakDefined);

        // breakAfter defined
        Map<String, Range<Integer>> actual =
                itemAttributes.getIndexRangeMap(defs, itemMap);

        assertThat(actual).hasSize(1);
        assertThat(actual.get("foo")).isEqualTo(indexRangeBreakDefined);

        // breakAfter not defined
        actual = itemAttributes.getIndexRangeMap(defs, itemMap);

        assertThat(actual).hasSize(1);
        assertThat(actual.get("foo")).isEqualTo(indexRangeBreakNotDefined);

        // index blank
        actual = itemAttributes.getIndexRangeMap(defs, itemMap);

        assertThat(actual).hasSize(1);
        assertThat(actual.get("foo")).isEqualTo(indexRangeDefault);
    }

    @Test
    public void testGetBreakAfterMap() {
        Map<String, JsonNode> itemMap = getItemMap();
        JsonNode jItem = itemMap.get("foo");
        JsonNode defs = new TextNode("defs");

        List<String> breakAfters = Lists.newArrayList("foo", "bar");

        when(jacksons.getArrayAsStrings(jItem, "breakAfter"))
                .thenReturn(breakAfters);

        Map<String, Optional<List<String>>> actual =
                itemAttributes.getBreakAfterMap(defs, itemMap);

        assertThat(actual).hasSize(1);
        assertThat(actual.get("foo")).isPresent();
        assertThat(actual.get("foo").get())
                .containsExactlyElementsOf(breakAfters);
    }

    @Test
    public void testGetPrefixMap() {
        Map<String, JsonNode> itemMap = getItemMap();
        JsonNode jItem = itemMap.get("foo");
        JsonNode defs = new TextNode("defs");

        List<String> prefixes = Lists.newArrayList("foo", "bar");

        when(jacksons.getArrayAsStrings(jItem, "prefix")).thenReturn(prefixes);

        Map<String, Optional<List<String>>> actual =
                itemAttributes.getPrefixMap(defs, itemMap);

        assertThat(actual).hasSize(1);
        assertThat(actual.get("foo")).isPresent();
        assertThat(actual.get("foo").get()).containsExactlyElementsOf(prefixes);
    }

    @Test
    public void testGetFilterMap() {
        Map<String, JsonNode> itemMap = getItemMap();
        JsonNode jItem = itemMap.get("foo");
        JsonNode defs = new TextNode("defs");

        JsonNode jFilter = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        List<JsonNode> jFilters = new ArrayList<>();
        jFilters.add(jFilter);

        String type = "foo";
        String pattern = "bar";
        Filter filter = of.createFilter(type, pattern);

        when(jItem.at("/filters").findValues("filter")).thenReturn(jFilters)
                .thenReturn(new ArrayList<>());
        when(jFilter.path("type").asText()).thenReturn(type);
        when(jFilter.path("pattern").asText()).thenReturn(pattern);
        when(objectFactory.createFilter(type, pattern)).thenReturn(filter);

        Map<String, Optional<List<Filter>>> actual =
                itemAttributes.getFilterMap(defs, itemMap);

        assertThat(actual).hasSize(1);
        assertThat(actual.get("foo")).isPresent();
        assertThat(actual.get("foo").get()).containsExactly(filter);

        // no filters
        actual = itemAttributes.getFilterMap(defs, itemMap);

        assertThat(actual).hasSize(1);
        assertThat(actual.get("foo")).isNotPresent();
    }

    @Test
    public void testGetLinkGroupMap() {
        Map<String, JsonNode> itemMap = getItemMap();
        JsonNode jItem = itemMap.get("foo");
        JsonNode defs = new TextNode("defs");

        String linkGroup = "foo";

        when(jItem.path("linkGroup").asText()).thenReturn(linkGroup, "");

        Map<String, Optional<String>> actual =
                itemAttributes.getLinkGroupMap(defs, itemMap);

        assertThat(actual).hasSize(1);
        assertThat(actual.get("foo")).isPresent();
        assertThat(actual.get("foo").get()).isEqualTo(linkGroup);

        // linkGroup is blank
        actual = itemAttributes.getLinkGroupMap(defs, itemMap);

        assertThat(actual).hasSize(1);
        assertThat(actual.get("foo")).isNotPresent();
    }

    @Test
    public void testGetLinkBreakOnMap() {
        Map<String, JsonNode> itemMap = getItemMap();
        JsonNode jItem = itemMap.get("foo");
        JsonNode defs = new TextNode("defs");

        List<String> linkBreakOn = Lists.newArrayList("foo", "bar");

        when(jacksons.getArrayAsStrings(jItem, "linkBreakOn"))
                .thenReturn(linkBreakOn);

        Map<String, Optional<List<String>>> actual =
                itemAttributes.getLinkBreakOnMap(defs, itemMap);

        assertThat(actual).hasSize(1);
        assertThat(actual.get("foo")).isPresent();
        assertThat(actual.get("foo").get())
                .containsExactlyElementsOf(linkBreakOn);
    }

    private Map<String, JsonNode> getItemMap() {
        Map<String, JsonNode> itemMap = new HashMap<>();
        itemMap.put("foo", Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS));
        return itemMap;
    }
}
