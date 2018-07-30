package org.codetab.scoopi.step;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.GuardedBy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.pool.TaskPoolService;
import org.codetab.scoopi.shared.StatService;
import org.codetab.scoopi.shared.StepService;
import org.codetab.scoopi.store.IStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TaskMediator {

    static final Logger LOGGER = LoggerFactory.getLogger(TaskMediator.class);

    @Inject
    private TaskPoolService poolService;
    @Inject
    private IStore store;
    @Inject
    private StepService stepService;
    @Inject
    private StatService statService;

    private TaskRunnerThread taskRunner = new TaskRunnerThread();
    private AtomicInteger jobIdCounter = new AtomicInteger();

    @GuardedBy("this")
    private int reservations = 0;

    public void start() {
        taskRunner.start();
    }

    public void waitForFinish() {
        try {
            taskRunner.join();
        } catch (InterruptedException e) {
            String message = "wait for finish interrupted";
            LOGGER.error("{}", message);
            LOGGER.debug("{}", message, e);
            statService.log(CAT.INTERNAL, message, e);
        }
    }

    public boolean pushPayload(final Payload payload)
            throws InterruptedException {
        synchronized (this) {
            ++reservations;
        }
        store.putPayload(payload);
        return true;
    }

    public int getJobId() {
        return jobIdCounter.incrementAndGet();
    }

    private void initiateTask()
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, InterruptedException {
        Payload payload = store.takePayload();
        synchronized (this) {
            --reservations;
        }
        Task task = stepService.createTask(payload);
        String poolName = task.getStep().getStepName();
        poolService.submit(poolName, task);
    }

    class TaskRunnerThread extends Thread {

        @Override
        public void run() {
            while (true) {
                synchronized (this) {
                    if (poolService.isDone() && reservations == 0) {
                        poolService.waitForFinish();
                        break;
                    }
                }
                try {
                    if (reservations > 0) {
                        initiateTask();
                    }
                } catch (ClassNotFoundException | InstantiationException
                        | IllegalAccessException | InterruptedException e) {
                    String message = "initiate task";
                    LOGGER.error("{}", message);
                    LOGGER.debug("{}", message, e);
                    statService.log(CAT.ERROR, message, e);
                }
            }
        }
    }
}
