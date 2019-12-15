package org.codetab.scoopi.store.cluster.ignite;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.store.cluster.IClusterJobStore;

public class IgniteJobStore implements IClusterJobStore {

    private static final int QUEUE_SIZE = 32768;

    private BlockingQueue<Payload> payloads =
            new ArrayBlockingQueue<>(QUEUE_SIZE);

    @Override
    public Payload takePayload() throws InterruptedException {
        System.out.println("take job from ignite cluster");
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
