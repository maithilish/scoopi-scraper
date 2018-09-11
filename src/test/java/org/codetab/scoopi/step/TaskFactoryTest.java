package org.codetab.scoopi.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.step.extract.URLLoader;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class TaskFactoryTest {

    @Mock
    private DInjector dInjector;
    @InjectMocks
    private TaskFactory taskFactory;

    @Rule
    public ExpectedException testRule = ExpectedException.none();
    private String clzName;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        clzName = "org.codetab.scoopi.step.extract.URLLoader";
    }

    @Test
    public void testCreateTaskFromPayload() throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        ObjectFactory mf = new ObjectFactory();
        JobInfo jobInfo =
                mf.createJobInfo(0, "locator", "group", "task", "dataDef");
        StepInfo stepInfo = mf.createStepInfo("s1", "s0", "s2", clzName);
        String data = "data";
        Payload payload = mf.createPayload(jobInfo, stepInfo, data);

        URLLoader step = Mockito.mock(URLLoader.class);
        Task task = Mockito.mock(Task.class);

        given(dInjector.instance(URLLoader.class)).willReturn(step);
        given(dInjector.instance(Task.class)).willReturn(task);

        Task actual = taskFactory.createTask(payload);

        verify(step).setPayload(payload);
        verify(task).setStep(step);
        assertThat(actual).isSameAs(task);
    }

    @Test
    public void testCreateTaskFromStep() {
        URLLoader step = Mockito.mock(URLLoader.class);
        Task task = Mockito.mock(Task.class);

        given(dInjector.instance(Task.class)).willReturn(task);

        Task actual = taskFactory.createTask(step);

        verify(task).setStep(step);
        assertThat(actual).isSameAs(task);
    }

    @Test
    public void testGetStep() throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        URLLoader step = Mockito.mock(URLLoader.class);
        given(dInjector.instance(URLLoader.class)).willReturn(step);

        IStep actual = taskFactory.createStep(clzName);

        assertThat(actual).isInstanceOf(IStep.class);
        assertThat(actual).isSameAs(step);
    }

    @Test
    public void testGetStepClassCastException() throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        String notAStepClzName = "org.codetab.scoopi.model.Locator";

        testRule.expect(ClassCastException.class);
        taskFactory.createStep(notAStepClzName);
    }

    @Test
    public void testGetStepClassNotFoundException()
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        String invalidClzName = "org.codetab.scoopi.XYZ";

        testRule.expect(ClassNotFoundException.class);
        taskFactory.createStep(invalidClzName);
    }
}
