package org.codetab.scoopi.store;

import java.util.List;

import org.codetab.scoopi.model.Payload;

public interface IJobStore {

    enum State {
        NEW, INITIALIZE, READY
    }

    void open();

    void close();

    boolean putJob(Payload payload) throws InterruptedException;

    boolean putJobs(List<Payload> payloads, long jobId)
            throws InterruptedException;

    Payload takeJob() throws InterruptedException;

    boolean markFinished(long id);

    void resetCrashedJobs();

    int getJobCount();

    boolean isDone();

    State getState();

    void setState(State state);

    boolean changeStateToInitialize();

    String getMemberId();

    int getJobTakenCount();

    int getJobTakenByMemberCount();

    int getJobTakeLimit();

    long getJobIdSeq();
}
