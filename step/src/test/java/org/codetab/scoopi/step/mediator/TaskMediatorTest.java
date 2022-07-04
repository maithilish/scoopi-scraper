package org.codetab.scoopi.step.mediator;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.step.base.FetchThrottle;
import org.codetab.scoopi.store.IPayloadStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class TaskMediatorTest {
    @InjectMocks
    private TaskMediator taskMediator;

    @Mock
    private IPayloadStore payloadStore;
    @Mock
    private TaskRunner taskRunner;
    @Mock
    private StateFliper stateFliper;
    @Mock
    private FetchThrottle fetchThrottle;
    @Mock
    private Errors errors;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testStart() {
        taskMediator.start();

        verify(fetchThrottle).init();
        verify(taskRunner).start();
    }

    @Test
    public void testWaitForFinish() throws Exception {
        taskMediator.waitForFinish();

        verify(taskRunner).join();
        verify(stateFliper).setTMState(TMState.TERMINATED);
        verify(errors, never()).inc();
    }

    @Test
    public void testWaitForFinishInterrupted() throws Exception {

        doThrow(InterruptedException.class).when(taskRunner).join();

        taskMediator.waitForFinish();

        verify(stateFliper, never()).setTMState(TMState.TERMINATED);
        verify(errors, times(1)).inc();
    }

    @Test
    public void testPushPayload() throws Exception {
        Payload payload = Mockito.mock(Payload.class);
        TMState tmState = TMState.READY;

        when(stateFliper.getTMState()).thenReturn(tmState);

        boolean actual = taskMediator.pushPayload(payload);

        assertTrue(actual);
        verify(stateFliper).setTMState(TMState.READY);
        verify(payloadStore).putPayload(payload);
    }

    @Test
    public void testPushPayloadStateIsShutdown() throws Exception {
        Payload payload = Mockito.mock(Payload.class);
        TMState tmState = TMState.SHUTDOWN;

        when(stateFliper.getTMState()).thenReturn(tmState);

        assertThrows(IllegalStateException.class,
                () -> taskMediator.pushPayload(payload));

        verify(stateFliper, never()).setTMState(TMState.READY);
        verify(payloadStore, never()).putPayload(payload);
    }

    @Test
    public void testPushPayloadStateIsTerminated() throws Exception {
        Payload payload = Mockito.mock(Payload.class);
        TMState tmState = TMState.TERMINATED;

        when(stateFliper.getTMState()).thenReturn(tmState);

        assertThrows(IllegalStateException.class,
                () -> taskMediator.pushPayload(payload));

        verify(stateFliper, never()).setTMState(TMState.READY);
        verify(payloadStore, never()).putPayload(payload);
    }

}
