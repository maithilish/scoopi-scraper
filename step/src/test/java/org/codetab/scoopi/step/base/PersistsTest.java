package org.codetab.scoopi.step.base;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.JobInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class PersistsTest {
    @InjectMocks
    private Persists persists;

    @Mock
    private ITaskDef taskDef;
    @Mock
    private Configs configs;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testPersistDocumentUseDataSource() {
        when(configs.useDataStore()).thenReturn(true).thenReturn(false);
        when(configs.isPersist("scoopi.persist.locator")).thenReturn(true);

        assertTrue(persists.persistDocument());
        assertFalse(persists.persistDocument());
    }

    @Test
    public void testPersistDocumentIsPersist() {
        when(configs.useDataStore()).thenReturn(true);
        when(configs.isPersist("scoopi.persist.locator")).thenReturn(true)
                .thenReturn(false);

        assertTrue(persists.persistDocument());
        assertFalse(persists.persistDocument());
    }

    @Test
    public void testPersistData() throws Exception {
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String taskGroup = "Foo";
        String taskName = "Bar";
        String apple = "true";
        boolean kiwi = true;
        boolean banana = true;

        when(jobInfo.getGroup()).thenReturn(taskGroup);
        when(jobInfo.getTask()).thenReturn(taskName);
        when(taskDef.getFieldValue(taskGroup, taskName, "persist", "data"))
                .thenReturn(apple);
        when(configs.useDataStore()).thenReturn(kiwi);
        when(configs.isPersist("scoopi.persist.data")).thenReturn(banana);

        boolean actual = persists.persistData(jobInfo);

        assertTrue(actual);
    }

    @Test
    public void testPersistDataFalsePersistDataNull() throws Exception {
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String taskGroup = "Foo";
        String taskName = "Bar";
        boolean kiwi = true;
        boolean banana = true;

        when(jobInfo.getGroup()).thenReturn(taskGroup);
        when(jobInfo.getTask()).thenReturn(taskName);
        when(taskDef.getFieldValue(taskGroup, taskName, "persist", "data"))
                .thenReturn(null);
        when(configs.useDataStore()).thenReturn(kiwi);
        when(configs.isPersist("scoopi.persist.data")).thenReturn(banana);

        boolean actual = persists.persistData(jobInfo);

        assertTrue(actual);
    }

    @Test
    public void testPersistDataNoPersist() throws Exception {
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String taskGroup = "Foo";
        String taskName = "Bar";

        when(jobInfo.getGroup()).thenReturn(taskGroup);
        when(jobInfo.getTask()).thenReturn(taskName);
        when(taskDef.getFieldValue(taskGroup, taskName, "persist", "data"))
                .thenReturn("true");
        when(configs.useDataStore()).thenReturn(true).thenReturn(false)
                .thenReturn(true);
        when(configs.isPersist("scoopi.persist.data")).thenReturn(false)
                .thenReturn(true).thenReturn(true);

        assertFalse(persists.persistData(jobInfo));
        assertFalse(persists.persistData(jobInfo));
    }

    @Test
    public void testPersistDataException() throws Exception {
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String taskGroup = "Foo";
        String taskName = "Bar";

        when(jobInfo.getGroup()).thenReturn(taskGroup);
        when(jobInfo.getTask()).thenReturn(taskName);
        when(taskDef.getFieldValue(taskGroup, taskName, "persist", "data"))
                .thenThrow(DefNotFoundException.class)
                .thenThrow(IOException.class);
        when(configs.useDataStore()).thenReturn(true);
        when(configs.isPersist("scoopi.persist.data")).thenReturn(true);

        assertTrue(persists.persistData(jobInfo));
        assertTrue(persists.persistData(jobInfo));
    }
}
