package org.codetab.scoopi.step;

import javax.annotation.concurrent.GuardedBy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.pool.TaskPoolService;
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
    private TaskRunnerThread taskRunner = new TaskRunnerThread();

    @GuardedBy("this")
    private int reservations = 0;

    public void start() {
        taskRunner.start();
    }

    public void waitToFinish() {
        try {
            taskRunner.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
        LOGGER.info("submit to " + poolName);
        poolService.submit(poolName, task);
    }

    public void pushPayload(final Payload payload) throws InterruptedException {
        synchronized (this) {
            ++reservations;
        }
        store.putPayload(payload);
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
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
