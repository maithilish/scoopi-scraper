package org.codetab.scoopi.store.solo.simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class PayloadStoreTest {
    @InjectMocks
    private PayloadStore payloadStore;

    @Mock
    private BlockingQueue<Payload> payloads;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test queue initialization without mockito.
     * @throws Exception
     */
    @Test
    public void testQueueInit() throws Exception {
        ObjectFactory factory = new ObjectFactory();
        Payload payload = factory.createPayload(null, null, null);

        PayloadStore pStore = new PayloadStore();

        assertEquals(0, pStore.getPayloadsCount());

        pStore.putPayload(payload);
        Payload actual = pStore.takePayload(0);

        assertSame(actual, payload);
    }

    @Test
    public void testTakePayload() throws Exception {
        Payload grape = Mockito.mock(Payload.class);
        Payload orange = Mockito.mock(Payload.class);

        when(payloads.take()).thenReturn(grape);

        int timeout = 1;
        when(payloads.poll(timeout, TimeUnit.MILLISECONDS)).thenReturn(orange);

        Payload actual = payloadStore.takePayload(timeout);
        assertSame(orange, actual);

        timeout = 0;
        actual = payloadStore.takePayload(timeout);
        assertSame(grape, actual);

    }

    @Test
    public void testPutPayload() throws Exception {
        Payload payload = Mockito.mock(Payload.class);
        payloadStore.putPayload(payload);
    }

    @Test
    public void testGetPayloadsCount() {
        int apple = 1;

        when(payloads.size()).thenReturn(apple);

        int actual = payloadStore.getPayloadsCount();

        assertEquals(apple, actual);
    }

    @Test
    public void testClear() {
        payloadStore.clear();
    }

    @Test
    public void testIsDone() {
        int done = 0;
        int notDone = 1;
        when(payloads.size()).thenReturn(notDone).thenReturn(done);

        assertFalse(payloadStore.isDone());
        assertTrue(payloadStore.isDone());
    }
}
