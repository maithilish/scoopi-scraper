package org.codetab.scoopi.shared;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.messages.Messages;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.step.IStep;
import org.codetab.scoopi.step.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class StepService {

    static final Logger LOGGER = LoggerFactory.getLogger(StepService.class);

    @Inject
    private DInjector dInjector;

    @Inject
    private StepService() {
    }

    /**
     * Safely create instance from clzName string using DI. Steps should use
     * this method to create any objects such as converters etc.
     * @param clzName
     * @return
     * @throws ClassNotFoundException
     */
    public Object createInstance(final String clzName)
            throws ClassNotFoundException {
        Class<?> clz = Class.forName(clzName);
        return dInjector.instance(clz);
    }

    /**
     * Create step and assign it to task.
     *
     * @param stepType
     *            type
     * @param taskClassName
     *            task class
     * @param input
     *            task input
     * @param labels
     *            step labels
     * @param fields
     *            task fields
     * @return task
     * @throws ClassNotFoundException
     *             exception
     * @throws InstantiationException
     *             exception
     * @throws IllegalAccessException
     *             exception
     */
    public Task createTask(final Payload payload) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        IStep step = getStep(payload.getStepInfo().getClassName());
        step.setPayload(payload);
        Task task = createTask(step);
        return task;
    }

    /**
     * Create task and assign step.
     * @param step
     *            to assign
     * @return task
     */
    public Task createTask(final IStep step) {
        Task task = dInjector.instance(Task.class);
        task.setStep(step);
        return task;
    }

    public IStep getStep(final String clzName) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        IStep step = null;
        Class<?> stepClass = Class.forName(clzName);
        Object obj = dInjector.instance(stepClass);
        if (obj instanceof IStep) {
            step = (IStep) obj;
        } else {
            throw new ClassCastException(Messages.getString("StepService.0") //$NON-NLS-1$
                    + clzName + Messages.getString("StepService.1")); //$NON-NLS-1$
        }
        return step;
    }

}
