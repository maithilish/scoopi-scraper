package org.codetab.scoopi.step.mediator;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.JobStateException;
import org.codetab.scoopi.exception.TransactionException;
import org.codetab.scoopi.model.ERROR;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.store.IJobStore;

import com.google.common.util.concurrent.Uninterruptibles;

public class JobRunner extends Thread {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private Configs configs;
    @Inject
    private TaskMediator taskMediator;
    @Inject
    private IJobStore jobStore;
    @Inject
    private StateFliper stateFliper;

    @Override
    public void run() {

        LOG.debug("take jobs from cluster and initiate task");
        int jobTakeRetryDelay =
                configs.getInt("scoopi.job.takeRetryDelay", "50");

        while (true) {
            if (stateFliper.isTMState(TMState.TERMINATED)
                    || stateFliper.isTMState(TMState.SHUTDOWN)) {
                break;
            }
            try {
                jobStore.resetCrashedJobs();

                // job store Semaphore throttles take job
                Payload payload = jobStore.takeJob();
                taskMediator.pushPayload(payload);
            } catch (NoSuchElementException e) {
                LOG.debug("{}, retry", e.getLocalizedMessage());
                Uninterruptibles.sleepUninterruptibly(jobTakeRetryDelay,
                        TimeUnit.MILLISECONDS);
            } catch (JobStateException | IllegalStateException
                    | TransactionException | InterruptedException
                    | TimeoutException e) {
                if (e instanceof IllegalStateException) {
                    LOG.error("unable to initiate job [{}]", ERROR.INTERNAL, e);
                }
                LOG.debug("task mediator state {}", stateFliper.getTMState());
                LOG.debug("retry initiate job, {}", e.getLocalizedMessage());
            }
        }
    }
}
