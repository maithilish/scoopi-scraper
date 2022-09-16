package org.codetab.scoopi.step.process;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.codetab.scoopi.defs.IItemDef;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.Filter;
import org.codetab.scoopi.model.Item;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class FilterHelperTest {
    @InjectMocks
    private FilterHelper filterHelper;

    @Mock
    private IItemDef itemDef;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFilterIfFiltersIsPresentIfRequireFilter() {
        Item item = Mockito.mock(Item.class);
        String dataDef = "Foo";
        Axis axis = Mockito.mock(Axis.class);
        List<Axis> list = new ArrayList<>();
        String itemName = "Bar";

        List<Filter> filterList = new ArrayList<>();
        Optional<List<Filter>> filters = Optional.of(filterList);

        String axisValue = " "; // blank
        String axisMatch = "Qux";
        Filter filter = Mockito.mock(Filter.class);
        String grape = "Quux";
        String pattern = " "; // blank
        list.add(axis);
        filterList.add(filter);

        when(item.getAxes()).thenReturn(list);
        when(axis.getItemName()).thenReturn(itemName);
        when(itemDef.getFilter(dataDef, itemName)).thenReturn(filters);
        when(axis.getValue()).thenReturn(axisValue);
        when(axis.getMatch()).thenReturn(axisMatch);
        when(filter.getType()).thenReturn(grape);
        when(filter.getPattern()).thenReturn(pattern);

        boolean actual = filterHelper.filter(item, dataDef);

        assertTrue(actual);
    }

    @Test
    public void testFilterIfFiltersIsPresentElseRequireFilter() {
        Item item = Mockito.mock(Item.class);
        String dataDef = "Foo";
        Axis axis = Mockito.mock(Axis.class);
        List<Axis> list = new ArrayList<>();
        String itemName = "Bar";

        List<Filter> filterList = new ArrayList<>();
        Optional<List<Filter>> filters = Optional.of(filterList);

        String axisValue = "Baz";
        String axisMatch = "Qux";
        Filter filter = Mockito.mock(Filter.class);
        String grape = "Quux";
        String pattern = "Corge";
        list.add(axis);
        filterList.add(filter);

        when(item.getAxes()).thenReturn(list);
        when(axis.getItemName()).thenReturn(itemName);
        when(itemDef.getFilter(dataDef, itemName)).thenReturn(filters);
        when(axis.getValue()).thenReturn(axisValue);
        when(axis.getMatch()).thenReturn(axisMatch);
        when(filter.getType()).thenReturn(grape);
        when(filter.getPattern()).thenReturn(pattern);

        boolean actual = filterHelper.filter(item, dataDef);

        assertFalse(actual);
    }

    @Test
    public void testFilterElseFiltersIsPresent() {
        Item item = Mockito.mock(Item.class);
        String dataDef = "Foo";
        Axis axis = Mockito.mock(Axis.class);
        List<Axis> list = new ArrayList<>();
        String itemName = "Bar";
        Optional<List<Filter>> filters = Optional.empty();
        String axisValue = "Baz";
        String axisMatch = "Qux";
        Filter filter = Mockito.mock(Filter.class);
        String grape = "Quux";
        String pattern = "Corge";
        list.add(axis);

        when(item.getAxes()).thenReturn(list);
        when(axis.getItemName()).thenReturn(itemName);
        when(itemDef.getFilter(dataDef, itemName)).thenReturn(filters);
        when(axis.getValue()).thenReturn(axisValue);
        when(axis.getMatch()).thenReturn(axisMatch);
        when(filter.getType()).thenReturn(grape);
        when(filter.getPattern()).thenReturn(pattern);

        boolean actual = filterHelper.filter(item, dataDef);

        assertFalse(actual);
    }

    @Test
    public void testFilterIfFiltersIfRequireFilterTypeIsMatch() {
        Item item = Mockito.mock(Item.class);
        String dataDef = "Foo";
        Axis axis = Mockito.mock(Axis.class);
        List<Axis> list = new ArrayList<>();
        String itemName = "Bar";

        List<Filter> filterList = new ArrayList<>();
        Optional<List<Filter>> filters = Optional.of(filterList);

        String axisValue = "Baz";
        String axisMatch = "Qux";
        Filter filter = Mockito.mock(Filter.class);

        String grape = "match"; // filter type is match
        String pattern = axisMatch; // pattern is axisMatch

        list.add(axis);
        filterList.add(filter);

        when(item.getAxes()).thenReturn(list);
        when(axis.getItemName()).thenReturn(itemName);
        when(itemDef.getFilter(dataDef, itemName)).thenReturn(filters);
        when(axis.getValue()).thenReturn(axisValue);
        when(axis.getMatch()).thenReturn(axisMatch);
        when(filter.getType()).thenReturn(grape);
        when(filter.getPattern()).thenReturn(pattern);

        boolean actual = filterHelper.filter(item, dataDef);

        assertTrue(actual);
    }

    @Test
    public void testFilterIfFiltersIfRequireValueNotBlankPatternBlank() {
        Item item = Mockito.mock(Item.class);
        String dataDef = "Foo";
        Axis axis = Mockito.mock(Axis.class);
        List<Axis> list = new ArrayList<>();
        String itemName = "Bar";

        List<Filter> filterList = new ArrayList<>();
        Optional<List<Filter>> filters = Optional.of(filterList);

        String axisValue = "Baz";
        String axisMatch = "Qux";
        Filter filter = Mockito.mock(Filter.class);

        String grape = "Quux"; // filter type is match
        String pattern = " "; // pattern is axisMatch

        list.add(axis);
        filterList.add(filter);

        when(item.getAxes()).thenReturn(list);
        when(axis.getItemName()).thenReturn(itemName);
        when(itemDef.getFilter(dataDef, itemName)).thenReturn(filters);
        when(axis.getValue()).thenReturn(axisValue);
        when(axis.getMatch()).thenReturn(axisMatch);
        when(filter.getType()).thenReturn(grape);
        when(filter.getPattern()).thenReturn(pattern);

        boolean actual = filterHelper.filter(item, dataDef);

        assertFalse(actual);
    }

    @Test
    public void testFilterIfFiltersIfRequirePatternMatch() {
        Item item = Mockito.mock(Item.class);
        String dataDef = "Foo";
        Axis axis = Mockito.mock(Axis.class);
        List<Axis> list = new ArrayList<>();
        String itemName = "Bar";

        List<Filter> filterList = new ArrayList<>();
        Optional<List<Filter>> filters = Optional.of(filterList);

        String axisValue = "abc";
        String axisMatch = "Qux";
        Filter filter = Mockito.mock(Filter.class);

        String grape = "Quux"; // filter type is match
        String pattern = "^abc"; // pattern is axisMatch

        list.add(axis);
        filterList.add(filter);

        when(item.getAxes()).thenReturn(list);
        when(axis.getItemName()).thenReturn(itemName);
        when(itemDef.getFilter(dataDef, itemName)).thenReturn(filters);
        when(axis.getValue()).thenReturn(axisValue);
        when(axis.getMatch()).thenReturn(axisMatch);
        when(filter.getType()).thenReturn(grape);
        when(filter.getPattern()).thenReturn(pattern);

        boolean actual = filterHelper.filter(item, dataDef);

        assertTrue(actual);
    }

    @Test
    public void testFilterIfFiltersIfRequirePatternSyntaxException() {
        Item item = Mockito.mock(Item.class);
        String dataDef = "Foo";
        Axis axis = Mockito.mock(Axis.class);
        List<Axis> list = new ArrayList<>();
        String itemName = "Bar";

        List<Filter> filterList = new ArrayList<>();
        Optional<List<Filter>> filters = Optional.of(filterList);

        String axisValue = "abc";
        String axisMatch = "Qux";
        Filter filter = Mockito.mock(Filter.class);

        String grape = "Quux"; // filter type is match
        String pattern = "][abc"; // pattern is axisMatch

        list.add(axis);
        filterList.add(filter);

        when(item.getAxes()).thenReturn(list);
        when(axis.getItemName()).thenReturn(itemName);
        when(itemDef.getFilter(dataDef, itemName)).thenReturn(filters);
        when(axis.getValue()).thenReturn(axisValue);
        when(axis.getMatch()).thenReturn(axisMatch);
        when(filter.getType()).thenReturn(grape);
        when(filter.getPattern()).thenReturn(pattern);

        assertThrows(StepRunException.class,
                () -> filterHelper.filter(item, dataDef));
    }
}
