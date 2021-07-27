package org.codetab.scoopi.dao.fs;

import java.io.Serializable;

import org.apache.commons.lang3.SerializationUtils;

public class Serializer {

    public <T> T deserialize(final byte[] data) {
        return SerializationUtils.deserialize(data);
    }

    public byte[] serialize(final Serializable obj) {
        return SerializationUtils.serialize(obj);
    }
}
