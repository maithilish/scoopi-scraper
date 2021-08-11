package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.Fingerprint;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.helper.Fingerprints;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DataDefDefTest {

    @InjectMocks
    private DataDefDef dataDefDef;

    @Mock
    private DataDefDefData data;

    private ObjectFactory of;

    @Mock
    private Map<String, DataDef> dataDefMap;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        of = new ObjectFactory();
    }

    @Test
    public void testGetDataDefId() throws DataDefNotFoundException {
        String name = "foo";
        DataDef dataDef = of.createDataDef(name);
        dataDef.setId(10L);

        when(data.getDataDefMap()).thenReturn(dataDefMap);
        when(dataDefMap.get(name)).thenReturn(dataDef);

        Long actual = dataDefDef.getDataDefId(name);

        assertThat(actual).isEqualTo(10L);
    }

    @Test
    public void testGetDataDefIdException() throws DataDefNotFoundException {
        String name = "foo";
        DataDef dataDef = of.createDataDef(name);
        dataDef.setId(10L);

        when(data.getDataDefMap()).thenReturn(dataDefMap);
        when(dataDefMap.get(name)).thenReturn(null);

        assertThrows(DataDefNotFoundException.class,
                () -> dataDefDef.getDataDefId(name));
    }

    @Test
    public void testGetDefinedDataDefs() {
        List<DataDef> dataDefs = new ArrayList<>();
        when(data.getDefinedDataDefs()).thenReturn(dataDefs);

        List<DataDef> actual = dataDefDef.getDefinedDataDefs();

        assertThat(actual).isSameAs(dataDefs);
    }

    @Test
    public void testGetFingerprint() {
        String name = "foo";
        String defJson = "foo:bar";
        DataDef dataDef = of.createDataDef(name);
        dataDef.setDefJson(defJson);
        Fingerprint fingerprint = Fingerprints.fingerprint(defJson.getBytes());

        when(data.getDataDefMap()).thenReturn(dataDefMap);
        when(dataDefMap.get(name)).thenReturn(dataDef);

        Fingerprint actual = dataDefDef.getFingerprint(name);

        assertThat(actual).isEqualTo(fingerprint);
    }

}
