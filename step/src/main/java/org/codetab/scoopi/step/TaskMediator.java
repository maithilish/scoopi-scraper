package org.codetab.scoopi.step;

import static org.apache.commons.lang3.Validate.notNull;

import javax.annotation.concurrent.GuardedBy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.log.Log.CAT;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.step.pool.TaskPoolService;
import org.codetab.scoopi.store.IJobStore;
import org.codetab.scoopi.store.IPayloadStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TaskMediator {

    static final Logger LOGGER = LoggerFactory.getLogger(TaskMediator.class);
    private static final long SLEEP_MILLIS = 1000;

    @Inject
    private TaskPoolService poolService;
    @Inject
    private IPayloadStore payloadStore;
    @Inject
    private TaskFactory taskFactory;
    @Inject
    private ErrorLogger errorLogger;
    @Inject
    private IJobStore jobStore;

    private TaskRunnerThread taskRunner = new TaskRunnerThread();

    @GuardedBy("this")
    private int reservations = 0;
    @GuardedBy("this")
    private boolean done = false;

    public void start() {
        taskRunner.start();
    }

    public void waitForFinish() {
        try {
            taskRunner.join();
            synchronized (this) {
                done = true;
            }
        } catch (final InterruptedException e) {
            final String message = "wait for finish interrupted";
            errorLogger.log(CAT.INTERNAL, message, e);
        }
    }

    public boolean pushPayload(final Payload payload)
            throws InterruptedException {
        notNull(payload, "payload must not be null");
        synchronized (this) {
            ++reservations;
        }
        payloadStore.putPayload(payload);
        return true;
    }

    private void initiateTask()
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, InterruptedException {
        final Payload payload = payloadStore.takePayload();
        synchronized (this) {
            --reservations;
        }
        final Task task = taskFactory.createTask(payload);
        final String poolName = task.getStep().getStepName();
        poolService.submit(poolName, task);
    }

    // public void setJobsDone(final boolean jobsDone) {
    // // synchronized (this) {
    // // this.jobsDone = jobsDone;
    // // }
    // }

    public boolean isDone() {
        return (done);
    }

    class TaskRunnerThread extends Thread {
        @Override
        public void run() {
            int reservationWaitCount = 0;
            while (true) {
                synchronized (this) {
                    if (jobStore.isDone() && poolService.isDone()
                            && reservations == 0) {
                        poolService.waitForFinish();
                        break;
                    }
                }
                try {
                    if (reservations > 0) {
                        if (reservationWaitCount > 0) {
                            LOGGER.debug("wait for task: {} ms",
                                    reservationWaitCount * SLEEP_MILLIS);
                            reservationWaitCount = 0;
                        }
                        initiateTask();
                    } else {
                        Thread.sleep(SLEEP_MILLIS);
                        reservationWaitCount++;
                    }
                } catch (ClassNotFoundException | InstantiationException
                        | IllegalAccessException | InterruptedException e) {
                    final String message = "unable to initiate task";
                    errorLogger.log(CAT.ERROR, message, e);
                }
            }
        }
    }

    public boolean isTaskPoolFree(final Payload payload) {
        final String poolName = payload.getStepInfo().getStepName();
        return poolService.isPoolFree(poolName);
    }
}
