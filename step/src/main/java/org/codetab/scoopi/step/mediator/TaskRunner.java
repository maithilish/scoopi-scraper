package org.codetab.scoopi.step.mediator;

import static java.util.Objects.isNull;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.model.ERROR;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.step.Task;
import org.codetab.scoopi.store.IPayloadStore;

public class TaskRunner extends Thread {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private Configs configs;
    @Inject
    private TaskPoolService poolService;
    @Inject
    private IPayloadStore payloadStore;
    @Inject
    private StateFliper stateFliper;
    @Inject
    private TaskFactory taskFactory;
    @Inject
    private Errors errors;

    private final int aquireLockTimeout = 50;

    @Override
    public void run() {

        int retryCount = 1;
        int taskTakeTimeout = configs.getInt("scoopi.task.takeTimeout", "500");

        while (true) {
            // monitor changes state to DONE and SHUTDOWN
            if (stateFliper.isTMState(TMState.SHUTDOWN)) {
                poolService.waitForFinish();
                break;
            }
            try {
                int takeTimeout = 0;
                if (payloadStore.getPayloadsCount() == 0) {
                    // no payload wait else take payload without wait
                    takeTimeout = taskTakeTimeout;
                }

                try {
                    stateFliper.acquireTaskQueueToPoolLock(aquireLockTimeout,
                            TimeUnit.MILLISECONDS);
                    Payload payload = payloadStore.takePayload(takeTimeout);

                    if (isNull(payload)) {
                        retryCount++;
                    } else {

                        logRetryCount(retryCount, takeTimeout);
                        retryCount = 1;

                        Task task = taskFactory.createTask(payload);
                        String poolName = task.getStep().getStepName();
                        poolService.submit(poolName, task);
                    }

                } finally {
                    stateFliper.releaseTaskQueueToPoolLock();
                }
            } catch (ClassNotFoundException | InstantiationException
                    | IllegalAccessException | InterruptedException e) {
                errors.inc();
                LOG.error("unable to initiate task [{}]", ERROR.INTERNAL, e);
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void logRetryCount(final int retryCount, final int takeTimeout) {
        if (retryCount > 1) {
            LOG.debug("take task timed out {} times, timeout {} ms", retryCount,
                    takeTimeout);
        }
    }
}
