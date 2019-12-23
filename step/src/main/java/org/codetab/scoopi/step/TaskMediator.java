package org.codetab.scoopi.step;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.GuardedBy;
import javax.inject.Inject;
import javax.inject.Singleton;

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
    private static final long SLEEP_MILLIS = 1000;

    @Inject
    private TaskPoolService poolService;
    @Inject
    private IPayloadStore payloadStore;
    @Inject
    private TaskFactory taskFactory;
    @Inject
    private ErrorLogger errorLogger;

    private TaskRunnerThread taskRunner = new TaskRunnerThread();
    private AtomicInteger jobIdCounter = new AtomicInteger();

    @GuardedBy("this")
    private int reservations = 0;
    @GuardedBy("this")
    private boolean done = false;
    @GuardedBy("this")
    private boolean jobsDone = false;

    public void start() {
        taskRunner.start();
    }

    public void waitForFinish() {
        try {
            taskRunner.join();
            synchronized (this) {
                done = true;
            }
        } catch (InterruptedException e) {
            String message = "wait for finish interrupted";
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

    public int getJobId() {
        return jobIdCounter.incrementAndGet();
    }

    private void initiateTask()
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, InterruptedException {
        Payload payload = payloadStore.takePayload();
        synchronized (this) {
            --reservations;
        }
        Task task = taskFactory.createTask(payload);
        String poolName = task.getStep().getStepName();
        poolService.submit(poolName, task);
    }

    public void setJobsDone(final boolean jobsDone) {
        synchronized (this) {
            this.jobsDone = jobsDone;
        }
    }

    public boolean isDone() {
        return (done && jobsDone);
    }

    class TaskRunnerThread extends Thread {
        @Override
        public void run() {
            while (true) {
                synchronized (this) {
                    if (jobsDone && poolService.isDone() && reservations == 0) {
                        poolService.waitForFinish();
                        break;
                    }
                }
                try {
                    if (reservations > 0) {
                        initiateTask();
                    } else {
                        LOGGER.info("sleep 1s");
                        Thread.sleep(SLEEP_MILLIS);
                    }
                } catch (ClassNotFoundException | InstantiationException
                        | IllegalAccessException | InterruptedException e) {
                    String message = "unable to initiate task";
                    errorLogger.log(CAT.ERROR, message, e);
                }
            }
        }
    }

}
