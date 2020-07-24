package org.codetab.scoopi.store;

import org.codetab.scoopi.model.Payload;

public interface IPayloadStore {

    void putPayload(Payload payload) throws InterruptedException;

    Payload takePayload(int timeout) throws InterruptedException;

    int getPayloadsCount();

    boolean isDone();

    void clear();
}
