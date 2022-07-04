package org.codetab.scoopi.step.mediator;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ScheduledExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class FactoryTest {
    @InjectMocks
    private Factory factory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testNewSingleThreadScheduledExecutor() {
        ScheduledExecutorService actual =
                factory.newSingleThreadScheduledExecutor();

        assertEquals(
                "java.util.concurrent.Executors$DelegatedScheduledExecutorService",
                actual.getClass().getName());
    }
}
