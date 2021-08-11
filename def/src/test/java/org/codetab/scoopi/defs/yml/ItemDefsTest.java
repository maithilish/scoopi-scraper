package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.commons.lang3.Range;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.Filter;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Query;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

public class ItemDefsTest {

    @InjectMocks
    private ItemDefs itemDefs;

    @Mock
    private Jacksons jacksons;
    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private ItemAttributes itemAttributes;
    @Mock
    private ItemMapFactory itemMapFactory;

    private ObjectFactory of;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        of = new ObjectFactory();
    }

    @Test
    public void testGetQueryMap() {

        String dataDefName = "foo";
        String block = "tblock";
        String blockPath = "tblockpath";
        String selector = "tselector";
        String selectorPath = "tselectorpath";
        String script = "tscript";
        String scriptPath = "tscriptpath";
        Query query = of.createQuery();
        Iterator<String> dataDefNames =
                Lists.newArrayList(dataDefName).iterator();

        JsonNode defs = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);

        when(defs.fieldNames()).thenReturn(dataDefNames);
        when(objectFactory.createQuery()).thenReturn(query);
        when(jacksons.path(dataDefName, "query", "block"))
                .thenReturn(blockPath);
        when(defs.at(blockPath).asText()).thenReturn(block);
        when(jacksons.path(dataDefName, "query", "selector"))
                .thenReturn(selectorPath);
        when(defs.at(selectorPath).asText()).thenReturn(selector);
        when(jacksons.path(dataDefName, "query", "script"))
                .thenReturn(scriptPath);
        when(defs.at(scriptPath).asText()).thenReturn(script);

        Map<String, Query> actual = itemDefs.getQueryMap(defs);

        assertThat(actual).hasSize(1);

        Query actualQuery = actual.get("foo");

        assertThat(actualQuery.getQuery("block")).isEqualTo(block);
        assertThat(actualQuery.getQuery("selector")).isEqualTo(selector);
        assertThat(actualQuery.getQuery("script")).isEqualTo(script);
    }

    @Test
    public void testGetQueryBlanks() {

        String dataDefName = "foo";
        String blockPath = "tblockpath";
        String selectorPath = "tselectorpath";
        String scriptPath = "tscriptpath";
        Query query = of.createQuery();
        Iterator<String> dataDefNames =
                Lists.newArrayList(dataDefName).iterator();

        JsonNode defs = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);

        when(defs.fieldNames()).thenReturn(dataDefNames);
        when(objectFactory.createQuery()).thenReturn(query);
        when(jacksons.path(dataDefName, "query", "block"))
                .thenReturn(blockPath);
        when(defs.at(blockPath).asText()).thenReturn("");
        when(jacksons.path(dataDefName, "query", "selector"))
                .thenReturn(selectorPath);
        when(defs.at(selectorPath).asText()).thenReturn("");
        when(jacksons.path(dataDefName, "query", "script"))
                .thenReturn(scriptPath);
        when(defs.at(scriptPath).asText()).thenReturn("");

        Map<String, Query> actual = itemDefs.getQueryMap(defs);

        assertThat(actual).hasSize(1);

        Query actualQuery = actual.get("foo");

        assertThrows(NoSuchElementException.class,
                () -> actualQuery.getQuery("block"));
        assertThrows(NoSuchElementException.class,
                () -> actualQuery.getQuery("selector"));
        assertThrows(NoSuchElementException.class,
                () -> actualQuery.getQuery("script"));
    }

    @Test
    public void testGetItemAxisMap() {
        JsonNode defs = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        Map<String, List<Axis>> itemMap = new HashMap<>();

        when(itemMapFactory.getItemMap(defs, "items", "item"))
                .thenReturn(itemMap);

        Map<String, List<Axis>> actual = itemDefs.getItemAxisMap(defs);

        assertThat(actual).isEqualTo(itemMap);
    }

    @Test
    public void testGetDimAxisMap() {
        JsonNode defs = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        Map<String, List<Axis>> itemMap = new HashMap<>();

        when(itemMapFactory.getItemMap(defs, "dims", "dim"))
                .thenReturn(itemMap);

        Map<String, List<Axis>> actual = itemDefs.getDimAxisMap(defs);

        assertThat(actual).isEqualTo(itemMap);
    }

    @Test
    public void testGetFactAxisMap() {
        JsonNode defs = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        Map<String, List<Axis>> itemMap = new HashMap<>();

        when(itemMapFactory.getItemMap(defs, "facts", "fact"))
                .thenReturn(itemMap);

        Map<String, List<Axis>> actual = itemDefs.getFactAxisMap(defs);

        assertThat(actual).isEqualTo(itemMap);
    }

    @Test
    public void testGenerateDataTemplates() {
        String dataDef = "foo";

        Data data = of.createData(dataDef);
        Item fooItem = of.createItem();
        Item barItem = of.createItem();

        Axis fooItemAxis = of.createAxis("foo", "foo");
        Axis barItemAxis = of.createAxis("bar", "bar");
        Axis dateAxis = of.createAxis("date", "date");
        Axis factAxis = of.createAxis("fact", "fact");

        List<Axis> itemAxisList = Lists.newArrayList(fooItemAxis, barItemAxis);
        List<Axis> dimAxisList = Lists.newArrayList(dateAxis);
        List<Axis> factAxisList = Lists.newArrayList(factAxis);

        Map<String, List<Axis>> itemAxisMap = new HashMap<>();
        itemAxisMap.put(dataDef, itemAxisList);
        Map<String, List<Axis>> dimAxisMap = new HashMap<>();
        dimAxisMap.put(dataDef, dimAxisList);
        Map<String, List<Axis>> factAxisMap = new HashMap<>();
        factAxisMap.put(dataDef, factAxisList);

        when(objectFactory.createData(dataDef)).thenReturn(data);
        when(objectFactory.createItem()).thenReturn(fooItem)
                .thenReturn(barItem);

        Map<String, Data> actual = itemDefs.generateDataTemplates(itemAxisMap,
                dimAxisMap, factAxisMap);
        assertThat(actual).hasSize(1);

        // check Cartesian product
        List<Item> items = actual.get(dataDef).getItems();
        assertThat(items).hasSize(2);
        // foo item
        assertThat(items.get(0)).isSameAs(fooItem);
        assertThat(items.get(0).getAxes()).containsExactly(fooItemAxis,
                dateAxis, factAxis);
        // bar item
        assertThat(items.get(1)).isSameAs(barItem);
        assertThat(items.get(1).getAxes()).containsExactly(barItemAxis,
                dateAxis, factAxis);
    }

    @Test
    public void testGetItemNodeMap() {
        String dataDefName = "foo";
        String itemName = "bar";
        JsonNode defs = Mockito.mock(JsonNode.class);
        JsonNode jDataDef = Mockito.mock(JsonNode.class);
        Map<String, JsonNode> map = new HashMap<>();
        map.put(dataDefName, jDataDef);
        Iterator<Entry<String, JsonNode>> entries = map.entrySet().iterator();
        JsonNode jItem = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        List<JsonNode> jItems = new ArrayList<>();
        jItems.add(jItem);

        when(defs.fields()).thenReturn(entries);
        when(jDataDef.findValues("item")).thenReturn(jItems);
        when(jItem.path("name").asText()).thenReturn(itemName);

        Map<String, JsonNode> actual = itemDefs.getItemNodeMap(defs);

        assertThat(actual).hasSize(1);
        assertThat(actual.get("foo-bar")).isSameAs(jItem);
    }

    @Test
    public void testGetItemAttributeMap() {
        JsonNode defs = Mockito.mock(JsonNode.class);
        JsonNode itemNode = Mockito.mock(JsonNode.class);
        String key = "foo";

        Map<String, JsonNode> itemNodeMap = new HashMap<>();
        itemNodeMap.put(key, itemNode);

        Optional<Query> query = Optional.of(of.createQuery());
        Map<String, Optional<Query>> itemQueryMap = new HashMap<>();
        itemQueryMap.put(key, query);

        Range<Integer> indexRange = Range.between(5, 10);
        Map<String, Range<Integer>> indexRangeMap = new HashMap<>();
        indexRangeMap.put(key, indexRange);

        Optional<List<String>> breakAfters = Optional.of(new ArrayList<>());
        Map<String, Optional<List<String>>> breakAfterMap = new HashMap<>();
        breakAfterMap.put(key, breakAfters);

        Optional<List<Filter>> filters = Optional.of(new ArrayList<>());
        Map<String, Optional<List<Filter>>> filterMap = new HashMap<>();
        filterMap.put(key, filters);

        Optional<List<String>> prefixes = Optional.of(new ArrayList<>());
        Map<String, Optional<List<String>>> prefixMap = new HashMap<>();
        prefixMap.put(key, prefixes);

        Optional<String> linkGroup = Optional.of("tlinkgroup");
        Map<String, Optional<String>> linkGroupMap = new HashMap<>();
        linkGroupMap.put(key, linkGroup);

        Optional<List<String>> linkBreakOns = Optional.of(new ArrayList<>());
        Map<String, Optional<List<String>>> linkBreakOnMap = new HashMap<>();
        linkBreakOnMap.put(key, linkBreakOns);

        when(itemAttributes.getItemQueryMap(defs, itemNodeMap))
                .thenReturn(itemQueryMap);
        when(itemAttributes.getIndexRangeMap(defs, itemNodeMap))
                .thenReturn(indexRangeMap);
        when(itemAttributes.getBreakAfterMap(defs, itemNodeMap))
                .thenReturn(breakAfterMap);
        when(itemAttributes.getFilterMap(defs, itemNodeMap))
                .thenReturn(filterMap);
        when(itemAttributes.getPrefixMap(defs, itemNodeMap))
                .thenReturn(prefixMap);
        when(itemAttributes.getLinkGroupMap(defs, itemNodeMap))
                .thenReturn(linkGroupMap);
        when(itemAttributes.getLinkBreakOnMap(defs, itemNodeMap))
                .thenReturn(linkBreakOnMap);

        Map<String, ItemAttribute> actual =
                itemDefs.getItemAttributeMap(defs, itemNodeMap);

        assertThat(actual).hasSize(1);
        ItemAttribute aItemAttribute = actual.get(key);

        assertThat(aItemAttribute.getKey()).isEqualTo(key);
        assertThat(aItemAttribute.getQuery()).isEqualTo(query.get());
        assertThat(aItemAttribute.getIndexRange()).isEqualTo(indexRange);
        assertThat(aItemAttribute.getPrefix()).isEqualTo(prefixes.get());
        assertThat(aItemAttribute.getBreakAfter()).isEqualTo(breakAfters.get());
        assertThat(aItemAttribute.getFilter()).isEqualTo(filters.get());
        assertThat(aItemAttribute.getLinkGroup()).isEqualTo(linkGroup.get());
        assertThat(aItemAttribute.getLinkBreakOn())
                .isEqualTo(linkBreakOns.get());
    }

    @Test
    public void testTraceDataTemplates() {
        String dataDef = "foo";
        Data data = of.createData(dataDef);
        Map<String, Data> dataTemplates = new HashMap<>();
        dataTemplates.put(dataDef, data);

        itemDefs.traceDataTemplates(dataTemplates);
    }

}
