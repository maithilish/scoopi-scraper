package org.codetab.scoopi.store.basic;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.store.IStore;

public class BasicStore implements IStore {

    private static final int QUEUE_SIZE = 32768;

    private BlockingQueue<Payload> payloads =
            new ArrayBlockingQueue<>(QUEUE_SIZE);

    @Override
    public Payload takePayload() throws InterruptedException {
        return payloads.take();
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
