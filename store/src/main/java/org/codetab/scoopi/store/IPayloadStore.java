package org.codetab.scoopi.store;

import org.codetab.scoopi.model.Payload;

public interface IPayloadStore {

    void putPayload(Payload payload) throws InterruptedException;

    Payload takePayload() throws InterruptedException;

    int getPayloadsCount();

    void clear();

}
