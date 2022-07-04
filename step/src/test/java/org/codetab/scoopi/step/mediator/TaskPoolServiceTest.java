package org.codetab.scoopi.step.mediator;

import static org.junit.Assert.assertTrue;

import org.codetab.scoopi.pool.PoolService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class TaskPoolServiceTest {
    @InjectMocks
    private TaskPoolService taskPoolService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void isPoolService() {
        assertTrue(taskPoolService instanceof PoolService);
    }
}
