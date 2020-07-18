package org.codetab.scoopi.step.mediator;

import static org.apache.commons.lang3.Validate.notNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.model.ERROR;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.store.IPayloadStore;

@Singleton
public class TaskMediator {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private IPayloadStore payloadStore;
    @Inject
    private TaskRunner taskRunner;
    @Inject
    private StateFliper stateFliper;
    @Inject
    private Errors errors;

    public void start() {
        taskRunner.start();
    }

    public void waitForFinish() {
        try {
            taskRunner.join();
            stateFliper.setTMState(TMState.TERMINATED);
            LOG.info("task mediator state change {}", TMState.TERMINATED);
        } catch (InterruptedException e) {
            errors.inc();
            LOG.error("wait for finish interrupted [{}]", ERROR.INTERNAL, e);
            Thread.currentThread().interrupt();
        }
    }

    public boolean pushPayload(final Payload payload)
            throws InterruptedException {
        notNull(payload, "payload must not be null");

        TMState tmState = stateFliper.getTMState();
        if (tmState.equals(TMState.SHUTDOWN)
                || tmState.equals(TMState.TERMINATED)) {
            throw new IllegalStateException(
                    "task mediator is terminated, can't push payload");
        }

        stateFliper.setTMState(TMState.READY);
        payloadStore.putPayload(payload);
        return true;
    }
}
