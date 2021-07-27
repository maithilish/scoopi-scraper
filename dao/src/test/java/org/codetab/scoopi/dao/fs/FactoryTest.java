package org.codetab.scoopi.dao.fs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.Test;

public class FactoryTest {

    @Test
    public void testCreateDataMap() {
        Factory factory = new Factory();
        Map<String, byte[]> map1 = factory.createDataMap();
        Map<String, byte[]> map2 = factory.createDataMap();
        assertThat(map1).isEmpty();
        assertThat(map2).isEmpty();
        assertThat(map1).isNotSameAs(map2);
    }

}
