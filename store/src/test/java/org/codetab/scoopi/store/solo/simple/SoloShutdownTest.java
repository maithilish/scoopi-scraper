package org.codetab.scoopi.store.solo.simple;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.codetab.scoopi.store.IJobStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SoloShutdownTest {
    @InjectMocks
    private SoloShutdown soloShutdown;

    @Mock
    private IJobStore jobStore;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInit() {
        soloShutdown.init();
    }

    @Test
    public void testSetDone() {
        soloShutdown.setDone();
    }

    @Test
    public void testSetTerminate() {
        soloShutdown.setTerminate();
    }

    @Test
    public void testTerminate() {
        soloShutdown.terminate();
    }

    @Test
    public void testCancel() {
        soloShutdown.cancel();
    }

    @Test
    public void testIsCancelled() {

        soloShutdown.cancel();

        boolean actual = soloShutdown.isCancelled();

        assertTrue(actual);
    }

    @Test
    public void testAllNodesDone() {

        boolean actual = soloShutdown.allNodesDone();

        assertTrue(actual);
    }

    @Test
    public void testJobStoreDone() {
        boolean apple = true;

        when(jobStore.isDone()).thenReturn(apple);

        boolean actual = soloShutdown.jobStoreDone();

        assertTrue(actual);
    }
}
