package org.codetab.scoopi.store.solo.simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class StoreTest {
    @InjectMocks
    private Store store;

    @Mock
    private Map<String, Object> cache;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testOpen() {
        store.open();
        store.put("foo", "bar");
        assertEquals("bar", store.get("foo"));

        store.open();
        assertFalse(store.contains("foo"));
    }

    @Test
    public void testClose() {
        assertThrows(NotImplementedException.class, () -> store.close());
    }

    @Test
    public void testPut() {
        String key = "Foo";
        Object value = Mockito.mock(Object.class);

        boolean actual = store.put(key, value);

        assertTrue(actual);
        verify(cache).put(key, value);
    }

    @Test
    public void testGet() {
        String key = "Foo";
        Object apple = Mockito.mock(Object.class);

        when(cache.get(key)).thenReturn(apple);

        Object actual = store.get(key);

        assertSame(apple, actual);
    }

    @Test
    public void testContains() {
        String key = "Foo";
        boolean apple = true;

        when(cache.containsKey(key)).thenReturn(apple);

        boolean actual = store.contains(key);

        assertTrue(actual);
    }
}
