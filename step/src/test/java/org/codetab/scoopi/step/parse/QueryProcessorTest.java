package org.codetab.scoopi.step.parse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.codetab.scoopi.defs.IItemDef;
import org.codetab.scoopi.model.Query;
import org.codetab.scoopi.step.parse.cache.ParserCache;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class QueryProcessorTest {
    @InjectMocks
    private QueryProcessor queryProcessor;

    @Mock
    private IItemDef itemDef;
    @Mock
    private ParserCache parserCache;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetQueriesIf() {
        String dataDef = "Foo";
        String axisName = "fact";
        String itemName = "Baz";
        Map<String, String> queries = new HashMap<>();
        Query query = Mockito.mock(Query.class);
        Optional<Query> itemQuery = Optional.of(query);
        String block = "Qux";
        String selector = "Quux";

        queries.put("block", block);
        queries.put("selector", selector);

        when(itemDef.getQuery(dataDef)).thenReturn(query);
        when(itemDef.getItemQuery(dataDef, itemName)).thenReturn(itemQuery);
        when(query.getQuery("block")).thenReturn(block);
        when(query.getQuery("selector")).thenReturn(selector);

        Map<String, String> actual =
                queryProcessor.getQueries(dataDef, axisName, itemName);

        assertEquals(queries, actual);
    }

    @Test
    public void testGetQueriesIf2() {
        String dataDef = "Foo";
        String axisName = "Bar";
        String itemName = "Baz";
        Map<String, String> queries = new HashMap<>();
        Query query = Mockito.mock(Query.class);

        String block = "Qux";
        Query query2 = Mockito.mock(Query.class);
        Optional<Query> itemQuery = Optional.of(query2);
        String selector = "Quux";

        queries.put("block", block);
        queries.put("selector", selector);

        when(itemDef.getQuery(dataDef)).thenReturn(query);
        when(itemDef.getItemQuery(dataDef, itemName)).thenReturn(itemQuery);
        when(query.getQuery("block")).thenReturn(block);
        when(query2.getQuery("selector")).thenReturn(selector);

        Map<String, String> actual =
                queryProcessor.getQueries(dataDef, axisName, itemName);

        assertEquals(queries, actual);
    }

    @Test
    public void testGetQueriesElseThrowException() {
        String dataDef = "Foo";
        String axisName = "Bar";
        String itemName = "Baz";
        Query query = Mockito.mock(Query.class);
        Optional<Query> itemQuery = Optional.empty();

        when(itemDef.getQuery(dataDef)).thenReturn(query);
        when(itemDef.getItemQuery(dataDef, itemName)).thenReturn(itemQuery);

        assertThrows(NoSuchElementException.class,
                () -> queryProcessor.getQueries(dataDef, axisName, itemName));

    }

    @Test
    public void testGetQueriesIf3() {
        String dataDef = "Foo";
        String axisName = "Bar";
        String itemName = "Baz";
        Map<String, String> queries = new HashMap<>();
        Query query = Mockito.mock(Query.class);
        Optional<Query> itemQuery = Optional.of(query);
        String block = "Qux";
        String selector = "Quux attribute: x";

        queries.put("block", block);
        queries.put("selector", "Quux");
        queries.put("attribute", "x");

        when(itemDef.getQuery(dataDef)).thenReturn(query);
        when(itemDef.getItemQuery(dataDef, itemName)).thenReturn(itemQuery);
        when(query.getQuery("block")).thenReturn(block);
        when(query.getQuery("selector")).thenReturn(selector);

        Map<String, String> actual =
                queryProcessor.getQueries(dataDef, axisName, itemName);

        assertEquals(queries, actual);
    }

    @Test
    public void testQueryIfNull() throws Exception {
        Map<String, String> queries = new HashMap<>();
        IValueParser valueParser = Mockito.mock(IValueParser.class);
        int key = 1;
        String value = null; // value is null

        when(parserCache.getKey(queries)).thenReturn(key);
        when(parserCache.get(key)).thenReturn(value);
        when(valueParser.parseValue(queries)).thenReturn(value);

        String actual = queryProcessor.query(queries, valueParser);

        assertEquals(value, actual);
        verify(parserCache).put(key, value);
    }

    @Test
    public void testQuery() throws Exception {
        Map<String, String> queries = new HashMap<>();
        IValueParser valueParser = Mockito.mock(IValueParser.class);
        int key = 1;
        String value = "Foo";

        when(parserCache.getKey(queries)).thenReturn(key);
        when(parserCache.get(key)).thenReturn(value);

        String actual = queryProcessor.query(queries, valueParser);

        assertEquals(value, actual);
    }
}
