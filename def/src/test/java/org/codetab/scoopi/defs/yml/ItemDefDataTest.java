package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.Query;
import org.junit.Before;
import org.junit.Test;

public class ItemDefDataTest {

    private Map<String, List<Axis>> itemAxisMap = new HashMap<>();
    private Map<String, List<Axis>> dimAxisMap = new HashMap<>();;
    private Map<String, List<Axis>> factAxisMap = new HashMap<>();;
    private Map<String, Query> queryMap = new HashMap<>();;
    private Map<String, ItemAttribute> itemAttributeMap = new HashMap<>();;
    private Map<String, Data> dataTemplateMap = new HashMap<>();

    private ItemDefData itemDefData;

    @Before
    public void setUp() throws Exception {
        itemDefData = new ItemDefData();
    }

    @Test
    public void testGetItemAxisMap() {
        itemDefData.setItemAxisMap(itemAxisMap);
        assertThat(itemDefData.getItemAxisMap()).isSameAs(itemAxisMap);
    }

    @Test
    public void testGetDimAxisMap() {
        itemDefData.setDimAxisMap(dimAxisMap);
        assertThat(itemDefData.getDimAxisMap()).isSameAs(dimAxisMap);
    }

    @Test
    public void testGetFactAxisMap() {
        itemDefData.setFactAxisMap(factAxisMap);
        assertThat(itemDefData.getFactAxisMap()).isSameAs(factAxisMap);
    }

    @Test
    public void testGetQueryMap() {
        itemDefData.setQueryMap(queryMap);
        assertThat(itemDefData.getQueryMap()).isSameAs(queryMap);
    }

    @Test
    public void testGetItemAttributeMap() {
        itemDefData.setItemAttributeMap(itemAttributeMap);
        assertThat(itemDefData.getItemAttributeMap())
                .isSameAs(itemAttributeMap);
    }

    @Test
    public void testGetDataTemplateMap() {
        itemDefData.setDataTemplateMap(dataTemplateMap);
        assertThat(itemDefData.getDataTemplateMap()).isSameAs(dataTemplateMap);
    }

}
