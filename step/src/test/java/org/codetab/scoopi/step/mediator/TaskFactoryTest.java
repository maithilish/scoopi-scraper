package org.codetab.scoopi.step.mediator;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.step.IStep;
import org.codetab.scoopi.step.Step;
import org.codetab.scoopi.step.Task;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class TaskFactoryTest {
    @InjectMocks
    private TaskFactory taskFactory;

    @Mock
    private DInjector dInjector;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateTask() throws Exception {
        Payload payload = Mockito.mock(Payload.class);
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String apple = "org.codetab.scoopi.step.Step";
        Class<Step> stepClass = Step.class;
        Step obj = Mockito.mock(Step.class);
        IStep step = obj;
        Task task = Mockito.mock(Task.class);

        when(payload.getStepInfo()).thenReturn(stepInfo);
        when(stepInfo.getClassName()).thenReturn(apple);
        when(dInjector.instance(stepClass)).thenReturn(obj);
        when(dInjector.instance(Task.class)).thenReturn(task);

        Task actual = taskFactory.createTask(payload);

        assertSame(task, actual);
        verify(step).setPayload(payload);
        verify(task).setStep(step);
    }

    @Test
    public void testCreateTaskIStep() {
        IStep step = Mockito.mock(IStep.class);
        Task task = Mockito.mock(Task.class);

        when(dInjector.instance(Task.class)).thenReturn(task);

        Task actual = taskFactory.createTask(step);

        assertSame(task, actual);
        verify(task).setStep(step);
    }

    @Test
    public void testCreateStep() throws Exception {
        String clzName = "org.codetab.scoopi.step.Step";
        @SuppressWarnings({"unchecked", "rawtypes"})
        Class<IStep> stepClass = (Class) Class.forName(clzName);
        IStep obj = Mockito.mock(IStep.class);
        IStep step = obj;

        when(dInjector.instance(stepClass)).thenReturn(obj);

        IStep actual = taskFactory.createStep(clzName);

        assertSame(step, actual);
    }

    @Test
    public void testCreateStepClassCastException() throws Exception {
        String clzName = "org.codetab.scoopi.step.Task";
        @SuppressWarnings({"unchecked", "rawtypes"})
        Class<Task> stepClass = (Class) Class.forName(clzName);
        Task obj = Mockito.mock(Task.class);

        when(dInjector.instance(stepClass)).thenReturn(obj);

        assertThrows(ClassCastException.class,
                () -> taskFactory.createStep(clzName));
    }

}
