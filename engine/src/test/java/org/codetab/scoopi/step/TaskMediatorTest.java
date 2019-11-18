package org.codetab.scoopi.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.step.TaskMediator.TaskRunnerThread;
import org.codetab.scoopi.step.pool.TaskPoolService;
import org.codetab.scoopi.store.IStore;
import org.codetab.scoopi.system.ErrorLogger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TaskMediatorTest {

    @Mock
    private TaskPoolService poolService;
    @Mock
    private IStore store;
    @Mock
    private TaskFactory taskFactory;
    @Mock
    private ErrorLogger errorLogger;
    @Mock
    private TaskRunnerThread taskRunner;
    @Mock
    private AtomicInteger jobIdCounter;

    @InjectMocks
    private TaskMediator taskMediator;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testStart() {
        taskMediator.start();
        verify(taskRunner).start();
    }

    @Test
    public void testWaitForFinish() throws InterruptedException {
        taskMediator.waitForFinish();
        verify(taskRunner).join();
        verifyNoInteractions(errorLogger);
    }

    @Test
    public void testWaitForFinishThrowsException() throws InterruptedException {
        doThrow(InterruptedException.class).when(taskRunner).join();
        taskMediator.waitForFinish();
        verify(errorLogger).log(eq(CAT.INTERNAL), any(String.class),
                any(InterruptedException.class));
    }

    @Test
    public void testPushPayload()
            throws InterruptedException, IllegalAccessException {
        ObjectFactory mf = new ObjectFactory();
        Payload payload = mf.createPayload(null, null, null);

        boolean actual = taskMediator.pushPayload(payload);
        int reservations = (int) FieldUtils.readDeclaredField(taskMediator,
                "reservations", true);

        assertThat(actual).isTrue();
        assertThat(reservations).isEqualTo(1);
        verify(store).putPayload(payload);
    }

    @Test
    public void testGetJobId() {
        taskMediator.getJobId();
        verify(jobIdCounter).incrementAndGet();
        verifyNoMoreInteractions(jobIdCounter);
    }

}
