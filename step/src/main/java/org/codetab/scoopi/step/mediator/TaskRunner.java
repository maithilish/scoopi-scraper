package org.codetab.scoopi.step.mediator;

import static java.util.Objects.isNull;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.model.ERROR;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.step.Task;
import org.codetab.scoopi.step.pool.TaskPoolService;
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
                if (initiateTask(takeTimeout)) {
                    if (retryCount > 1) {
                        LOG.debug("take task timeout {} ms, timed out {} times",
                                takeTimeout, retryCount);
                        retryCount = 1;
                    }
                } else {
                    retryCount++;
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
}
