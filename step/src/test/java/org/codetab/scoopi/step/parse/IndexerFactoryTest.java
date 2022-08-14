package org.codetab.scoopi.step.parse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Range;
import org.codetab.scoopi.defs.IItemDef;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class IndexerFactoryTest {
    @InjectMocks
    private IndexerFactory indexerFactory;

    @Mock
    private IItemDef itemDef;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateIndexer() {
        String dataDef = "Foo";
        List<String> itemNames = new ArrayList<>();

        Indexer indexer = new Indexer();
        indexer.addIndex(dataDef, Range.between(1, 1));

        String itemName = "Bar";
        itemNames.add(itemName);
        itemNames.add("fact");

        Range<Integer> range = Mockito.mock(Range.class);
        Range<Integer> factRange = Mockito.mock(Range.class);

        when(itemDef.getIndexRange(dataDef, itemName)).thenReturn(range);
        when(itemDef.getIndexRange(dataDef, "fact")).thenReturn(factRange);
        when(range.getMinimum()).thenReturn(1);
        when(factRange.getMinimum()).thenReturn(2);

        Indexer actual = indexerFactory.createIndexer(dataDef, itemNames);

        Map<String, Integer> indexMap = actual.next();
        assertEquals(Integer.valueOf(1), indexMap.get(itemName));
        assertEquals(Integer.valueOf(2), indexMap.get("fact"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateIndexerNoFact() {
        String dataDef = "Foo";
        List<String> itemNames = new ArrayList<>();

        Indexer indexer = new Indexer();
        indexer.addIndex(dataDef, Range.between(1, 1));

        String itemName = "Bar";
        itemNames.add(itemName);

        Range<Integer> range = Mockito.mock(Range.class);

        when(itemDef.getIndexRange(dataDef, itemName)).thenReturn(range);
        when(range.getMinimum()).thenReturn(1);

        assertThrows(IllegalStateException.class,
                () -> indexerFactory.createIndexer(dataDef, itemNames));
    }
}
