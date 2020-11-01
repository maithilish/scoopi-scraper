package org.codetab.scoopi.store;

public interface IShutdown {

    void init();

    void setDone();

    boolean allNodesDone();

    boolean jobStoreDone();

    void setTerminate();

    void terminate();

    void cancel();

    boolean isCancelled();
}
