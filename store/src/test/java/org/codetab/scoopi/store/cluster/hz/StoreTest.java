package org.codetab.scoopi.store.cluster.hz;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.scoopi.store.ICluster;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

public class StoreTest {
    @InjectMocks
    private Store store;

    @Mock
    private ICluster cluster;
    @Mock
    private IMap<String, Object> objectMap;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testOpen() throws Exception {
        HazelcastInstance hz = Mockito.mock(HazelcastInstance.class);
        @SuppressWarnings("unchecked")
        IMap<Object, Object> objectMap1 = Mockito.mock(IMap.class);

        when(cluster.getInstance()).thenReturn(hz);
        when(hz.getMap(DsName.STORE_MAP.toString())).thenReturn(objectMap1);

        store.open();

        assertSame(objectMap1,
                FieldUtils.readDeclaredField(store, "objectMap", true));
    }

    @Test
    public void testClose() {
        store.close();
    }

    @Test
    public void testPut() {
        String key = "Foo";
        Object value = Mockito.mock(Object.class);

        boolean actual = store.put(key, value);

        assertTrue(actual);
        verify(objectMap).set(key, value);
    }

    @Test
    public void testGet() {
        String key = "Foo";
        Object apple = Mockito.mock(Object.class);

        when(objectMap.get(key)).thenReturn(apple);

        Object actual = store.get(key);

        assertSame(apple, actual);
    }

    @Test
    public void testContains() {
        String key = "Foo";
        boolean apple = true;

        when(objectMap.containsKey(key)).thenReturn(apple);

        boolean actual = store.contains(key);

        assertTrue(actual);
    }
}

