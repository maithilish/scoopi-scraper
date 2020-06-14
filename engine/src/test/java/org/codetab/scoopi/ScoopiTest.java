package org.codetab.scoopi;

import static org.mockito.Mockito.verify;

import org.codetab.scoopi.engine.ScoopiEngine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ScoopiTest {

    @Mock
    private ScoopiEngine engine;

    @InjectMocks
    private Scoopi scoopi;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testStart() {

        scoopi.start();

        verify(engine).initSystem();
        verify(engine).runJobs();
        verify(engine).shutdown();
    }
}
