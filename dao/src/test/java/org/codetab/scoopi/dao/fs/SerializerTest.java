package org.codetab.scoopi.dao.fs;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;

import org.junit.Test;

public class SerializerTest {

    @Test
    public void testDeserialize() {
        ZonedDateTime date = ZonedDateTime.now();
        Serializer serializer = new Serializer();
        byte[] data = serializer.serialize(date);
        ZonedDateTime actual = serializer.deserialize(data);
        assertThat(actual).isEqualTo(date);
    }
}
