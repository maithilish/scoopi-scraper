package org.codetab.scoopi.store.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.concurrent.BlockingQueue;

import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.store.solo.simple.PayloadStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class BasicStoreTest {

    @Mock
    private BlockingQueue<Payload> payloads;

    @InjectMocks
    private PayloadStore store;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testTakePayload() throws InterruptedException {
        store.takePayload(0);

        verify(payloads).take();
        verifyNoMoreInteractions(payloads);
    }

    @Test
    public void testPutPayload() throws InterruptedException {
        Payload payload = Mockito.mock(Payload.class);

        store.putPayload(payload);

        verify(payloads).put(payload);
        verifyNoMoreInteractions(payloads);
    }

    @Test
    public void testPutPayloadNullParams() throws InterruptedException {
        try {
            store.putPayload(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("payload must not be null");
        }
    }

    @Test
    public void testGetPayloadsCount() {
        given(payloads.size()).willReturn(2);

        assertThat(store.getPayloadsCount()).isEqualTo(2);
    }

}
