package org.codetab.scoopi.model.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codetab.scoopi.defs.IAxisDefs;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.Filter;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.ObjectFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

public class FilterHelperTest {

    @Mock
    private IAxisDefs axisDefs;

    @InjectMocks
    private FilterHelper filterHelper;

    private static ObjectFactory factory;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass() {
        factory = new ObjectFactory();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetFilterMap() {
        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "defJson");
        Map<AxisName, List<Filter>> filterMap = new HashMap<>();

        given(axisDefs.getFilterMap(dataDef)).willReturn(filterMap);

        Map<AxisName, List<Filter>> actual = filterHelper.getFilterMap(dataDef);

        assertThat(actual).isSameAs(filterMap);
    }

    @Test
    public void testGetFilterItemsByValue() {
        Filter filter1 = factory.createFilter("value", "r1");
        Filter filter2 = factory.createFilter("value", "r3");
        List<Filter> filters = Lists.newArrayList(filter1, filter2);
        Map<AxisName, List<Filter>> filterMap = new HashMap<>();
        filterMap.put(AxisName.ROW, filters);

        List<Item> items = createTestItems();

        List<Item> actual = filterHelper.getFilterItems(items, filterMap);

        assertThat(actual.size()).isEqualTo(2);
        assertThat(actual.get(0)).isSameAs(items.get(0));
        assertThat(actual.get(1)).isSameAs(items.get(2));

        filter1 = factory.createFilter("value", "c1");
        filter2 = factory.createFilter("value", "c2");
        Filter filter3 = factory.createFilter("value", "c10");
        filters = Lists.newArrayList(filter1, filter2, filter3);

        filterMap.put(AxisName.COL, filters);

        actual = filterHelper.getFilterItems(items, filterMap);

        assertThat(actual.size()).isEqualTo(3);
        assertThat(actual.get(0)).isSameAs(items.get(0));
        assertThat(actual.get(1)).isSameAs(items.get(1));
        assertThat(actual.get(2)).isSameAs(items.get(2));
    }

    @Test
    public void testGetFilterItemsPattern() {
        Filter filter1 = factory.createFilter("value", "r[1-2]");
        List<Filter> filters = Lists.newArrayList(filter1);
        Map<AxisName, List<Filter>> filterMap = new HashMap<>();
        filterMap.put(AxisName.ROW, filters);

        List<Item> items = createTestItems();

        List<Item> actual = filterHelper.getFilterItems(items, filterMap);

        assertThat(actual.size()).isEqualTo(2);
        assertThat(actual.get(0)).isSameAs(items.get(0));
        assertThat(actual.get(1)).isSameAs(items.get(1));
    }

    @Test
    public void testGetFilterItemsPatternShouldThrowException() {
        Filter filter1 = factory.createFilter("value", "r][");
        List<Filter> filters = Lists.newArrayList(filter1);
        Map<AxisName, List<Filter>> filterMap = new HashMap<>();
        filterMap.put(AxisName.ROW, filters);

        List<Item> items = createTestItems();

        testRule.expect(StepRunException.class);
        filterHelper.getFilterItems(items, filterMap);
    }

    @Test
    public void testGetFilterItemsByMatch() {
        Filter filter1 = factory.createFilter("match", "r4");
        Filter filter2 = factory.createFilter("match", "r6");
        List<Filter> filters = Lists.newArrayList(filter1, filter2);
        Map<AxisName, List<Filter>> filterMap = new HashMap<>();
        filterMap.put(AxisName.ROW, filters);

        List<Item> items = createTestItems();

        List<Item> actual = filterHelper.getFilterItems(items, filterMap);

        assertThat(actual.size()).isEqualTo(2);
        assertThat(actual.get(0)).isSameAs(items.get(3));
        assertThat(actual.get(1)).isSameAs(items.get(5));

        filter1 = factory.createFilter("match", "c4");
        filter2 = factory.createFilter("match", "c5");
        Filter filter3 = factory.createFilter("match", "c10");
        filters = Lists.newArrayList(filter1, filter2, filter3);

        filterMap.put(AxisName.COL, filters);

        actual = filterHelper.getFilterItems(items, filterMap);

        assertThat(actual.size()).isEqualTo(3);
        assertThat(actual.get(0)).isSameAs(items.get(3));
        assertThat(actual.get(1)).isSameAs(items.get(4));
        assertThat(actual.get(2)).isSameAs(items.get(5));
    }

    @Test
    public void testFilter() {
        List<Item> items = createTestItems();

        Item m1 = items.get(0);
        Item m2 = items.get(1);
        Item m3 = items.get(2);
        Item m4 = items.get(3);
        Item m5 = items.get(4);
        Item m6 = items.get(5);

        Data data = factory.createData("dataDef1");
        data.addItem(m1);
        data.addItem(m2);
        data.addItem(m3);
        data.addItem(m4);
        data.addItem(m5);
        data.addItem(m6);

        ArrayList<Item> filterItems = Lists.newArrayList(m1, m2, m5);

        filterHelper.filter(data, filterItems);

        List<Item> actual = data.getItems();

        assertThat(actual.size()).isEqualTo(3);
        assertThat(actual).doesNotContain(m1, m2, m5);
        assertThat(actual).containsExactly(m3, m4, m6);
    }

    private List<Item> createTestItems() {
        List<Item> items = new ArrayList<>();

        items.add(createTestItem("value", "1"));
        items.add(createTestItem("value", "2"));
        items.add(createTestItem("value", "3"));

        items.add(createTestItem("match", "4"));
        items.add(createTestItem("match", "5"));
        items.add(createTestItem("match", "6"));
        return items;
    }

    private Item createTestItem(final String type, final String index) {
        Item item = factory.createItem();
        String mName = "m" + index;
        String colValue = "c" + index;
        String colMatch = null;
        String rowValue = "r" + index;
        String rowMatch = null;
        String factValue = "f" + index;
        String factMatch = null;
        if (type.equals("match")) {
            colValue = null;
            colMatch = "c" + index;
            rowValue = null;
            rowMatch = "r" + index;
            factValue = null;
            factMatch = "f" + index;
        }

        Axis col = factory.createAxis(AxisName.COL, mName, colValue, colMatch,
                0, 0);
        Axis row = factory.createAxis(AxisName.ROW, mName, rowValue, rowMatch,
                0, 0);
        Axis fact = factory.createAxis(AxisName.FACT, mName, factValue,
                factMatch, 0, 0);

        item.addAxis(col);
        item.addAxis(row);
        item.addAxis(fact);
        return item;
    }

}
