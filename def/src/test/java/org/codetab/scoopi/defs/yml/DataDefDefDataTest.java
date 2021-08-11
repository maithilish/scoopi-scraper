package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codetab.scoopi.model.DataDef;
import org.junit.Before;
import org.junit.Test;

public class DataDefDefDataTest {

    private DataDefDefData dataDefDefData;

    @Before
    public void setUp() throws Exception {
        dataDefDefData = new DataDefDefData();
    }

    @Test
    public void testGetDataDefMap() {
        HashMap<String, DataDef> dataDefMap = new HashMap<>();
        dataDefDefData.setDataDefMap(dataDefMap);
        Map<String, DataDef> actual = dataDefDefData.getDataDefMap();
        assertThat(actual).isEqualTo(dataDefMap);
    }

    @Test
    public void testGetDefinedDataDefs() {
        List<DataDef> definedDataDefs = new ArrayList<>();
        dataDefDefData.setDefinedDataDefs(definedDataDefs);
        List<DataDef> actual = dataDefDefData.getDefinedDataDefs();
        assertThat(actual).isEqualTo(definedDataDefs);
    }
}
