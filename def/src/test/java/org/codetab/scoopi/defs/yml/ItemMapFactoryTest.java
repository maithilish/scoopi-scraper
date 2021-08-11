package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.ObjectFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

public class ItemMapFactoryTest {

    @InjectMocks
    private ItemMapFactory itemMapFactory;

    @Mock
    private Jacksons jacksons;
    @Mock
    private ObjectFactory objectFactory;

    private ObjectFactory of;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        of = new ObjectFactory();
    }

    @Test
    public void testGetItemMap() {
        JsonNode defs = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        String type = "items";
        String axisName = "item";
        String dataDefName = "foo";
        String path = "tpath";
        String indexStr = "5";
        String orderStr = "2";
        Integer index = Integer.valueOf(indexStr);
        Integer order = Integer.valueOf(orderStr);
        String itemName = null;
        String match = null;
        String value = null;

        Iterator<String> dataDefNames =
                Lists.newArrayList(dataDefName).iterator();

        JsonNode jItem = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        List<JsonNode> jItems = new ArrayList<>();
        jItems.add(jItem);

        Axis axis =
                of.createAxis(axisName, itemName, value, match, index, order);

        when(defs.fieldNames()).thenReturn(dataDefNames);
        when(jacksons.path(dataDefName, type)).thenReturn(path);
        when(defs.at(path).findValues("item")).thenReturn(jItems);
        when(jItem.path("index").asText()).thenReturn(indexStr);
        when(jItem.path("order").asText()).thenReturn(orderStr);
        when(objectFactory.createAxis(axisName, null, null, null,
                Integer.valueOf(5), Integer.valueOf(2))).thenReturn(axis);

        Map<String, List<Axis>> actual =
                itemMapFactory.getItemMap(defs, type, axisName);

        assertThat(actual).hasSize(1);
        assertThat(actual.get(dataDefName)).hasSize(1);
        Axis aAxis = actual.get(dataDefName).get(0);

        assertThat(aAxis).isSameAs(axis);
    }

    @Test
    public void testGetItemMapBlanks() {
        JsonNode defs = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        String type = "items";
        String axisName = "item";
        String dataDefName = "foo";
        String path = "tpath";
        String indexStr = ""; // blank
        String orderStr = ""; // blank
        String itemName = null;
        String match = null;
        String value = null;

        Iterator<String> dataDefNames =
                Lists.newArrayList(dataDefName).iterator();

        JsonNode jItem = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        List<JsonNode> jItems = new ArrayList<>();
        jItems.add(jItem);

        Axis axis = of.createAxis(axisName, itemName, value, match, null, null);

        when(defs.fieldNames()).thenReturn(dataDefNames);
        when(jacksons.path(dataDefName, type)).thenReturn(path);
        when(defs.at(path).findValues("item")).thenReturn(jItems);
        when(jItem.path("index").asText()).thenReturn(indexStr);
        when(jItem.path("order").asText()).thenReturn(orderStr);
        when(objectFactory.createAxis(axisName, null, null, null, null, null))
                .thenReturn(axis);

        Map<String, List<Axis>> actual =
                itemMapFactory.getItemMap(defs, type, axisName);

        assertThat(actual).hasSize(1);
        assertThat(actual.get(dataDefName)).hasSize(1);
        Axis aAxis = actual.get(dataDefName).get(0);

        assertThat(aAxis).isSameAs(axis);
    }
}
