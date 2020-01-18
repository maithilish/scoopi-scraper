package org.codetab.scoopi.store;

import java.util.List;

import org.codetab.scoopi.model.Payload;

public interface IJobStore {

    enum State {
        NEW, INITIALIZE, READY
    }

    boolean open();

    boolean close();

    boolean putJob(Payload payload) throws InterruptedException;

    Payload takeJob() throws InterruptedException;

    boolean putJobs(List<Payload> payloads, long jobId)
            throws InterruptedException;

    boolean markFinished(long id);

    boolean resetTakenJobs(String memberId);

    int getJobCount();

    boolean isDone();

    State getState();

    void setState(State state);

    boolean changeStateToInitialize();

    String getNodeId();

    int getJobTakenCount();

    int getJobTakenByMemberCount();

    int getJobTakeLimit();

    long getJobIdSeq();
}
