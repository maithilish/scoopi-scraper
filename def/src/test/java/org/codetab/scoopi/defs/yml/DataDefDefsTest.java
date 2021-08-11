package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.ObjectFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class DataDefDefsTest {

    private static ObjectMapper mapper;
    private static ObjectFactory of;

    @InjectMocks
    private DataDefDefs dataDefDefs;

    @Mock
    private Configs configs;
    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private Yamls yamls;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        mapper = new ObjectMapper();
        of = new ObjectFactory();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateDataDefs()
            throws JsonMappingException, JsonProcessingException {

        JsonNode defs = Mockito.mock(JsonNode.class);
        Entry<String, JsonNode> entry1 = createEntry("dataDef1", "1");

        Iterator<Entry<String, JsonNode>> entries =
                Lists.newArrayList(entry1).iterator();

        DataDef dataDef = createDataDef("dataDef1", ZonedDateTime.now(),
                ZonedDateTime.now(), "json1");

        when(defs.fields()).thenReturn(entries);
        when(yamls.toJson(entry1.getValue())).thenReturn(dataDef.getDefJson());
        when(configs.getRunDateTime()).thenReturn(dataDef.getFromDate());
        when(configs.getHighDate()).thenReturn(dataDef.getToDate());
        when(objectFactory.createDataDef(dataDef.getName(),
                dataDef.getFromDate(), dataDef.getToDate(),
                dataDef.getDefJson())).thenReturn(dataDef);

        List<DataDef> actual = dataDefDefs.createDataDefs(defs);

        assertThat(actual).hasSize(1);
        assertThat(actual.get(0)).isEqualTo(dataDef);
    }

    @Test
    public void testSetDefs() throws IOException, IllegalAccessException {
        String json1 = "{\"k1\":\"v1\"}";
        JsonNode jsonNode1 = mapper.readTree(json1);
        DataDef dataDef1 = createDataDef("dataDef1", ZonedDateTime.now(),
                ZonedDateTime.now(), json1);

        String json2 = "{\"k2\":\"v2\"}";
        JsonNode jsonNode2 = mapper.readTree(json2);
        DataDef dataDef2 = createDataDef("dataDef2", ZonedDateTime.now(),
                ZonedDateTime.now(), json2);

        ArrayList<DataDef> dataDefs = Lists.newArrayList(dataDef1, dataDef2);

        when(yamls.toJsonNode(json1)).thenReturn(jsonNode1);
        when(yamls.toJsonNode(json2)).thenReturn(jsonNode2);

        dataDefDefs.setDefs(dataDefs);

        JsonNode def1 =
                (JsonNode) FieldUtils.readDeclaredField(dataDef1, "def", true);
        JsonNode def2 =
                (JsonNode) FieldUtils.readDeclaredField(dataDef2, "def", true);
        assertThat(def1.toString()).isEqualTo(json1);
        assertThat(def2.toString()).isEqualTo(json2);
    }

    @Test
    public void testToMap() {
        DataDef dataDef1 = createDataDef("dataDef1", ZonedDateTime.now(),
                ZonedDateTime.now(), "json1");
        DataDef dataDef2 = createDataDef("dataDef2", ZonedDateTime.now(),
                ZonedDateTime.now(), "json2");
        ArrayList<DataDef> dataDefs = Lists.newArrayList(dataDef1, dataDef2);

        Map<String, DataDef> actual = dataDefDefs.toMap(dataDefs);

        assertThat(actual).hasSize(2);
        assertThat(actual.get("dataDef1")).isEqualTo(dataDef1);
        assertThat(actual.get("dataDef2")).isEqualTo(dataDef2);
    }

    @Test
    public void testTraceDataDef() {
        DataDef dataDef1 = createDataDef("dataDef1", ZonedDateTime.now(),
                ZonedDateTime.now(), "json1");
        DataDef dataDef2 = createDataDef("dataDef2", ZonedDateTime.now(),
                ZonedDateTime.now(), "json2");
        ArrayList<DataDef> dataDefs = Lists.newArrayList(dataDef1, dataDef2);

        dataDefDefs.traceDataDef(dataDefs);
    }

    private Entry<String, JsonNode> createEntry(final String key,
            final String value)
            throws JsonMappingException, JsonProcessingException {
        String json = String.format("{\"k%s\":\"v%s\"}", value, value);
        JsonNode defs = mapper.readTree(json);
        return new SimpleImmutableEntry<>(key, defs);
    }

    private DataDef createDataDef(final String name,
            final ZonedDateTime fromDate, final ZonedDateTime toDate,
            final String json) {
        return of.createDataDef(name, fromDate, toDate, json);
    }
}
