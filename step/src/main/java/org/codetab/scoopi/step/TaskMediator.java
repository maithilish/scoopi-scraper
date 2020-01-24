package org.codetab.scoopi.step;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.log.Log.CAT;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.step.pool.TaskPoolService;
import org.codetab.scoopi.store.IPayloadStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TaskMediator {

    static final Logger LOGGER = LoggerFactory.getLogger(TaskMediator.class);

    @Inject
    private Configs configs;
    @Inject
    private TaskPoolService poolService;
    @Inject
    private IPayloadStore payloadStore;
    @Inject
    private TaskFactory taskFactory;
    @Inject
    private ErrorLogger errorLogger;

    private TaskRunnerThread taskRunner = new TaskRunnerThread();

    private AtomicInteger reservations = new AtomicInteger();
    private AtomicReference<TMState> state =
            new AtomicReference<>(TMState.READY);

    private int taskGetRetryInterval;

    public void start() {
        taskGetRetryInterval = Integer.parseInt(
                configs.getConfig("scoopi.task.getRetryInterval", "10"));
        taskGetRetryInterval = 10;
        taskRunner.start();
    }

    public void waitForFinish() {
        try {
            taskRunner.join();
            state.set(TMState.TERMINATED);
        } catch (final InterruptedException e) {
            final String message = "wait for finish interrupted";
            errorLogger.log(CAT.INTERNAL, message, e);
        }
    }

    public boolean pushPayload(final Payload payload)
            throws InterruptedException {
        notNull(payload, "payload must not be null");

        TMState st = state.get();
        if (st.equals(TMState.SHUTDOWN) || st.equals(TMState.TERMINATED)) {
            throw new IllegalStateException(
                    "task mediator is terminated, can't push payload");
        }

        payloadStore.putPayload(payload);
        reservations.getAndIncrement();
        state.set(TMState.READY);

        return true;
    }

    public boolean isTaskPoolFree(final Payload payload) {
        final String poolName = payload.getStepInfo().getStepName();
        return poolService.isPoolFree(poolName);
    }

    public void setState(final TMState tMState) {
        this.state.set(tMState);
    }

    public TMState getState() {
        return state.get();
    }

    public boolean isDone() {
        return state.get().equals(TMState.DONE);
    }

    public boolean isTerminated() {
        return state.get().equals(TMState.TERMINATED);
    }

    class TaskRunnerThread extends Thread {
        @Override
        public void run() {
            int resWaitCount = 1;
            while (true) {
                if (state.get().equals(TMState.SHUTDOWN)) {
                    poolService.waitForFinish();
                    break;
                }
                if (reservations.get() == 0 && poolService.isDone()) {
                    state.set(TMState.DONE);
                }

                try {
                    int retryWait =
                            resWaitCount * resWaitCount * taskGetRetryInterval;
                    if (reservations.get() > 0) {
                        if (resWaitCount > 0) {
                            LOGGER.debug(
                                    "waited for task {} times, total {} ms",
                                    resWaitCount, retryWait);
                            resWaitCount = 0;
                        }
                        initiateTask();
                    } else {
                        resWaitCount++;
                        Thread.sleep(retryWait);
                    }
                } catch (ClassNotFoundException | InstantiationException
                        | IllegalAccessException | InterruptedException e) {
                    final String message = "unable to initiate task";
                    errorLogger.log(CAT.ERROR, message, e);
                }
            }
        }
    }

    private void initiateTask()
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, InterruptedException {

        final Payload payload = payloadStore.takePayload();
        reservations.getAndDecrement();

        final Task task = taskFactory.createTask(payload);
        final String poolName = task.getStep().getStepName();
        poolService.submit(poolName, task);
    }

}
