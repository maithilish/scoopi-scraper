package org.codetab.scoopi.store.cluster.hz;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.codetab.scoopi.store.cluster.hz.Barricade.State;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionTimedOutException;
import com.hazelcast.transaction.TransactionalMap;

public class BarricadeTxTest {
    @InjectMocks
    private BarricadeTx barricadeTx;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAcquireStateNotSet() {
        String barricadeKey = "Foo";
        String memberId = "Bar";
        TransactionContext tx = Mockito.mock(TransactionContext.class);
        String ownerKey = barricadeKey + "_owner";
        String objectMapKey = DsName.STORE_MAP.toString();
        @SuppressWarnings("unchecked")
        TransactionalMap<Object, Object> txCache =
                Mockito.mock(TransactionalMap.class);
        State state = null;

        when(tx.getMap(objectMapKey)).thenReturn(txCache);
        when(txCache.getForUpdate(barricadeKey)).thenReturn(state);

        boolean actual = barricadeTx.acquire(barricadeKey, memberId, tx);

        assertTrue(actual);
        verify(tx).beginTransaction();
        verify(txCache).set(barricadeKey, State.INITIALIZE);
        verify(txCache).set(ownerKey, memberId);
        verify(tx).commitTransaction();
        verify(tx, never()).rollbackTransaction();
    }

    @Test
    public void testAcquireStateIsSet() {
        String barricadeKey = "Foo";
        String memberId = "Bar";
        TransactionContext tx = Mockito.mock(TransactionContext.class);
        String ownerKey = barricadeKey + "_owner";
        String objectMapKey = DsName.STORE_MAP.toString();
        @SuppressWarnings("unchecked")
        TransactionalMap<Object, Object> txCache =
                Mockito.mock(TransactionalMap.class);
        State state = State.INITIALIZE;

        when(tx.getMap(objectMapKey)).thenReturn(txCache);
        when(txCache.getForUpdate(barricadeKey)).thenReturn(state);

        boolean actual = barricadeTx.acquire(barricadeKey, memberId, tx);

        assertFalse(actual);
        verify(tx).beginTransaction();
        verify(txCache, never()).set(barricadeKey, State.INITIALIZE);
        verify(txCache, never()).set(ownerKey, memberId);
        verify(tx).commitTransaction();
        verify(tx, never()).rollbackTransaction();
    }

    @Test
    public void testAcquireException() {
        String barricadeKey = "Foo";
        String memberId = "Bar";
        TransactionContext tx = Mockito.mock(TransactionContext.class);
        String ownerKey = barricadeKey + "_owner";
        String objectMapKey = DsName.STORE_MAP.toString();
        @SuppressWarnings("unchecked")
        TransactionalMap<Object, Object> txCache =
                Mockito.mock(TransactionalMap.class);

        when(tx.getMap(objectMapKey)).thenReturn(txCache);
        when(txCache.getForUpdate(barricadeKey))
                .thenThrow(TransactionTimedOutException.class);

        boolean actual = barricadeTx.acquire(barricadeKey, memberId, tx);

        assertFalse(actual);
        verify(tx).beginTransaction();
        verify(txCache, never()).set(barricadeKey, State.INITIALIZE);
        verify(txCache, never()).set(ownerKey, memberId);
        verify(tx, never()).commitTransaction();
        verify(tx).rollbackTransaction();
    }

    @Test
    public void testResetStateRemoved() {
        String barricadeKey = "Foo";
        String owner = "Bar";
        String ownerShort = "Baz";
        TransactionContext tx = Mockito.mock(TransactionContext.class);
        String ownerKey = barricadeKey + "_owner";
        String objectMapKey = DsName.STORE_MAP.toString();
        @SuppressWarnings("unchecked")
        TransactionalMap<Object, Object> txCache =
                Mockito.mock(TransactionalMap.class);
        boolean grape = true;

        when(tx.getMap(objectMapKey)).thenReturn(txCache);
        when(txCache.remove(ownerKey, owner)).thenReturn(grape);
        // state removed
        when(txCache.remove(barricadeKey, State.INITIALIZE)).thenReturn(true);
        barricadeTx.reset(barricadeKey, owner, ownerShort, tx);

        verify(tx).beginTransaction();
        verify(txCache).getForUpdate(barricadeKey);
        verify(txCache).getForUpdate(ownerKey);
        verify(tx).commitTransaction();
        verify(tx, never()).rollbackTransaction();
    }

