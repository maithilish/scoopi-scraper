package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.Range;
import org.codetab.scoopi.defs.IItemDef;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.Filter;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Query;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

public class ItemDefsIT {

    private static IItemDef itemDef;
    private static DInjector di;
    private static JsonNode dataDefs;

    private ObjectFactory factory;

    @BeforeClass
    public static void setUpBeforeClass()
            throws IOException, DefNotFoundException {
        JsonNode node = YamlLoader.load("/testdefs/yml/item-defs-it.yml");
        dataDefs = node.at("/dataDefs");
        di = new DInjector();
    }

    @Before
    public void setUp() throws Exception {
        itemDef = di.instance(ItemDef.class);
        factory = di.instance(ObjectFactory.class);
        itemDef.init(dataDefs);
    }

    @Test
    public void testGetQuery() throws IOException {
        Query expected = factory.createQuery();
        expected.setQuery("block", "def1 block query");
        Query actual = itemDef.getQuery("def1");
        assertThat(actual).isEqualTo(expected);

        expected = factory.createQuery();
        expected.setQuery("block", "def2 block query");
        expected.setQuery("selector", "def2 selector query");
        expected.setQuery("script", "def2 script query");
        actual = itemDef.getQuery("def2");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetDataTemplate() {
        Data expected = factory.createData("def1");
        Item item = factory.createItem();
        expected.addItem(item);
        item.addAxis(factory.createAxis("item1", null, null, 11, null));
        item.addAxis(factory.createAxis("dim1", null, null, null, null));
        item.addAxis(factory.createAxis("fact", null, null, 1, 1));
        Data actual = itemDef.getDataTemplate("def1");
        assertThat(actual).isEqualTo(expected);

        expected = factory.createData("def2");
        item = factory.createItem();
        expected.addItem(item);
        item.addAxis(factory.createAxis("item1", null, null, null, null));
        item.addAxis(factory.createAxis("dim1", "dim1 value", "dim1 match", 22,
                122));
        item.addAxis(factory.createAxis("fact", null, null, 1, 1));
        item = factory.createItem();
        expected.addItem(item);
        item.addAxis(factory.createAxis("item1", null, null, null, null));
        item.addAxis(factory.createAxis("dim2", null, null, 23, null));
        item.addAxis(factory.createAxis("fact", null, null, 1, 1));
        actual = itemDef.getDataTemplate("def2");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetItemQuery() {
        Query expected = factory.createQuery();
        expected.setQuery("selector", "def1-item1 selector");
        expected.setQuery("nameSelector", "def1-item1 name selector");
        Optional<Query> actual = itemDef.getItemQuery("def1", "item1");
        assertThat(actual.get()).isEqualTo(expected);

        expected = factory.createQuery();
        expected.setQuery("selector", "def1-dim1 selector");
        actual = itemDef.getItemQuery("def1", "dim1");
        assertThat(actual.get()).isEqualTo(expected);

        expected = factory.createQuery();
        expected.setQuery("selector", "def2-item1 selector");
        expected.setQuery("nameSelector", "def2-item1 name selector");
        actual = itemDef.getItemQuery("def2", "item1");
        assertThat(actual.get()).isEqualTo(expected);

        expected = factory.createQuery();
        expected.setQuery("selector", "def2-dim1 selector");
        actual = itemDef.getItemQuery("def2", "dim1");
        assertThat(actual.get()).isEqualTo(expected);

        expected = factory.createQuery();
        expected.setQuery("selector", "def2-dim2 selector");
        expected.setQuery("nameSelector", "def2-dim2 name selector");
        expected.setQuery("script", "def2-dim2 script");
        actual = itemDef.getItemQuery("def2", "dim2");
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void testGetIndexRange() {
        // breakAfter index 11 = 11-max
        Range<Integer> actual = itemDef.getIndexRange("def1", "item1");
        assertThat(actual).isEqualTo(Range.between(11, Integer.MAX_VALUE));

        // indexRange 1-12
        actual = itemDef.getIndexRange("def1", "dim1");
        assertThat(actual).isEqualTo(Range.between(1, 12));

        // indexRange 2-21
        actual = itemDef.getIndexRange("def2", "item1");
        assertThat(actual).isEqualTo(Range.between(2, 21));

        // breakAfter index 22 = 22-Max
        actual = itemDef.getIndexRange("def2", "dim1");
        assertThat(actual).isEqualTo(Range.between(22, Integer.MAX_VALUE));

        // index 23 = 23-23
        actual = itemDef.getIndexRange("def2", "dim2");
        assertThat(actual).isEqualTo(Range.between(23, 23));
    }

    @Test
    public void testGetBreakAfter() {
        List<String> expected = Lists.newArrayList("def1-item1 breakafter 1",
                "def1-item1 breakafter 2");
        Optional<List<String>> actual = itemDef.getBreakAfter("def1", "item1");
        assertThat(actual.get()).isEqualTo(expected);

        actual = itemDef.getBreakAfter("def1", "dim1");
        assertThat(actual).isNotPresent();

        actual = itemDef.getBreakAfter("def2", "item1");
        assertThat(actual).isNotPresent();

        expected = Lists.newArrayList("def2-dim1 breakafter 1");
        actual = itemDef.getBreakAfter("def2", "dim1");
        assertThat(actual.get()).isEqualTo(expected);

        actual = itemDef.getBreakAfter("def2", "dim2");
        assertThat(actual).isNotPresent();
    }

    @Test
    public void testGetFilter() {
        Filter filter1 = factory.createFilter("value", "def1-item1 filter 1");
        Filter filter2 = factory.createFilter("match", "def1-item1 filter 2");
        List<Filter> expected = Lists.newArrayList(filter1, filter2);
        Optional<List<Filter>> actual = itemDef.getFilter("def1", "item1");
        assertThat(actual.get()).isEqualTo(expected);

        actual = itemDef.getFilter("def1", "dim1");
        assertThat(actual).isNotPresent();

        actual = itemDef.getFilter("def2", "item1");
        assertThat(actual).isNotPresent();

        actual = itemDef.getFilter("def2", "dim1");
        assertThat(actual).isNotPresent();

        filter1 = factory.createFilter("value", "def2-dim2 filter 1");
        expected = Lists.newArrayList(filter1);
        actual = itemDef.getFilter("def2", "dim2");
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void testGetPrefix() {
        List<String> expected = Lists.newArrayList("def1-item1 prefix 1",
                "def1-item1 prefix 2");
        Optional<List<String>> actual = itemDef.getPrefix("def1", "item1");
        assertThat(actual.get()).isEqualTo(expected);

        actual = itemDef.getPrefix("def1", "dim1");
        assertThat(actual).isNotPresent();

        actual = itemDef.getPrefix("def2", "item1");
        assertThat(actual).isNotPresent();

        actual = itemDef.getPrefix("def2", "dim1");
        assertThat(actual).isNotPresent();

        expected = Lists.newArrayList("def2-dim2 prefix 1");
        actual = itemDef.getPrefix("def2", "dim2");
        assertThat(actual.get()).isEqualTo(expected);
    }

}
