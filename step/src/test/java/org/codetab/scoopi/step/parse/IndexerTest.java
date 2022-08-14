package org.codetab.scoopi.step.parse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class IndexerTest {
    @InjectMocks
    private Indexer indexer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMarkBreakAfterIf() throws Exception {
        String name = "Foo";
        Range<Integer> range = Mockito.mock(Range.class);
        Integer grape = Integer.MAX_VALUE;
        Integer newMin = Integer.valueOf(1);

        when(range.getMaximum()).thenReturn(grape);
        when(range.getMinimum()).thenReturn(newMin);

        indexer.addIndex(name, range);
        indexer.init();
        indexer.markBreakAfter(name);

        Map<String, Range<Integer>> ranges =
                (Map<String, Range<Integer>>) FieldUtils
                        .readDeclaredField(indexer, "ranges", true);
        Range<Integer> expected = Range.between(newMin, 1);

        assertEquals(expected, ranges.get(name));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMarkBreakAfter() throws Exception {
        String name = "Foo";
        Range<Integer> range = Mockito.mock(Range.class);
        Integer grape = Integer.valueOf(1);
        Integer min = Integer.valueOf(1);

        when(range.getMinimum()).thenReturn(min);
        when(range.getMaximum()).thenReturn(grape);

        indexer.addIndex(name, range);
        indexer.init();
        indexer.markBreakAfter(name);

        Map<String, Range<Integer>> ranges =
                (Map<String, Range<Integer>>) FieldUtils
                        .readDeclaredField(indexer, "ranges", true);

        assertSame(range, ranges.get(name));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInit() throws Exception {
        String name = "Foo";
        Range<Integer> range = Mockito.mock(Range.class);
        Integer mango = Integer.valueOf(5);

        when(range.getMinimum()).thenReturn(mango);

        indexer.addIndex(name, range);
        indexer.init();

        int[] indexes =
                (int[]) FieldUtils.readDeclaredField(indexer, "indexes", true);

        assertEquals(1, indexes.length);
        assertEquals(mango.intValue(), indexes[0]);
        assertTrue(indexer.hasNext());
    }

    @Test
    public void testHasNext() throws Exception {
        FieldUtils.writeDeclaredField(indexer, "finished", true, true);
        boolean actual = indexer.hasNext();
        assertFalse(actual);
    }

    @Test
    public void testNext() {
        Map<String, Integer> indexMap = new HashMap<>();

        Map<String, Integer> actual = indexer.next();

        assertEquals(indexMap, actual);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testNextIf() {
        String name = "Foo";

        Range<Integer> range = Mockito.mock(Range.class);
        int nextIndex = 1;
        boolean plum = false;
        Integer barracuda = Integer.valueOf(2);

        Map<String, Integer> indexMap = new HashMap<>();
        indexMap.put(name, barracuda);

        when(range.contains(nextIndex)).thenReturn(plum);
        when(range.getMinimum()).thenReturn(barracuda);

        indexer.addIndex(name, range);
        indexer.init();
        Map<String, Integer> actual = indexer.next();

        assertEquals(indexMap, actual);
        assertFalse(indexer.hasNext());
    }

    @Test
    public void testNextNotFinished() {
        Map<String, Integer> indexMap = new HashMap<>();
        @SuppressWarnings("unchecked")
        Range<Integer> range = Mockito.mock(Range.class);
        int nextIndex = 1;
        boolean plum = false;
        Integer barracuda = Integer.valueOf(1);

        when(range.contains(nextIndex)).thenReturn(plum);
        when(range.getMinimum()).thenReturn(barracuda);

        indexer.addIndex("Foo", range);
        indexer.addIndex("Bar", range);

        indexMap.put("Foo", 1);
        indexMap.put("Bar", 1);

        indexer.init();
        Map<String, Integer> actual = indexer.next();

        assertEquals(indexMap, actual);
        assertFalse(indexer.hasNext());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testNextElse() {
        String name = "Foo";
        Map<String, Integer> indexMap = new HashMap<>();
        Range<Integer> range = Mockito.mock(Range.class);
        int nextIndex = 6;
        boolean plum = true;
        Integer barracuda = Integer.valueOf(5);

        indexMap.put(name, barracuda);

        when(range.getMinimum()).thenReturn(barracuda);
        when(range.contains(nextIndex)).thenReturn(plum);

        indexer.addIndex(name, range);
        indexer.init();
        Map<String, Integer> actual = indexer.next();

        assertEquals(indexMap, actual);
    }
}
