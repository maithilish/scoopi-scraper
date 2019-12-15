package org.codetab.scoopi.step;

import static org.apache.commons.lang3.Validate.notNull;

import javax.annotation.concurrent.GuardedBy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.log.Log.CAT;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.store.IJobStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class JobMediator {

    static final Logger LOGGER = LoggerFactory.getLogger(JobMediator.class);

    @Inject
    private TaskMediator taskMediator;
    @Inject
    private IJobStore jobStore;
    @Inject
    private ErrorLogger errorLogger;

    private JobRunnerThread jobRunner = new JobRunnerThread();

    @GuardedBy("this")
    private int reservations = 0;

    public void start() {
        jobRunner.start();
    }

    public void waitForFinish() {
        try {
            jobRunner.join();
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
        jobStore.putPayload(payload);
        return true;
    }

    private void initiateJob()
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, InterruptedException {
        Payload payload = jobStore.takePayload();
        synchronized (this) {
            --reservations;
        }
        taskMediator.pushPayload(payload);
    }

    class JobRunnerThread extends Thread {

        @Override
        public void run() {
            while (true) {
                synchronized (this) {
                    if (taskMediator.isDone() && reservations == 0) {
                        break;
                    }
                }
                try {
                    if (reservations > 0) {
                        initiateJob();
                    }
                } catch (ClassNotFoundException | InstantiationException
                        | IllegalAccessException | InterruptedException e) {
                    String message = "unable to initiate job";
                    errorLogger.log(CAT.ERROR, message, e);
                }
            }
        }
    }
}
