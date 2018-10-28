package org.codetab.scoopi.model.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.commons.lang3.Range;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.ObjectFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

public class ItemHelperTest {

    @InjectMocks
    private ItemHelper itemHelper;

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
    public void testCopy() {
        Axis fact = factory.createAxis(AxisName.FACT, "fact");
        Axis col = factory.createAxis(AxisName.COL, "date");
        Axis row = factory.createAxis(AxisName.ROW, "item");

        Item item = createTestItem();
        item.addAxis(fact);
        item.addAxis(col);
        item.addAxis(row);

        Item actual = itemHelper.copy(item);

        assertThat(actual).isNotSameAs(item);
        assertThat(actual).isEqualTo(item);
    }

    @Test
    public void testGetNextItemIndexesKey() {
        Item item = createTestItem();

        // indexes: fact 10, col 20, row 30, page 0

        String actual = itemHelper.getNextItemIndexesAsKey(item,
                item.getAxis(AxisName.FACT));

        assertThat(actual).isEqualTo("1120300");

        actual = itemHelper.getNextItemIndexesAsKey(item,
                item.getAxis(AxisName.COL));

        assertThat(actual).isEqualTo("1021300");

        actual = itemHelper.getNextItemIndexesAsKey(item,
                item.getAxis(AxisName.ROW));

        assertThat(actual).isEqualTo("1020310");
    }

    @Test
    public void testGetNextItemIndexesKeyShouldThrowException() {
        Item item = createTestItem();

        // indexes: fact 10, col 20, row 30, page 0
        testRule.expect(NoSuchElementException.class);
        itemHelper.getNextItemIndexesAsKey(item, item.getAxis(AxisName.PAGE));
    }

    @Test
    public void testGetItemIndexesNullIndex() {
        Item item = createTestItem();
        item.getAxis(AxisName.COL).setIndex(null);

        // indexes: fact 10, col null, row 30, page 0
        String actual = itemHelper.getNextItemIndexesAsKey(item,
                item.getAxis(AxisName.COL));
        assertThat(actual).isEqualTo("101300");
    }

    @Test
    public void testIsAxisWithinRangeNotRangeAxis()
            throws NumberFormatException {
        Axis fact = factory.createAxis(AxisName.FACT, "factItem");
        Optional<Range<Integer>> indexRange = Optional.empty();
        Optional<List<String>> breakAfters = Optional.empty();
        boolean actual =
                itemHelper.isAxisWithinRange(fact, breakAfters, indexRange);
        assertThat(actual).isFalse();
    }

    @Test
    public void testIsAxisWithinRange() throws NumberFormatException {
        Axis row = factory.createAxis(AxisName.ROW, "item");

        Optional<List<String>> breakAfters = Optional.empty();
        Optional<Range<Integer>> indexRange = Optional.empty();

        // not range axis
        row.setValue("z");
        boolean actual =
                itemHelper.isAxisWithinRange(row, breakAfters, indexRange);
        assertThat(actual).isFalse();

        // breakAfters exists but value not matches
        breakAfters = Optional.of(Lists.newArrayList("x", "y"));
        indexRange = Optional.empty();
        row.setValue("z");
        actual = itemHelper.isAxisWithinRange(row, breakAfters, indexRange);
        assertThat(actual).isTrue();

        // breakAfters exists and value matches
        breakAfters = Optional.of(Lists.newArrayList("x", "y"));
        indexRange = Optional.empty();
        row.setValue("x");
        actual = itemHelper.isAxisWithinRange(row, breakAfters, indexRange);
        assertThat(actual).isFalse();

        // indexRange exists and index within range
        breakAfters = Optional.empty();
        indexRange = Optional.of(Range.between(1, 3));
        row.setIndex(2);
        actual = itemHelper.isAxisWithinRange(row, breakAfters, indexRange);
        assertThat(actual).isTrue();

        // indexRange exists but index outside the range
        breakAfters = Optional.empty();
        indexRange = Optional.of(Range.between(1, 3));
        row.setIndex(3);
        actual = itemHelper.isAxisWithinRange(row, breakAfters, indexRange);
        assertThat(actual).isFalse();
    }

    @Test
    public void testIsAxisWithinRangeShouldThrowException()
            throws NumberFormatException {
        Axis row = factory.createAxis(AxisName.ROW, "item");

        Optional<List<String>> breakAfters =
                Optional.of(Lists.newArrayList("x", "y"));
        Optional<Range<Integer>> indexRange = Optional.of(Range.between(1, 3));

        row.setValue(null);

        testRule.expect(StepRunException.class);
        itemHelper.isAxisWithinRange(row, breakAfters, indexRange);
    }

    public Item createTestItem() {
        Item item = factory.createItem();

        Axis fact = factory.createAxis(AxisName.FACT, "factItem");
        fact.setIndex(10);
        item.addAxis(fact);

        Axis col = factory.createAxis(AxisName.COL, "colItem");
        col.setIndex(20);
        item.addAxis(col);

        Axis row = factory.createAxis(AxisName.ROW, "rowItem");
        row.setIndex(30);
        item.addAxis(row);
        return item;
    }

}
