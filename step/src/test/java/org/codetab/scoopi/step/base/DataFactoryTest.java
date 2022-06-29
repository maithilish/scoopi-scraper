package org.codetab.scoopi.step.base;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;

import org.codetab.scoopi.defs.IDataDefDef;
import org.codetab.scoopi.defs.IItemDef;
import org.codetab.scoopi.model.Data;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class DataFactoryTest {
    @InjectMocks
    private DataFactory dataFactory;

    @Mock
    private IDataDefDef dataDefDef;
    @Mock
    private IItemDef itemDef;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateData() throws Exception {
        String dataDef = "Foo";
        Long documentId = Long.valueOf(1L);
        String label = "Bar";
        ZonedDateTime runDateTime = Mockito.mock(ZonedDateTime.class);
        Data data = Mockito.mock(Data.class);
        Long apple = Long.valueOf(1L);

        when(itemDef.getDataTemplate(dataDef)).thenReturn(data);
        when(dataDefDef.getDataDefId(dataDef)).thenReturn(apple);

        Data actual =
                dataFactory.createData(dataDef, documentId, label, runDateTime);

        assertSame(data, actual);
        verify(data).setName(label);
        verify(data).setDataDef(dataDef);
        verify(data).setDataDefId(apple);
        verify(data).setDocumentId(documentId);
        verify(data).setRunDate(runDateTime);
    }
}
