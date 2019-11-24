package org.codetab.scoopi.step;

import static org.apache.commons.lang3.Validate.notNull;
import static org.codetab.scoopi.util.Util.spaceit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.model.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TaskFactory {

    static final Logger LOGGER = LoggerFactory.getLogger(TaskFactory.class);

    @Inject
    private DInjector dInjector;

    @Inject
    private TaskFactory() {
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

        notNull(payload, "payload must not be null");

        IStep step = createStep(payload.getStepInfo().getClassName());
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

        notNull(step, "step must not be null");

        Task task = dInjector.instance(Task.class);
        task.setStep(step);
        return task;
    }

    public IStep createStep(final String clzName) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {

        notNull(clzName, "clzName must not be null");

        IStep step = null;
        Class<?> stepClass = Class.forName(clzName);
        Object obj = dInjector.instance(stepClass);
        if (obj instanceof IStep) {
            step = (IStep) obj;
        } else {
            throw new ClassCastException(
                    spaceit("step class:", clzName, "is not IStep type"));
        }
        return step;
    }

}
