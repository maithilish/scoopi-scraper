package org.codetab.scoopi.step;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.Validate.notNull;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.log.Log.CAT;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.step.pool.TaskPoolService;
import org.codetab.scoopi.store.IPayloadStore;
import org.codetab.scoopi.store.IShutdown;
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
    @Inject
    private IShutdown shutdown;

    private AtomicReference<TMState> state =
            new AtomicReference<>(TMState.READY);
    private TaskRunnerThread taskRunner = new TaskRunnerThread();
    private int taskPollRetryInterval;

    public void start() {
        taskPollRetryInterval = Integer.parseInt(
                configs.getConfig("scoopi.task.pollRetryInterval", "500"));
        taskRunner.start();
    }

    public void waitForFinish() {
        try {
            taskRunner.join();
            state.set(TMState.TERMINATED);
            LOGGER.info("task mediator state change {}", state);
        } catch (final InterruptedException e) {
            final String message = "wait for finish interrupted";
            errorLogger.log(CAT.INTERNAL, message, e);
        }
    }

    public boolean pushPayload(final Payload payload)
            throws InterruptedException {
        notNull(payload, "payload must not be null");

        if (state.get().equals(TMState.SHUTDOWN)
                || state.get().equals(TMState.TERMINATED)) {
            throw new IllegalStateException(
                    "task mediator is terminated, can't push payload");
        }

        state.set(TMState.READY);
        payloadStore.putPayload(payload);
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

    public boolean isState(final TMState other) {
        return state.get().equals(other);
    }

    class TaskRunnerThread extends Thread {
        @Override
        public void run() {
            int retryCount = 1;
            while (true) {
                if (state.get().equals(TMState.SHUTDOWN)) {
                    poolService.waitForFinish();
                    break;
                }
                if (payloadStore.getPayloadsCount() == 0
                        && poolService.isDone()) {
                    state.set(TMState.DONE);
                }
                try {
                    int takeTimeout = 0;
                    if (payloadStore.getPayloadsCount() == 0) {
                        takeTimeout = taskPollRetryInterval;
                    }
                    if (initiateTask(takeTimeout)) {
                        if (retryCount > 1) {
                            LOGGER.debug(
                                    "take task timeout {} ms, timed out {} times",
                                    takeTimeout, retryCount);
                            retryCount = 1;
                        }
                    } else {
                        retryCount++;
                    }
                } catch (ClassNotFoundException | InstantiationException
                        | IllegalAccessException | InterruptedException e) {
                    final String message = "unable to initiate task";
                    errorLogger.log(CAT.ERROR, message, e);
                }
            }
        }
    }

    private boolean initiateTask(final int timeout)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, InterruptedException {
        Payload payload = payloadStore.takePayload(timeout);
        if (isNull(payload)) {
            return false;
        }

        final Task task = taskFactory.createTask(payload);
        final String poolName = task.getStep().getStepName();
        poolService.submit(poolName, task);
        return true;
    }

    public boolean tryShutdown() {
        LOGGER.info("task mediator done, try shutdown");
        shutdown.setDone();
        if (shutdown.tryShutdown(shutdownFunction, this)) {
            LOGGER.info("task mediator shutdown successful");
            return true;
        } else {
            state.set(TMState.READY);
            LOGGER.info(
                    "task mediator shutdown failed, reset state back to ready");
            return false;
        }
    }

    private Function<TaskMediator, Boolean> shutdownFunction = tm -> {
        if (tm.getState().equals(TMState.DONE)) {
            tm.setState(TMState.SHUTDOWN);
            return true;
        } else {
            return false;
        }
    };

}
