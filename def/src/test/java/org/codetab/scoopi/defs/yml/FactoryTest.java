package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class FactoryTest {

    @InjectMocks
    private Factory factory;

    @Before()
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateDataDefDefData() {
        DataDefDefData actual = factory.createDataDefDefData();
        DataDefDefData another = factory.createDataDefDefData();
        assertThat(actual).isInstanceOf(DataDefDefData.class);
        assertThat(actual).isNotSameAs(another);
    }

    @Test
    public void testCreateItemDefData() {
        ItemDefData actual = factory.createItemDefData();
        ItemDefData another = factory.createItemDefData();
        assertThat(actual).isInstanceOf(ItemDefData.class);
        assertThat(actual).isNotSameAs(another);
    }
}
