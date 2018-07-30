package org.codetab.scoopi.step;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.pool.TaskPoolService;
import org.codetab.scoopi.shared.StatService;
import org.codetab.scoopi.shared.StepService;
import org.codetab.scoopi.step.TaskMediator.TaskRunnerThread;
import org.codetab.scoopi.store.IStore;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class TaskRunnerTest {

    @Mock
    private TaskPoolService poolService;
    @Mock
    private IStore store;
    @Mock
    private StepService stepService;
    @Mock
    private StatService statService;
    @Mock
    private AtomicInteger jobIdCounter;

    @InjectMocks
    private TaskMediator taskMediator;

    // can't spy the inner class
    private TaskRunnerThread taskRunner;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        // can't create spy of inner class
        // so create an instance and inject it to the field
        taskRunner = taskMediator.new TaskRunnerThread();
        FieldUtils.writeDeclaredField(taskMediator, "taskRunner", taskRunner,
                true);
    }

    @Test
    public void testRunPayloadInQNoTaskInPool()
            throws IllegalAccessException, InterruptedException,
            ClassNotFoundException, InstantiationException {

        Payload payload = Mockito.mock(Payload.class);
        Task task = Mockito.mock(Task.class);
        Step step = Mockito.mock(Step.class);
        String poolName = "test pool";

        FieldUtils.writeDeclaredField(taskMediator, "reservations", 1, true);
        given(poolService.isDone()).willReturn(true);

        given(store.takePayload()).willReturn(payload);
        given(stepService.createTask(payload)).willReturn(task);
        given(task.getStep()).willReturn(step);
        given(step.getStepName()).willReturn(poolName);

        taskRunner.run();
        verify(poolService).waitForFinish();
        verify(poolService).submit(poolName, task);
    }

    @Test
    public void testRunPayloadInQTaskInPool()
            throws IllegalAccessException, InterruptedException,
            ClassNotFoundException, InstantiationException {

        Payload payload = Mockito.mock(Payload.class);
        Task task = Mockito.mock(Task.class);
        Step step = Mockito.mock(Step.class);
        String poolName = "test pool";

        FieldUtils.writeDeclaredField(taskMediator, "reservations", 1, true);
        given(poolService.isDone()).willReturn(false).willReturn(true);

        given(store.takePayload()).willReturn(payload);
        given(stepService.createTask(payload)).willReturn(task);
        given(task.getStep()).willReturn(step);
        given(step.getStepName()).willReturn(poolName);

        taskRunner.run();
        verify(poolService).waitForFinish();
        verify(poolService).submit(poolName, task);
    }

    @Test
    public void testRunNoPayloadInQ() throws IllegalAccessException {
        FieldUtils.writeDeclaredField(taskMediator, "reservations", 0, true);
        given(poolService.isDone()).willReturn(true);
        taskRunner.run();
        verify(poolService).waitForFinish();
    }

    @Test
    public void testRunNoPayloadInQButTaskInPool()
            throws IllegalAccessException, InterruptedException,
            ClassNotFoundException, InstantiationException {

        FieldUtils.writeDeclaredField(taskMediator, "reservations", 0, true);
        given(poolService.isDone()).willReturn(false).willReturn(true);

        taskRunner.run();
        verify(poolService).waitForFinish();
    }

    @Test
    public void testRunThrowsException()
            throws IllegalAccessException, InterruptedException,
            ClassNotFoundException, InstantiationException {

        Payload payload = Mockito.mock(Payload.class);

        given(store.takePayload()).willReturn(payload);
        given(stepService.createTask(payload))
                .willThrow(ClassNotFoundException.class)
                .willThrow(InstantiationException.class)
                .willThrow(IllegalAccessException.class);

        FieldUtils.writeDeclaredField(taskMediator, "reservations", 1, true);
        given(poolService.isDone()).willReturn(false).willReturn(true);
        taskRunner.run();
        verify(statService).log(eq(CAT.ERROR), any(String.class),
                any(ClassNotFoundException.class));

        FieldUtils.writeDeclaredField(taskMediator, "reservations", 1, true);
        given(poolService.isDone()).willReturn(false).willReturn(true);
        taskRunner.run();
        verify(statService).log(eq(CAT.ERROR), any(String.class),
                any(InstantiationException.class));

        FieldUtils.writeDeclaredField(taskMediator, "reservations", 1, true);
        given(poolService.isDone()).willReturn(false).willReturn(true);
        taskRunner.run();
        verify(statService).log(eq(CAT.ERROR), any(String.class),
                any(IllegalAccessException.class));
    }

    @Test
    public void testRunThrowsInterruptedException()
            throws IllegalAccessException, InterruptedException,
            ClassNotFoundException, InstantiationException {

        Payload payload = Mockito.mock(Payload.class);
        Task task = Mockito.mock(Task.class);
        Step step = Mockito.mock(Step.class);
        String poolName = "test pool";

        FieldUtils.writeDeclaredField(taskMediator, "reservations", 1, true);
        given(poolService.isDone()).willReturn(false).willReturn(true);

        given(store.takePayload()).willThrow(InterruptedException.class)
                .willReturn(payload);
        given(stepService.createTask(payload)).willReturn(task);
        given(task.getStep()).willReturn(step);
        given(step.getStepName()).willReturn(poolName);

        taskRunner.run();
        verify(statService).log(eq(CAT.ERROR), any(String.class),
                any(InterruptedException.class));
        verify(poolService).submit(poolName, task);
    }
}
