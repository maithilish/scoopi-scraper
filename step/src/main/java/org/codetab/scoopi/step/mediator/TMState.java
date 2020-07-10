package org.codetab.scoopi.step.mediator;

/**
 * READY - accept tasks for execution
 * <p>
 * DONE - accepted tasks are completed and no task in pending in taskpool, but
 * may flip to READY state.
 * <p>
 * SHUTDOWN - no more tasks are accepted, wait for pool service to end and exit
 * the task runner
 * <p>
 * TERMINATED - exit
 * <p>
 * @author m
 *
 */
public enum TMState {
    READY, DONE, SHUTDOWN, TERMINATED;
}
