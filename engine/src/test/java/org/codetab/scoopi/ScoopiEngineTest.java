package org.codetab.scoopi;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.codetab.scoopi.engine.ScoopiEngine;
import org.codetab.scoopi.engine.SystemModule;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.log.Log.CAT;
import org.codetab.scoopi.step.JobMediator;
import org.codetab.scoopi.step.TaskMediator;
import org.codetab.scoopi.step.extract.JobSeeder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ScoopiEngineTest {

    @Mock
    private SystemModule systemModule;
    @Mock
    private ErrorLogger errorLogger;
    @Mock
    private TaskMediator taskMediator;
    @Mock
    private JobMediator jobMediator;
    @Mock
    private JobSeeder jobSeeder;

    @InjectMocks
    private ScoopiEngine scoopiEngine;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testStart() {

        // when
        scoopiEngine.initSystem();

        // then
        InOrder inOrder =
                inOrder(systemModule, taskMediator, jobMediator, jobSeeder);
        inOrder.verify(systemModule).startStats();
        inOrder.verify(systemModule).startErrorLogger();
        inOrder.verify(systemModule).addShutdownHook();
        inOrder.verify(systemModule).startMetrics();
        inOrder.verify(jobSeeder).seedLocatorGroups();
        inOrder.verify(systemModule).waitForInput();

        inOrder.verify(taskMediator).start();
        inOrder.verify(jobMediator).start();
        inOrder.verify(taskMediator).waitForFinish();
        inOrder.verify(jobMediator).waitForFinish();
        inOrder.verify(systemModule).waitForFinish();
        inOrder.verify(systemModule).waitForInput();

        inOrder.verify(systemModule).stopMetrics();
        inOrder.verify(systemModule).stopStats();
        verifyNoMoreInteractions(systemModule, taskMediator, jobMediator,
                jobSeeder);
    }

    @Test
    public void testStartShouldCatchException() {
        CriticalException ex = new CriticalException("fatal");
        // given
        given(systemModule.startStats()).willThrow(ex);

        scoopiEngine.initSystem();

        // then
        InOrder inOrder = inOrder(systemModule, errorLogger);
        inOrder.verify(systemModule).startStats();
        inOrder.verify(errorLogger).log(eq(CAT.FATAL), any(String.class),
                eq(ex));
        inOrder.verify(systemModule).stopMetrics();
        inOrder.verify(systemModule).stopStats();
        verifyNoMoreInteractions(systemModule, errorLogger);
    }
}
