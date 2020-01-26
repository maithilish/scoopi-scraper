package org.codetab.scoopi.store.solo.simple;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.store.IPayloadStore;

@Singleton
public class PayloadStore implements IPayloadStore {

    private static final int QUEUE_SIZE = 32768;

    private BlockingQueue<Payload> payloads =
            new ArrayBlockingQueue<>(QUEUE_SIZE);

    @Override
    public Payload takePayload(final int timeout) throws InterruptedException {
        if (timeout == 0) {
            return payloads.take();
        } else {
            return payloads.poll(timeout, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void putPayload(final Payload payload) throws InterruptedException {
        notNull(payload, "payload must not be null");
        payloads.put(payload);
    }

    @Override
    public int getPayloadsCount() {
        return payloads.size();
    }

    @Override
    public void clear() {
        payloads.clear();
    }
}
