package org.codetab.scoopi.misc;

import static org.mockito.Mockito.verify;

import org.codetab.scoopi.shared.StatService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * <p>
 * MemoryTask tests.
 * @author Maithilish
 *
 */
public class MemoryTaskTest {

    @Mock
    private StatService statService;

    @InjectMocks
    private MemoryTask memoryTask;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRun() throws InterruptedException {
        Thread t = new Thread(memoryTask);
        t.start();
        t.join();

        verify(statService).collectMemoryStat();
    }
}
