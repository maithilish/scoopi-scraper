package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codetab.scoopi.defs.IDefData;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.Query;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class ItemDefBuilderTest {

    @InjectMocks
    private ItemDefBuilder builder;

    @Mock
    private ItemDefs itemDefs;
    @Mock
    private Factory factory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testBuildData() throws DefNotFoundException {

        JsonNode node = new TextNode("foo"); // defs
        ItemDefData itemDefData = Mockito.mock(ItemDefData.class);

        Map<String, Data> dataTemplates = new HashMap<>();
        Map<String, JsonNode> itemNodeMap = new HashMap<>();

        Map<String, Query> queryMap = new HashMap<>();
        Map<String, List<Axis>> factAxisMap = new HashMap<>();
        Map<String, List<Axis>> itemAxisMap = new HashMap<>();
        Map<String, List<Axis>> dimAxisMap = new HashMap<>();

        when(factory.createItemDefData()).thenReturn(itemDefData);
        when(itemDefs.getQueryMap(node)).thenReturn(queryMap);
        when(itemDefs.getItemAxisMap(node)).thenReturn(itemAxisMap);
        when(itemDefs.getDimAxisMap(node)).thenReturn(dimAxisMap);
        when(itemDefs.getFactAxisMap(node)).thenReturn(factAxisMap);
        when(itemDefs.generateDataTemplates(itemDefData.getItemAxisMap(),
                itemDefData.getDimAxisMap(), itemDefData.getFactAxisMap()))
                        .thenReturn(dataTemplates);
        when(itemDefs.getItemNodeMap(node)).thenReturn(itemNodeMap);

        IDefData actual = builder.buildData(node);

        assertThat(actual).isSameAs(itemDefData);

        verify(itemDefData).setQueryMap(queryMap);
        verify(itemDefData).setItemAxisMap(itemAxisMap);
        verify(itemDefData).setDimAxisMap(dimAxisMap);
        verify(itemDefData).setFactAxisMap(factAxisMap);
        verify(itemDefData).setItemAttributeMap(
                itemDefs.getItemAttributeMap(node, itemNodeMap));
    }

}
