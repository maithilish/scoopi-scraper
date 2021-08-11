package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.codetab.scoopi.defs.IDefData;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.DataDef;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DataDefDefBuilderTest {

    @InjectMocks
    private DataDefDefBuilder builder;

    @Mock
    private DataDefDefs dataDefDefs;
    @Mock
    private Factory factory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testBuildData() throws JsonMappingException,
            JsonProcessingException, DefNotFoundException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode defs = mapper.readTree("{\"k1\":\"v1\"}");

        DataDefDefData dataDefDefData = new DataDefDefData();
        List<DataDef> dataDefs = new ArrayList<>();
        HashMap<String, DataDef> dataDefMap = new HashMap<>();

        when(factory.createDataDefDefData()).thenReturn(dataDefDefData);
        when(dataDefDefs.createDataDefs(defs)).thenReturn(dataDefs);
        when(dataDefDefs.toMap(dataDefs)).thenReturn(dataDefMap);

        IDefData actual = builder.buildData(defs);

        assertThat(actual).isSameAs(dataDefDefData);

        assertThat(dataDefDefData.getDataDefMap()).isSameAs(dataDefMap);
        assertThat(dataDefDefData.getDefinedDataDefs()).isSameAs(dataDefs);
    }

    @Test
    public void testBuildDataException() throws JsonMappingException,
            JsonProcessingException, DefNotFoundException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode defs = mapper.readTree("{\"k1\":\"v1\"}");

        DataDefDefData dataDefDefData = new DataDefDefData();
        List<DataDef> dataDefs = new ArrayList<>();
        HashMap<String, DataDef> dataDefMap = new HashMap<>();

        when(factory.createDataDefDefData()).thenReturn(dataDefDefData);
        when(dataDefDefs.createDataDefs(defs))
                .thenThrow(JsonProcessingException.class);
        when(dataDefDefs.toMap(dataDefs)).thenReturn(dataDefMap);

        assertThrows(CriticalException.class, () -> builder.buildData(defs));
    }

}
