package org.codetab.scoopi;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.step.TaskMediator;
import org.codetab.scoopi.system.ErrorLogger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ScoopiEngineTest {

    @Mock
    private ScoopiSystem scoopiSystem;
    @Mock
    private ErrorLogger errorLogger;
    @Mock
    private TaskMediator taskMediator;

    @InjectMocks
    private ScoopiEngine scoopiEngine;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testStart() {

        String defaultConfigFile = "scoopi-default.xml";
        String userConfigFile = "scoopi-config.properties";

        given(scoopiSystem.getPropertyFileName()).willReturn(userConfigFile);

        // when
        scoopiEngine.start();

        // then
        InOrder inOrder = inOrder(scoopiSystem, taskMediator);
        inOrder.verify(scoopiSystem).startStats();
        inOrder.verify(scoopiSystem).startErrorLogger();
        inOrder.verify(scoopiSystem).addShutdownHook();
        inOrder.verify(scoopiSystem).getPropertyFileName();
        inOrder.verify(scoopiSystem).initConfigService(defaultConfigFile,
                userConfigFile);
        inOrder.verify(scoopiSystem).startMetricsServer();
        inOrder.verify(scoopiSystem).initDefs();
        inOrder.verify(scoopiSystem).seedLocatorGroups();
        inOrder.verify(scoopiSystem).waitForHeapDump();

        inOrder.verify(taskMediator).start();
        inOrder.verify(taskMediator).waitForFinish();
        inOrder.verify(scoopiSystem).waitForHeapDump();

        inOrder.verify(scoopiSystem).stopMetricsServer();
        inOrder.verify(scoopiSystem).stopStats();
        verifyNoMoreInteractions(scoopiSystem, taskMediator);
    }

    @Test
    public void testStartShouldCatchException() {
        CriticalException ex = new CriticalException("fatal");
        // given
        given(scoopiSystem.startStats()).willThrow(ex);

        scoopiEngine.start();

        // then
        InOrder inOrder = inOrder(scoopiSystem, errorLogger);
        inOrder.verify(scoopiSystem).startStats();
        inOrder.verify(errorLogger).log(eq(CAT.FATAL), any(String.class),
                eq(ex));
        inOrder.verify(scoopiSystem).stopMetricsServer();
        inOrder.verify(scoopiSystem).stopStats();
        verifyNoMoreInteractions(scoopiSystem, errorLogger);
    }
}
