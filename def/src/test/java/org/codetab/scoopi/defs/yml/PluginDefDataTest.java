package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codetab.scoopi.model.Plugin;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class PluginDefDataTest {

    @InjectMocks
    private PluginDefData pluginDefData;

    @Before()
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSetPluginMap() {
        Map<String, List<Plugin>> pluginMap = new HashMap<>();
        pluginDefData.setPluginMap(pluginMap);

        Map<String, List<Plugin>> actual = pluginDefData.getPluginMap();

        assertThat(actual).isSameAs(pluginMap);
    }
}