    @Test
    public void testResetStateNotRemoved() {
        String barricadeKey = "Foo";
        String owner = "Bar";
        String ownerShort = "Baz";
        TransactionContext tx = Mockito.mock(TransactionContext.class);
        String ownerKey = barricadeKey + "_owner";
        String objectMapKey = DsName.STORE_MAP.toString();
        @SuppressWarnings("unchecked")
        TransactionalMap<Object, Object> txCache =
                Mockito.mock(TransactionalMap.class);
        boolean grape = true;

        when(tx.getMap(objectMapKey)).thenReturn(txCache);
        when(txCache.remove(ownerKey, owner)).thenReturn(grape);
        // state not removed
        when(txCache.remove(barricadeKey, State.INITIALIZE)).thenReturn(false);
        barricadeTx.reset(barricadeKey, owner, ownerShort, tx);

        verify(tx).beginTransaction();
        verify(txCache).getForUpdate(barricadeKey);
        verify(txCache).getForUpdate(ownerKey);
        verify(tx).commitTransaction();
        verify(tx, never()).rollbackTransaction();
    }

    @Test
    public void testResetStateOwnerNotRemoved() {
        String barricadeKey = "Foo";
        String owner = "Bar";
        String ownerShort = "Baz";
        TransactionContext tx = Mockito.mock(TransactionContext.class);
        String ownerKey = barricadeKey + "_owner";
        String objectMapKey = DsName.STORE_MAP.toString();
        @SuppressWarnings("unchecked")
        TransactionalMap<Object, Object> txCache =
                Mockito.mock(TransactionalMap.class);

        when(tx.getMap(objectMapKey)).thenReturn(txCache);
        // owner not removed
        when(txCache.remove(ownerKey, owner)).thenReturn(false);
        when(txCache.remove(barricadeKey, State.INITIALIZE)).thenReturn(false);
        barricadeTx.reset(barricadeKey, owner, ownerShort, tx);

        verify(tx).beginTransaction();
        verify(txCache).getForUpdate(barricadeKey);
        verify(txCache).getForUpdate(ownerKey);
        verify(tx).commitTransaction();
        verify(tx, never()).rollbackTransaction();
    }

    @Test
    public void testResetException() {
        String barricadeKey = "Foo";
        String owner = "Bar";
        String ownerShort = "Baz";
        TransactionContext tx = Mockito.mock(TransactionContext.class);
        String ownerKey = barricadeKey + "_owner";
        @SuppressWarnings("unchecked")
        TransactionalMap<Object, Object> txCache =
                Mockito.mock(TransactionalMap.class);

        doThrow(IllegalStateException.class).when(tx).beginTransaction();
        barricadeTx.reset(barricadeKey, owner, ownerShort, tx);

        verify(txCache, never()).getForUpdate(barricadeKey);
        verify(txCache, never()).getForUpdate(ownerKey);
        verify(tx, never()).commitTransaction();
        verify(tx).rollbackTransaction();
    }

    @Test
    public void testRelease() {
        String barricadeKey = "Foo";
        TransactionContext tx = Mockito.mock(TransactionContext.class);
        String ownerKey = barricadeKey + "_owner";
        String objectMapKey = DsName.STORE_MAP.toString();
        @SuppressWarnings("unchecked")
        TransactionalMap<Object, Object> txCache =
                Mockito.mock(TransactionalMap.class);

        when(tx.getMap(objectMapKey)).thenReturn(txCache);

        boolean actual = barricadeTx.release(barricadeKey, tx);

        assertTrue(actual);
        verify(tx).beginTransaction();
        verify(txCache).delete(ownerKey);
        verify(txCache).set(barricadeKey, State.READY);
        verify(tx).commitTransaction();
        verify(tx, never()).rollbackTransaction();
    }

    @Test
    public void testReleaseException() {
        String barricadeKey = "Foo";
        TransactionContext tx = Mockito.mock(TransactionContext.class);
        String ownerKey = barricadeKey + "_owner";

        @SuppressWarnings("unchecked")
        TransactionalMap<Object, Object> txCache =
                Mockito.mock(TransactionalMap.class);

        doThrow(IllegalStateException.class).when(tx).beginTransaction();

        boolean actual = barricadeTx.release(barricadeKey, tx);

        assertFalse(actual);
        verify(txCache, never()).delete(ownerKey);
        verify(txCache, never()).set(barricadeKey, State.READY);
        verify(tx, never()).commitTransaction();
        verify(tx).rollbackTransaction();
    }
}
