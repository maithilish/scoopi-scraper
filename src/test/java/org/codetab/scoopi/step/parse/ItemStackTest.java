package org.codetab.scoopi.step.parse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.Range;
import org.codetab.scoopi.defs.yml.AxisDefs;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.helper.ItemHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

public class ItemStackTest {

    @Mock
    private ItemHelper itemHelper;
    @Mock
    private AxisDefs axisDefs;
    @Mock
    private ItemMatrix itemMatrix;

    @InjectMocks
    private ItemStack stack;

    private ObjectFactory factory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        factory = new ObjectFactory();
    }

    @Test
    public void testPushAndPopItems() {
        Item item1 = factory.createItem();
        Item item2 = factory.createItem();
        List<Item> items = Lists.newArrayList(item1, item2);

        stack.pushItems(items);

        assertThat(stack.popItem()).isEqualTo(item2);
        assertThat(stack.popItem()).isEqualTo(item1);
        assertThat(stack.isEmpty()).isTrue();
    }

    @Test
    public void testPushAdjacentItemsNextItemNotYetCreated()
            throws DataDefNotFoundException, IllegalAccessException {

        // push some item - to test whether PushAdjacentItems uses
        // addFirst()
        stack.pushItems(Lists.newArrayList(factory.createItem()));

        Axis col = factory.createAxis(AxisName.COL, "item1");
        Item item = factory.createItem();
        item.addAxis(col);

        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "defJson");

        Optional<List<String>> breakAfters =
                Optional.of(Lists.newArrayList("x", "y"));
        Optional<Range<Integer>> indexRange = Optional.of(Range.between(1, 3));

        // Integer[] indexes = new Integer[] {1, 2};
        // Integer[] nextItemIndexes = new Integer[] {3, 4, 5};
        String nextItemIndexesKey = "345";
        Item itemCopy = Mockito.mock(Item.class);

        // given(itemHelper.getItemIndexesKey(item)).willReturn(indexes);
        given(axisDefs.getBreakAfters(dataDef, col)).willReturn(breakAfters);
        given(axisDefs.getIndexRange(dataDef, col)).willReturn(indexRange);
        given(itemHelper.isAxisWithinRange(col, breakAfters, indexRange))
                .willReturn(true);
        // given(itemMatrix.nextItemIndexes(indexes, col))
        // .willReturn(nextItemIndexes);
        given(itemHelper.getNextItemIndexesAsKey(item, col))
                .willReturn(nextItemIndexesKey);
        given(itemMatrix.notYetCreated(nextItemIndexesKey)).willReturn(true);
        given(itemMatrix.createAdjacentItem(item, col, nextItemIndexesKey))
                .willReturn(itemCopy);

        stack.pushAdjacentItems(dataDef, item);

        Item actual = stack.popItem();

        assertThat(actual).isSameAs(itemCopy);
    }

    @Test
    public void testPushAdjacentItemsNextItemAlreadyCreated()
            throws DataDefNotFoundException, IllegalAccessException {

        // push some item - to test whether PushAdjacentItems uses
        // addFirst()
        Item dummyItem = factory.createItem();
        stack.pushItems(Lists.newArrayList(dummyItem));

        Axis col = factory.createAxis(AxisName.COL, "item1");
        Item item = factory.createItem();
        item.addAxis(col);

        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "defJson");

        Optional<List<String>> breakAfters =
                Optional.of(Lists.newArrayList("x", "y"));
        Optional<Range<Integer>> indexRange = Optional.of(Range.between(1, 3));

        // Integer[] indexes = new Integer[] {1, 2};
        // Integer[] nextItemIndexes = new Integer[] {3, 4, 5};
        String nextItemIndexesKey = "345";

        // given(itemHelper.getItemIndexesKey(item)).willReturn(indexes);
        given(axisDefs.getBreakAfters(dataDef, col)).willReturn(breakAfters);
        given(axisDefs.getIndexRange(dataDef, col)).willReturn(indexRange);
        given(itemHelper.isAxisWithinRange(col, breakAfters, indexRange))
                .willReturn(true);
        // given(itemMatrix.nextItemIndexes(indexes, col))
        // .willReturn(nextItemIndexes);
        given(itemHelper.getNextItemIndexesAsKey(item, col))
                .willReturn(nextItemIndexesKey);
        given(itemMatrix.notYetCreated(nextItemIndexesKey)).willReturn(false);

        stack.pushAdjacentItems(dataDef, item);

        Item actual = stack.popItem();

        assertThat(actual).isSameAs(dummyItem);

        verify(itemMatrix, never()).createAdjacentItem(item, col,
                nextItemIndexesKey);
    }

    @Test
    public void testPushAdjacentItemsAxisNotWithinRange()
            throws DataDefNotFoundException, IllegalAccessException {

        // push some item - to test whether PushAdjacentItems uses
        // addFirst()
        Item dummyItem = factory.createItem();
        stack.pushItems(Lists.newArrayList(dummyItem));

        Axis col = factory.createAxis(AxisName.COL, "item1");
        Axis fact = factory.createAxis(AxisName.FACT, "fact");
        Item item = factory.createItem();
        item.addAxis(col);
        item.addAxis(fact);

        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "defJson");

        Optional<List<String>> breakAfters =
                Optional.of(Lists.newArrayList("x", "y"));
        Optional<Range<Integer>> indexRange = Optional.of(Range.between(1, 3));

        // Integer[] indexes = new Integer[] {1, 2};

        // given(itemHelper.getItemIndexesKey(item)).willReturn(indexes);
        given(axisDefs.getBreakAfters(dataDef, col)).willReturn(breakAfters);
        given(axisDefs.getIndexRange(dataDef, col)).willReturn(indexRange);
        given(itemHelper.isAxisWithinRange(col, breakAfters, indexRange))
                .willReturn(false);

        stack.pushAdjacentItems(dataDef, item);

        Item actual = stack.popItem();

        assertThat(actual).isSameAs(dummyItem);

        verifyZeroInteractions(itemMatrix);
    }

}
