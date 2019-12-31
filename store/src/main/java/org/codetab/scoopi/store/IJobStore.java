package org.codetab.scoopi.store;

import org.codetab.scoopi.model.Payload;

public interface IJobStore {

    enum State {
        NEW, INITIALIZE, READY
    }

    boolean open();

    boolean close();

    boolean createTables();

    boolean putJob(Payload payload) throws InterruptedException;

    Payload takeJob() throws InterruptedException;

    boolean markFinished(long id);

    int getJobCount();

    boolean isDone();

    State getState();

    void setState(State state);

    boolean changeStateToInitialize();

    String getNodeId();

    int getJobTakenCount();

    int getJobQueueSize();

    long getJobIdSeq();
}
