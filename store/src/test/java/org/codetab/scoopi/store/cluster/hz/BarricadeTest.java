package org.codetab.scoopi.store.cluster.hz;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.scoopi.store.ICluster;
import org.codetab.scoopi.store.cluster.hz.Barricade.State;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;

public class BarricadeTest {
    @InjectMocks
    private Barricade barricade;

    @Mock
    private ICluster cluster;
    @Mock
    private CrashedMembers crashedMembers;
    @Mock
    private BarricadeTx barricadeTx;
    @Mock
    private TransactionOptions txOptions;
    @Mock
    private HazelcastInstance hz;
    @Mock
    private IMap<Object, Object> objectMap;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSetup() {
        String name = "Foo";

        when(cluster.getInstance()).thenReturn(hz);
        when(hz.getMap(DsName.STORE_MAP.toString())).thenReturn(objectMap);
        barricade.setup(name);
    }

    @Test
    public void testAwaitAllowedToCross() throws IllegalAccessException {
        String memberId = "Foo";
        String memberIdShort = "Bar";
        TransactionContext orange = Mockito.mock(TransactionContext.class);
        boolean allowedToCross = true;
        State state = null;
        String barricadeKey = "Quux";

        FieldUtils.writeDeclaredField(barricade, "barricadeKey", barricadeKey,
                true);

        when(cluster.getMemberId()).thenReturn(memberId);
        when(cluster.getShortId(memberId)).thenReturn(memberIdShort);
        when(objectMap.get(barricadeKey)).thenReturn(state);
        when(barricadeTx.acquire(barricadeKey, memberId, orange))
                .thenReturn(allowedToCross);
        when(hz.newTransactionContext(txOptions)).thenReturn(orange);

        assertFalse(barricade.isAllowed());

        barricade.await();

        assertTrue(barricade.isAllowed());
    }

    @Test
    public void testAwaitReadyState() throws IllegalAccessException {
        String barricadeKey = "Quux";

        FieldUtils.writeDeclaredField(barricade, "barricadeKey", barricadeKey,
                true);

        /*
         * First three to skip allowed to cross block, test debug and skip crash
         * block. Next three to skip allowed to cross block, test ready state
         * first branch and skip crash block. Last two to skip allowed to cross
         * block and test ready state second branch and break while loop.
         */
        when(objectMap.get(barricadeKey)).thenReturn(State.INITIALIZE)
                .thenReturn(null).thenReturn(null).thenReturn(State.INITIALIZE)
                .thenReturn(State.INITIALIZE).thenReturn(null)
                .thenReturn(State.INITIALIZE).thenReturn(State.READY);

        assertFalse(barricade.isAllowed());

        barricade.await();

        assertFalse(barricade.isAllowed());
    }

    @Test
    public void testAwaitOwnerNotSet() throws IllegalAccessException {

        String barricadeKey = "Quux";
        String owner = null; // not set
        String ownerKey = "Corge";
        String ownerShort = "Qux";
        TransactionContext tx2 = Mockito.mock(TransactionContext.class);

        FieldUtils.writeDeclaredField(barricade, "barricadeKey", barricadeKey,
                true);
        FieldUtils.writeDeclaredField(barricade, "ownerKey", ownerKey, true);

        // return null twice to test crashed block then to break the while
        when(objectMap.get(barricadeKey)).thenReturn(null).thenReturn(null)
                .thenReturn(State.READY);
        when(objectMap.get(ownerKey)).thenReturn(owner);

        assertFalse(barricade.isAllowed());

        barricade.await();

        verify(barricadeTx, never()).reset(barricadeKey, owner, ownerShort,
                tx2);

        assertFalse(barricade.isAllowed());
    }

    @Test
    public void testAwaitResetCrashed() throws IllegalAccessException {

        String barricadeKey = "Quux";
        String owner = "Baz";
        String ownerKey = "Corge";
        String ownerShort = "Qux";
        TransactionContext tx2 = Mockito.mock(TransactionContext.class);

        FieldUtils.writeDeclaredField(barricade, "barricadeKey", barricadeKey,
                true);
        FieldUtils.writeDeclaredField(barricade, "ownerKey", ownerKey, true);

        // return null twice to test crashed block then to break the while
        when(objectMap.get(barricadeKey)).thenReturn(null).thenReturn(null)
                .thenReturn(State.READY);
        when(objectMap.get(ownerKey)).thenReturn(owner);
        when(hz.newTransactionContext(txOptions)).thenReturn(tx2);
        when(cluster.getShortId(owner)).thenReturn(ownerShort);
        when(crashedMembers.contains(owner)).thenReturn(true);

        assertFalse(barricade.isAllowed());

        barricade.await();

        verify(barricadeTx).reset(barricadeKey, owner, ownerShort, tx2);

        assertFalse(barricade.isAllowed());
    }

    @Test
    public void testAwaitCrashedButNotOwner() throws IllegalAccessException {

        String barricadeKey = "Quux";
        String owner = "Baz";
        String ownerKey = "Corge";
        String ownerShort = "Qux";
        TransactionContext tx2 = Mockito.mock(TransactionContext.class);

        FieldUtils.writeDeclaredField(barricade, "barricadeKey", barricadeKey,
                true);
        FieldUtils.writeDeclaredField(barricade, "ownerKey", ownerKey, true);

        // return null twice to test crashed block then to break the while
        when(objectMap.get(barricadeKey)).thenReturn(null).thenReturn(null)
                .thenReturn(State.READY);
        when(objectMap.get(ownerKey)).thenReturn(owner);
        when(hz.newTransactionContext(txOptions)).thenReturn(tx2);
        when(cluster.getShortId(owner)).thenReturn(ownerShort);
        when(crashedMembers.contains(owner)).thenReturn(false);

        assertFalse(barricade.isAllowed());

        barricade.await();

        verify(barricadeTx, never()).reset(barricadeKey, owner, ownerShort,
                tx2);

        assertFalse(barricade.isAllowed());
    }

    @Test
    public void testRelease() throws IllegalAccessException {
        TransactionContext tx = Mockito.mock(TransactionContext.class);
        String barricadeKey = "Foo";

        FieldUtils.writeDeclaredField(barricade, "barricadeKey", barricadeKey,
                true);

        when(hz.newTransactionContext(txOptions)).thenReturn(tx);
        barricade.release();

        verify(barricadeTx).release(barricadeKey, tx);
    }

    @Test
    public void testIsReleased() throws IllegalAccessException {
        String barricadeKey = "Foo";

        FieldUtils.writeDeclaredField(barricade, "barricadeKey", barricadeKey,
                true);

        when(objectMap.get(barricadeKey)).thenReturn(null)
                .thenReturn(State.INITIALIZE).thenReturn(State.READY);

        assertFalse(barricade.isReleased());
        assertFalse(barricade.isReleased());
        assertTrue(barricade.isReleased());
    }

    @Test
    public void testIsAllowed() {

        boolean actual = barricade.isAllowed();

        assertFalse(actual);
    }
}
