package org.codetab.scoopi.store;

import java.util.List;
import java.util.concurrent.TimeoutException;

import org.codetab.scoopi.exception.TransactionException;
import org.codetab.scoopi.model.Payload;

public interface IJobStore {

    enum State {
        NEW, INITIALIZE, READY
    }

    void open();

    void close();

    boolean putJob(Payload payload)
            throws InterruptedException, TransactionException;

    boolean putJobs(List<Payload> payloads, long jobId)
            throws InterruptedException, TransactionException;

    Payload takeJob()
            throws InterruptedException, TransactionException, TimeoutException;

    boolean markFinished(long id) throws TransactionException;

    boolean resetTakenJob(long jobId) throws TransactionException;

    void resetCrashedJobs();

    void setState(State state);

    long getJobIdSeq();

    boolean isDone();
}
