package org.codetab.scoopi.store.cluster.ignite.dao;

import java.io.Serializable;

import org.apache.commons.lang3.SerializationUtils;

public class DaoHelper {

    public byte[] serialize(final Serializable obj) {
        return SerializationUtils.serialize(obj);
    }

    public <T> T deserialize(final byte[] bytes, final T clazz) {
        return SerializationUtils.deserialize(bytes);
    }
}
